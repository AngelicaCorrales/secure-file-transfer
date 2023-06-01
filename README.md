# Transferencia segura de archivos con esquema de clave simétrica

## Información del proyecto🚀
Deben desarrollarse dos programas, uno cliente y uno servidor. El programa servidor debe escuchar por un puerto determinado, y esperar la conexión del cliente. El cliente recibe un nombre de archivo como parámetro. Una vez conectados cliente y servidor, el cliente debe negociar una clave de cifrado con el servidor empleando el algoritmo Diffie-Hellman, y luego transferir el archivo empleando el algoritmo AES con clave de 256 bits, usando la clave previamente negociada. Al final del proceso el cliente debe calcular el hash SHA-256 del archivo que acaba de transmitir, y enviarlo al servidor. El servidor debe calcular el hash sobre el archivo recibido, y compararlo con el hash recibido del cliente. Si son iguales, debe indicarse que el archivo se transfirió adecuadamente.

## Instalación 🔧💻
* Este proyecto es compatible con todos los sistemas operativos como Windows, Linux, MacOS.
* Para ejecutar el programa necesita un JDK mínimo de: "jdk 1.8.0_281".
    
## Realizado con 🛠
* [IntelliJ IDEA](https://www.jetbrains.com/es-es/idea/download) - IDE utilizado.
* Java - Lenguaje de programación aplicado.
* [Scene Builder](https://gluonhq.com/products/scene-builder/) - Herramienta de diseño utilizada en la construcción de la interfaz gráfica.

## Autores ✒
* *Angélica Corrales Quevedo* - [AngelicaCorrales](https://github.com/AngelicaCorrales).
* *Keren López Córdoba* - [KerenLopez](https://github.com/KerenLopez).
* *Diana Sofia Olano Montaño* - [DianaSofiaOlano](https://github.com/DianaSofiaOlano).
