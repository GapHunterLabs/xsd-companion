package dev.gaphunter.xsdcompanion.structure

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.PsiManager
import com.intellij.psi.xml.XmlFile
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.ui.JBUI
import dev.gaphunter.xsdcompanion.detection.XsdWsdlDetector
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

/**
 * Structure tree for the XSD/WSDL file currently open in the editor,
 * resolved across every file it reaches via `<xs:include>`/`<xs:import>`/
 * `<xs:redefine>` -- the direct fix for the leading competitor's central
 * complaint ("not recognizing correlations between multiple xsd files",
 * "was not able to parse it" on multi-file WSDL). Follows editor
 * selection automatically, same "one tool window, tracks the active file"
 * pattern as the platform's own Structure view. Zero network, zero
 * external parser: every node comes from real XML PSI walked by
 * [SchemaStructureBuilder]/[buildSchemaGraph].
 */
class SchemaStructureToolWindow(private val project: Project, toolWindow: ToolWindow) {

    val component: JPanel = JPanel(BorderLayout())

    private val rootNode = DefaultMutableTreeNode("No XSD/WSDL file open")
    private val treeModel = DefaultTreeModel(rootNode)
    private val tree = Tree(treeModel)

    init {
        component.border = JBUI.Borders.empty(8)
        tree.isRootVisible = true
        component.add(JScrollPane(tree), BorderLayout.CENTER)
        subscribeToEditorChanges(toolWindow)
        refreshFromActiveEditor()
    }

    // Scoped to the tool window's own Disposable, same as the Alarm in
    // GitlabPipelineToolWindow -- disconnects automatically when the tool
    // window closes, no manual dispose() call to remember/forget.
    private fun subscribeToEditorChanges(toolWindow: ToolWindow) {
        val bus = project.messageBus.connect(toolWindow.disposable)
        bus.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun selectionChanged(event: FileEditorManagerEvent) {
                refreshFor(event.newFile)
            }
        })
    }

    private fun refreshFromActiveEditor() {
        val selected = FileEditorManager.getInstance(project).selectedFiles.firstOrNull()
        refreshFor(selected)
    }

    private fun refreshFor(virtualFile: VirtualFile?) {
        val xmlFile = virtualFile?.let { PsiManager.getInstance(project).findFile(it) as? XmlFile }
        if (xmlFile == null || !XsdWsdlDetector.isXsdOrWsdl(xmlFile)) {
            rootNode.removeAllChildren()
            rootNode.userObject = "No XSD/WSDL file open"
            treeModel.reload()
            return
        }

        val rootUrl = xmlFile.virtualFile.url
        val nodesByUrl = mutableMapOf<String, SchemaFileNode>()
        val graph = buildSchemaGraph(rootUrl) { url ->
            nodesByUrl[url]?.let { return@buildSchemaGraph it }
            val file = if (url == rootUrl) xmlFile else SchemaStructureBuilder.loadFile(project, url)
            val node = file?.let { SchemaStructureBuilder.buildNode(it) }
            if (node != null) nodesByUrl[url] = node
            node
        }

        rootNode.removeAllChildren()
        rootNode.userObject = fileLabel(rootUrl)
        for (fileNode in graph) {
            val fileTreeNode = DefaultMutableTreeNode(fileLabel(fileNode.filePath))
            for (component in fileNode.components) {
                fileTreeNode.add(DefaultMutableTreeNode("${component.kind}: ${component.name}"))
            }
            for (unresolved in fileNode.unresolved) {
                fileTreeNode.add(DefaultMutableTreeNode("[unresolved] $unresolved"))
            }
            rootNode.add(fileTreeNode)
        }
        treeModel.reload()
        for (i in 0 until tree.rowCount) tree.expandRow(i)
    }

    private fun fileLabel(path: String): String = path.substringAfterLast('/').substringAfterLast('\\')
}
