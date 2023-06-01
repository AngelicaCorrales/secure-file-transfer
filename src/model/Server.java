package model;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 * El Server implementa un servidor que realiza un intercambio de claves usando el algoritmo de Diffie-Hellman,
 * descifra un archivo recibido del cliente y verifica la integridad del archivo.
 */
public class Server {
	private ServerSocket serverSocket;
	private Socket socket;
	public static  final int PORT = 12345;
	
	private DataInputStream input;
	private DataOutputStream output;

    private String fileExtension;
	
    /**
     *start permite iniciar el servidor.
     */
    public void start() {
    	System.setProperty("jdk.crypto.KeyAgreement.legacyKDF", "true");

        try {

        	connecting();
        	initializeDataStream();
        	//Diffie Hellman Key exchange
        	PublicKey clientPublicKey = getClientPublicKey();
            
        	KeyPair keyPair=generateServerKeys();
            
            PublicKey serverPublicKey = keyPair.getPublic();
            PrivateKey serverPrivateKey=keyPair.getPrivate();
            
            sendPublicKeyToClient(serverPublicKey);
            
            KeyAgreement keyAgreement= negotiatedKeyDiffieHellman(serverPrivateKey, clientPublicKey);

            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            //Decrypt
            
            SecretKeySpec secretKeySpec = keyToDecrypt(keyAgreement,sha);
            Cipher cipher =configureCipher(secretKeySpec);
            receivingFile(cipher);
            //calcular el hash sobre el archivo recibido, y compararlo con el hash recibido del cliente.
            checkFileIntegrity(sha);
           
            socket.close();
            serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * connecting permite establecer la conexión con el cliente.
     */
    private void connecting() throws IOException {
    	 serverSocket = new ServerSocket(PORT);
         System.out.println("waiting for client connection...");
         socket = serverSocket.accept();
         System.out.println("client connected");
    }

    /**
     * initializeDataStream inicializa los flujos de datos de entrada y salida.
     */
    private void initializeDataStream() throws IOException {

        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * getClientPublicKey obtiene la clave pública del cliente.
     */
    private PublicKey getClientPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    	int clientPublicKeyLength = input.readInt();
        byte[] clientPublicKeyBytes = new byte[clientPublicKeyLength];
        input.readFully(clientPublicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientPublicKeyBytes);
               
        return keyFactory.generatePublic(x509KeySpec);
    }

    /**
     * generateServerKeys permite generar un par de claves pública y privada para el servidor.
     */
    private KeyPair generateServerKeys() throws NoSuchAlgorithmException {
    	 KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
         keyPairGenerator.initialize(2048);
         return keyPairGenerator.generateKeyPair();
    }

    /**
     * sendPublicKeyToClient envía la clave pública del servidor al cliente.
     */
    private void sendPublicKeyToClient(PublicKey serverPublicKey) throws IOException {
    	 byte[] publicKeyBytes = serverPublicKey.getEncoded();
         output.writeInt(publicKeyBytes.length);
         output.write(publicKeyBytes);

    }

    /**
     * negotiatedKeyDiffieHellman realiza la negociación de claves Diffie-Hellman entre el servidor y el cliente.
     */
    private KeyAgreement negotiatedKeyDiffieHellman(PrivateKey serverPrivateKey, PublicKey clientPublicKey) throws NoSuchAlgorithmException, InvalidKeyException {
    	KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
        keyAgreement.init(serverPrivateKey);
        keyAgreement.doPhase(clientPublicKey, true);
        return keyAgreement;
    }

    /**
     * keyToDecrypt convierte la clave acordada en una clave para descifrar el archivo.
     */
    private SecretKeySpec keyToDecrypt(KeyAgreement keyAgreement, MessageDigest sha) throws NoSuchAlgorithmException {
    	//algoritmo AES con clave de 256 bits, usando la clave previamente negociada
       
        byte[] hashedKeyBytes = sha.digest(keyAgreement.generateSecret());
        return new SecretKeySpec(hashedKeyBytes, "AES");
    }

    /**
     * configureCipher configura el cifrado con la clave para descifrar el archivo y el vector de inicialización.
     */
    private Cipher configureCipher(SecretKeySpec secretKeySpec) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
    	 int ivLength = input.readInt();
         byte[] ivBytes = new byte[ivLength];
         input.readFully(ivBytes);
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivBytes));
         return cipher;
    }



    /**
     * receivingFile recibe el archivo cifrado del cliente, lo descifra y lo guarda.
     */
    private void receivingFile(Cipher cipher) throws IOException, IllegalBlockSizeException, BadPaddingException {
        byte[] fileExtensionBytes =new byte[input.readInt()];
        input.readFully(fileExtensionBytes);
        fileExtension= new String(fileExtensionBytes);
        try (FileOutputStream fileReceived = new FileOutputStream("files_received/fileReceived"+fileExtension)) {
            int bytesRead= input.readInt();
            byte[] encryptedBytes = new byte[bytesRead];
            input.readFully(encryptedBytes);

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            fileReceived.write(decryptedBytes);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * checkFileIntegrity verifica la integridad del archivo recibido comparando su hash con el hash recibido del cliente.
     */
    private void checkFileIntegrity(MessageDigest sha) throws IOException {

        byte[] hashBytes = new byte[input.readInt()];
        input.readFully(hashBytes);

        byte[] serverHashBytes = sha.digest(Files.readAllBytes(Paths.get("files_received/fileReceived"+fileExtension)));

        boolean compareHash = MessageDigest.isEqual(hashBytes, serverHashBytes);
        String message="File not transferred successfully";
        if (compareHash) {
            message="File transferred successfully";
        }
        System.out.println(message);
        byte[] messageBytes= message.getBytes();
        output.writeInt(messageBytes.length);
    	output.write(messageBytes);
        
    }
        
}