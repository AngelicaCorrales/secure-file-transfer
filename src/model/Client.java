package model;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class Client {
    private Socket socket;
	public static  final int PORT = 12345;
    public static  final String SERVER_IP = "10.147.19.59";

    private DataInputStream input;
	private DataOutputStream output;
	private String fileName;

    /**
     * Establece el nombre del archivo que se transferirá.
     */
	public void setFileName(String fileName) {
		this.fileName=fileName.replace("\\","\\\\");
	}

    /**
     * Inicia la transferencia de archivos al servidor remoto.
     */
	public void startTransfer() {
    	System.setProperty("jdk.crypto.KeyAgreement.legacyKDF", "true");

        try {
            connecting();
            
            initializeDataStream();

            //Genera un par de claves (pública y privada)
            KeyPair keyPair=generateClientKeys();
            PublicKey clientPublicKey = keyPair.getPublic();
            PrivateKey clientPrivateKey=keyPair.getPrivate();

            //Envía la clave pública al servidor
            sendPublicKeyToServer(clientPublicKey);

            //Realiza el intercambio de claves de Diffie-Hellman
        	PublicKey serverPublicKey = getServerPublicKey();

            KeyAgreement keyAgreement=negotiatedKeyDiffieHellman(clientPrivateKey,serverPublicKey);

            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            
            //Encripta el archivo utilizando la clave acordada
            SecretKeySpec secretKeySpec = keyToEncrypt(keyAgreement,sha);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            sendIV(cipher);

            //Envía el archivo encriptado
            sendingFile(cipher);

            //Envía el hash del archivo al servidor
            sendingHashToServer(sha);
            
            receivingMessage();

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Establece la conexión con el servidor remoto.
     */
	private void connecting() throws IOException {
        socket = new Socket(SERVER_IP, PORT);
        System.out.println("Connected");
   }

    /**
     * Inicializa los flujos de datos de entrada y salida.
     */
    private void initializeDataStream() throws IOException {
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    /**
     * Genera un par de claves (pública y privada) utilizando el algoritmo Diffie-Hellman.
     */
    private KeyPair generateClientKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
   }

    /**
     * Envía la clave pública del cliente al servidor.
     */
   private void sendPublicKeyToServer(PublicKey clientePublicKey) throws IOException {
        byte[] publicKeyBytes = clientePublicKey.getEncoded();
        output.writeInt(publicKeyBytes.length);
        output.write(publicKeyBytes);
    }

    /**
     * Obtiene la clave pública del servidor desde el flujo de entrada.
     */
   private PublicKey getServerPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        int serverPublicKeyLength = input.readInt();
        byte[] serverPublicKeyBytes = new byte[serverPublicKeyLength];
        input.readFully(serverPublicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serverPublicKeyBytes);
        return keyFactory.generatePublic(x509KeySpec);
    }

    /**
     * Realiza el intercambio de claves de Diffie-Hellman, a partir de
     * la clave privada del cliente y la clave pública del servidor.
     */
    private KeyAgreement negotiatedKeyDiffieHellman(PrivateKey clientPrivateKey, PublicKey serverPublicKey) throws NoSuchAlgorithmException, InvalidKeyException {
    	KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
        keyAgreement.init(clientPrivateKey);
        keyAgreement.doPhase(serverPublicKey, true);
        return keyAgreement;
    }

    /**
     * Convierte la clave acordada en una especificación SecretKeySpec para encriptación.
     **/
    private SecretKeySpec keyToEncrypt(KeyAgreement keyAgreement, MessageDigest sha) throws NoSuchAlgorithmException {
    	byte[] hashedKeyBytes = sha.digest( keyAgreement.generateSecret());
        return new SecretKeySpec(hashedKeyBytes, "AES");
    }

    /**
     * Envía el vector de inicialización (IV) al servidor.
     */
    private void sendIV(Cipher cipher) throws Exception {
        byte[] ivBytes = cipher.getIV();
        output.writeInt(ivBytes.length);
        output.write(ivBytes);
    }

    /**
     * Envía el archivo encriptado al servidor.
     */
    private void sendingFile(Cipher cipher) throws IOException, IllegalBlockSizeException, BadPaddingException {
        //Enviar la extension del archivo
        String[] partsFile=fileName.split("\\.");
        String fileExtension="."+partsFile[partsFile.length-1];
        byte[] fileExtensionBytes= fileExtension.getBytes();
        output.writeInt(fileExtensionBytes.length);
        output.write(fileExtensionBytes);

        try (FileInputStream fileSent = new FileInputStream(fileName)) {
            byte[] fileBytes = new byte[fileSent.available()];
            fileSent.read(fileBytes);
            byte[] encryptedBytes = cipher.doFinal(fileBytes);
            output.writeInt(encryptedBytes.length);
            output.write(encryptedBytes);

        }

    }

    /**
     * Envía el hash del archivo al servidor.
     */
    private void sendingHashToServer(MessageDigest sha) throws IOException{
    	byte[] hashBytes = sha.digest(Files.readAllBytes(Paths.get(fileName)));
        output.writeInt(hashBytes.length);
        output.write(hashBytes);
    }

    /**
     * Recibe un mensaje del servidor.
     */
    private void receivingMessage() throws IOException {
    	byte[] messageBytes =new byte[input.readInt()];
    	input.readFully(messageBytes);
    	String message= new String(messageBytes);
    	
    	System.out.println(message);
    	
	}
}
