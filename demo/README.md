# Cómo probar este plugin

Este plugin sirve para esquemas (XSD) y servicios SOAP (WSDL) que están
partidos en varios archivos: te lleva de un archivo a otro con Ctrl+Click
y te muestra, en una sola ventana, todo lo que declaran juntos.

## Parte 1: el caso XSD (`order.xsd`)

1. Abrí **`order.xsd`**.
2. Poné el cursor sobre `common-types.xsd` (dentro de `xs:include`) y
   hacé **Ctrl+Click**.
   - Debería abrirse ese archivo.
3. Volvé a `order.xsd` y abrí la ventana **XSD Structure** (borde
   derecho del IDE).
   - Deberías ver los tres archivos y, dentro de cada uno, lo que
     declara: `Order`, `AddressType`, `LineItemType`, etc.

## Parte 2: el caso WSDL (`order-service.wsdl`) — lo nuevo en 0.3.0

1. Abrí **`order-service.wsdl`**.
2. Mirá la ventana **XSD Structure**.
   - **Antes de esta versión no aparecía nada acá.** Ahora deberías ver
     `GetOrderRequest` y `GetOrderResponse` (declarados dentro del
     WSDL), más lo que viene de los archivos importados.
3. Ctrl+Click sobre `order-types.wsdl` (en la línea `wsdl:import ...
   location="..."`).
   - Debería abrir ese archivo. Ese es el caso que usa `location` y no
     `schemaLocation`.
4. Ctrl+Click sobre `common-types.xsd` (en la línea `xs:import ...
   schemaLocation="..."`, dentro de `wsdl:types`).
   - Debería abrir el `.xsd`.

## Parte 3: los dos casos que NO deberían dar aviso o sí

En `order-service.wsdl`:

- La línea con `not-here.xsd` **sí** debe aparecer subrayada, con el
  aviso "Cannot resolve schemaLocation 'not-here.xsd'". Está puesta a
  propósito para eso.
- La línea de más abajo `<soap:address location="http://..."/>` **no**
  debe tener ningún aviso: ahí `location` es la dirección del servicio,
  no un archivo.

## Si algo no se ve así

Sacá la captura igual y avisame qué punto no coincide.
