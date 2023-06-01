# Transferencia segura de archivos con esquema de clave simétrica 📲

## Información del proyecto🚀
Este proyecto consiste en el desarrollo de dos programas: un cliente y un servidor, que permiten la transferencia segura de archivos a través de una conexión en red. El objetivo principal es garantizar la confidencialidad y la integridad de los datos transmitidos.

El programa servidor actúa como receptor y espera la conexión del cliente en un puerto específico. Por su parte, el cliente recibe como parámetro el nombre del archivo que desea transferir. Una vez establecida la conexión entre el cliente y el servidor, se procede a negociar una clave de cifrado utilizando el algoritmo Diffie-Hellman.

Una vez completada la negociación de la clave, el cliente utiliza el algoritmo de cifrado AES con una clave de 256 bits previamente acordada para transferir el archivo de manera segura al servidor. Al finalizar la transferencia, el cliente calcula el hash SHA-256 del archivo transmitido y lo envía al servidor.

El servidor, por su parte, recibe el archivo y realiza el cálculo del hash SHA-256 sobre el mismo. A continuación, compara el hash recibido del cliente con el calculado localmente. Si ambos hashes son idénticos, se considera que el archivo se ha transferido adecuadamente, garantizando su integridad durante todo el proceso.

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
