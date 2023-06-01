# Transferencia segura de archivos con esquema de clave simétrica 📲

## Información del proyecto🚀
Deben desarrollarse dos programas, uno cliente y uno servidor. El programa servidor debe escuchar por un puerto determinado, y esperar la conexión del cliente. El cliente recibe un nombre de archivo como parámetro. Una vez conectados cliente y servidor, el cliente debe negociar una clave de cifrado con el servidor empleando el algoritmo Diffie-Hellman, y luego transferir el archivo empleando el algoritmo AES con clave de 256 bits, usando la clave previamente negociada. Al final del proceso el cliente debe calcular el hash SHA-256 del archivo que acaba de transmitir, y enviarlo al servidor. El servidor debe calcular el hash sobre el archivo recibido, y compararlo con el hash recibido del cliente. Si son iguales, debe indicarse que el archivo se transfirió adecuadamente.

## ¿Cómo se hizó el programa? 👩‍💻🦾
En primer lugar, se desarrollaron dos clases modelo, uno para el cliente y otro para el servidor donde se realiza lo siguiente:

* El programa servidor actúa como receptor y espera la conexión del cliente en un puerto específico. 
* El cliente selecciona desde el explorador de archivos y envía como parámetro el nombre del archivo que desea transferir. 
* Una vez establecida la conexión entre el cliente y el servidor, se procede a negociar una clave de cifrado utilizando el algoritmo Diffie-Hellman.
* Una vez completada la negociación de la clave, el cliente utiliza el algoritmo de cifrado AES con una clave de 256 bits previamente acordada para transferir el archivo de manera segura al servidor. 
* Al finalizar la transferencia, el cliente calcula el hash SHA-256 del archivo transmitido y lo envía al servidor.
* El servidor, por su parte, recibe el archivo y realiza el cálculo del hash SHA-256 sobre el mismo. 
* A continuación, compara el hash recibido del cliente con el calculado localmente. Si ambos hashes son idénticos, se considera que el archivo se ha transferido adecuadamente, garantizando su integridad durante todo el proceso.

En segundo lugar, se implementó un main para el cliente el cual cuenta con interfaz grafica con ayuda de Scene Builder, esto con el fin de que visualmente sea más facil la selección del archivo que se desea transferir al servidor.

Por otro lado, se implementó el main del servidor que consiste en bucle while que tiene como función no parar el servidor hasta que cliente deje de mandar archivos.

Por ultimo, el proceso de transferencia que se lleva a cabo entre el servidor y el cliente se visualiza en consola, al recibir el mensaje "File transferred successfully" desde el lado del cliente y servidor esto indica que no hubo problema alguno con la transferencia del arhivo. Sin embargo si el mensaje es "File not transferred successfully" esto indica que las claves no coinciden y por ende no se puede enviar el archivo.

## ¿Cuales fueron las dificultades? ❌
* Problema de transmisión de archivos: Inicialmente, se creyó que el problema residía en el formato del archivo, ya que no era posible enviar archivos en formato de texto plano (txt) al inicio, solo imágenes. Sin embargo, más tarde se descubrió que la causa era la cantidad de bytes que se estaban enviando.
* Debido a nuestra falta de experiencia en la API criptográfica de Java, fue necesario llevar a cabo una investigación adicional que incluyó revisar documentación, consultar ejemplos y videos, entre otras fuentes de información.
* Se realizó un análisis exhaustivo sobre cómo llevar a cabo la negociación de claves utilizando el algoritmo de Diffie-Hellman.

## Conclusiones ✅
* La seguridad es un factor crucial a considerar al desarrollar un programa destinado a ser lanzado en producción. Garantizar la protección de los datos y sistemas es fundamental para prevenir posibles brechas de seguridad, pérdida de información confidencial y potenciales ataques cibernéticos. Al implementar medidas sólidas de seguridad, como el uso de autenticación, encriptación y prácticas de codificación seguras, se pueden minimizar los riesgos y salvaguardar la integridad de la aplicación.
* La transferencia de archivos cifrados desempeña un papel fundamental en la protección de la información confidencial durante su transmisión. Al emplear técnicas de cifrado, se garantiza que solo las partes autorizadas puedan acceder y comprender la información transmitida, evitando así que personas no autorizadas intercepten y obtengan acceso a datos sensibles.

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
