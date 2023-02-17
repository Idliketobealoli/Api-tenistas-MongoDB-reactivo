# AD-P03-TennislabNoSQL

## ¿Qué es TennislabNoSQL?

Se trata de una aplicación centrada en bases de datos no relacionales, en este caso hacemos uso de [mongo](https://www.mongodb.com/es) en ambos casos, en la primera aplicación trabajamos con Mongo a pelo, mientras que en la segunda aplicación hemos convertido la aplicación para trabajar con [Spring Data](https://spring.io/projects/spring-data).

## Planteamiento

![diagrama](https://github.com/IvanAzagraTroya/AD-P03-TennislabNoSQL/blob/baef18306d766606908d8fe6da440e4b69de49a6/TennisLab-Mongo/diagram/diagrama.png)

Tenemos varios modelos con los que trabajar de los que hablaré a continuación:

- **Pedido**: Este objeto está compuesto con un identificador, los estados que puede tener el pedido y las fechas requeridas, estas son la fecha de entrada del pedido, la fecha programada de salida, la fecha real de salida, y la fecha tope de entrega, este planteamiento viene dado para tener un registro de cuándo se recibe el pedido, cuándo es la estimación en la que se piensa que se entregará para dar una fecha al cliente cuando se recibe el pedido, esta fecha después se puede actualizar a la fecha de salida real, que podría no ser la misma y por último tendremos la fecha de entrega, que podría variar en unos pocos días, ya que cuando sale el pedido de las instalaciones no tiene por qué ser el mismo día que el pedido llegue al cliente, o que el mismo lo recoja.

- **Tarea**: Este objeto está compuesto por un identificador propio, un identificador de la raqueta, precio que va a tener, el tipo de tarea a realizar, un atributo para marcar si está finalizada o no, el id del pedido al que pertenece la tarea, el id del producto adquirido que puede ser nulo porque puede no ser una tarea de tipo adquisición sino una tarea de tipo personalización o de encordaje, en el caso de las personalizaciones tenermos los atributos de peso de tipo int nullable, balance de tipo doble nullable, rigidez de tipo int nullable por lo dicho anteriormente y por último los atributos de la tarea de encordaje que son la tensión horizontal, identificador del cordaje horizontal, tensión vertical, identificación del cordaje vertical y un atributo de tipo boolean para definir si tiene dos nudos o no, todos estos también nullables.

- **Producto**: Este objeto está compuesto de un identificador, el **tipo de producto** que es, ya que podrían ser **raquetas, cordajes, overgrips, grips, antivibradores y fundas**,
  el modelo también tiene registros de las marcas y modelos los cuáles son cadenas de texto, el precio del producto que se trata con un Double y el stock actual.

- **User**: Este objeto será la representación de los usuarios del sistema, se compone de un identificador, el nombre, apellido, teléfono, email, la contraseña que será encriptada con sha512, el tipo de perfil que podrá ser **admin, worker o client** y por último un valor de si está activo o no para conservar el registro pero sin poder acceder a la cuenta en caso de estar inactiva. Guardamos los datos pero no se permite más el acceso si la cuenta está inactiva.

- **Turno**: Este objeto representa la jornada con un identificador en la que se ha completado una o dos tareas, las cuales son referenciadas a través de su identificador, este modelo se compone por la referencia al identificador del trabajador, y al de la máquina, las horas de inicio y de fin que son dos **LocalDateTime** y el número de pedidos activos, solo es obligatoria una referencia a una tarea, ya que es necesario que mínimo haya una tarea para guardar el registro del turno, por ello la segunda es de tipo **String?** con lo que admitimos valores nullables en este.

- **Máquina**: Se trata de la representación de la máquina que se haya utilizado para trabajar durante el turno, está compuesto por un identificador, modelo, marca y número de serie de tipo **String**, la fecha de adquisición de la máquina **LocalDate**, el tipo de la máquina que podrá ser **encordadora o personalizadora**, un atributo de tipo **boolean** para definir si la máquina está en activo o no y por último se encapsulan los datos en el valor data representado el cuál será los atributos de específicos del tipo de máquina del que se trata. Estos atributos encapsulados son en caso de la _encordadora_ un atributo para saber si es manual de tipo **boolean**, tensión máxima y tensión mínima de tipo **doble** nullable, en el caso de las personalizadoras utilizarán los atributos que indican si mide la maniobravilidad, si mide la rigidez y si mide el balance todos estos de tipo **boolean** nullable. Estos últimos atributos son nullables porque dependiendo del tipo de máquina del que se trate utilizarán unos atributos u otros.

### En cuanto a las relaciones:

- **Pedido-User**: Se trata de una relación _0..N-1_ ya que el usuario puede darse de alta en la plataforma pero no realizar ningún pedido, o por otro lado podría realizar múltiples, sin embargo para que haya un pedido debe de existir un usuario que lo realice.

- **Pedido-Tarea**: Se trata de una relación _1-1..N_ ya que cada pedido está compuesto por mínimo una tarea y puede tener tantas tareas como requisitos establezca el cliente.

- **User-Turno**: Se trata de una relación _1-0..N_ ya que tiene que haber un usuario de tipo _worker_ para poder realizar el turno de trabajo, sin un trabajador disponible no puede haber un turno y por ello no hay una máquina en uso ni una o varias tareas que se vayan a cumplir durante ese turno.

- **Tarea-Producto**: Se trata de una relación 0..N-1 ya que el producto existirá aunque no haya una tarea existente en ese momento, sin embargo también puede haber varias tareas que requieran de los productos.

- **Tarea-Turno**: Se trata de una relación 1..N-1 ya que puede haber entre 1 y varias tareas durante el transcurso de un turno y por la existencia de las tareas habrá un turno en las que se irán cumpliendo.

- **Turno-Maquina**: Se trata de una relación 0..N-1 ya que la máquina siempre va a existir aunque no se esté realizando ningún turno en ese momento, mientras que puede haber varios turnos que hagan uso de máquinas.

## ¿Cómo funciona?

Se conecta a una base de datos de mongo, almacenamos la línea de conexión en el archivo de configuración situado en [esta carpeta](https://github.com/IvanAzagraTroya/AD-P03-TennislabNoSQL/blob/develop/TennisLab-Mongo/src/main/resources/config.properties).
Al iniciar la aplicación el servidor recogerá todos los datos almacenados en la base de datos y los cargará para su acceso mientras que el cliente tendrá un cli (Command Line Interface) con los menús correspondientes para acceder a las distintas opciones provistas por la aplicación.
Tenemos un sistema de verificación por tokens por lo que el usuario de un cliente no podrá hacer uso de las funciones provistas para la administración del sistema.
Mediante el uso de una caché con la librería [cache4k](https://github.com/ReactiveCircus/cache4k) al hacer peticiones estos datos se guardarán en la caché para agilizar el ritmo de consultas y cada cierto tiempo se refrescará para mantenerse actualizado con posibles cambios que hayan podido haber en la fuente de estos mismos datos.

## Tecnologías:

<p align="center">
  
  <a href="https://www.mongodb.com/es">
    <img src="https://user-images.githubusercontent.com/67174666/212279453-f2a9887b-29d3-4394-a753-b0ef7d3a428a.png" width="120" heigth="120" alt="Mongo"/>
  </a>
  <a href="https://spring.io/projects/spring-data">
    <img src="https://user-images.githubusercontent.com/67174666/212282111-85be3f7e-e6e8-4cce-83a2-5abbaf9a3517.png" width="90" heigth="90" alt="Spring Data"/>
  </a>
  <a href="https://insert-koin.io/">
    <img src="https://user-images.githubusercontent.com/67174666/212279657-d90c0aa4-8741-456c-9f70-e02887f204db.png" width="200" heigth="200" alt="Koin"/>
  </a>
  <a href="https://kotlinlang.org/">
    <img src="https://user-images.githubusercontent.com/67174666/212279750-122c6f68-7b30-4ba8-a003-1db50df5feec.png" width="90" heigth="90" alt="Kotlin"/>
  </a>

</p>

### Autores:
- [Daniel Rodriguez Muñoz](https://github.com/Idliketobealoli)
- [Iván Azagra Troya](https://github.com/IvanAzagraTroya)
