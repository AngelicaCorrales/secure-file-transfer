package model;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class Server {
	private ServerSocket serverSocket;
	private Socket socket;
	public static  final int PORT = 12345;
	
	private DataInputStream input;
	private DataOutputStream output;
	
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
    
    private void connecting() throws IOException {
    	 serverSocket = new ServerSocket(PORT);
         System.out.println("waiting for client connection...");
         socket = serverSocket.accept();
         System.out.println("client connected");
    }
    
    private void initializeDataStream() throws IOException {

        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }
    
    private PublicKey getClientPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    	int clientPublicKeyLength = input.readInt();
        byte[] clientPublicKeyBytes = new byte[clientPublicKeyLength];
        input.readFully(clientPublicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientPublicKeyBytes);
               
        return keyFactory.generatePublic(x509KeySpec);
    }
    
    private KeyPair generateServerKeys() throws NoSuchAlgorithmException {
    	 KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
         keyPairGenerator.initialize(2048);
         return keyPairGenerator.generateKeyPair();
    }
    
    private void sendPublicKeyToClient(PublicKey serverPublicKey) throws IOException {
    	 byte[] publicKeyBytes = serverPublicKey.getEncoded();
         output.writeInt(publicKeyBytes.length);
         output.write(publicKeyBytes);

    }
    
    private KeyAgreement negotiatedKeyDiffieHellman(PrivateKey serverPrivateKey, PublicKey clientPublicKey) throws NoSuchAlgorithmException, InvalidKeyException {
    	KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
        keyAgreement.init(serverPrivateKey);
        keyAgreement.doPhase(clientPublicKey, true);
        return keyAgreement;
    }
    
    private SecretKeySpec keyToDecrypt(KeyAgreement keyAgreement, MessageDigest sha) throws NoSuchAlgorithmException {
    	//algoritmo AES con clave de 256 bits, usando la clave previamente negociada
       
        byte[] hashedKeyBytes = sha.digest(keyAgreement.generateSecret());
        return new SecretKeySpec(hashedKeyBytes, "AES");
    }
    
    private Cipher configureCipher(SecretKeySpec secretKeySpec) throws InvalidKeyException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
    	 int ivLength = input.readInt();
         byte[] ivBytes = new byte[ivLength];
         input.readFully(ivBytes);
         Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
         cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivBytes));
         return cipher;
    }
    
    private void receivingFile(Cipher cipher) throws IOException, IllegalBlockSizeException, BadPaddingException {
    	 try (FileOutputStream fileReceived = new FileOutputStream("fileReceived.txt")) {
             int bytesRead;
             while ((bytesRead = input.readInt()) >0) {
                 byte[] encryptedBytes = new byte[bytesRead];
                 input.readFully(encryptedBytes);
                 byte[] decryptedBytes = cipher.update(encryptedBytes);
                 fileReceived.write(decryptedBytes);
             }
             byte[] encryptedBytes = new byte[input.readInt()];
             input.readFully(encryptedBytes);
             byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
             fileReceived.write(decryptedBytes);
         }
    }
    
    private void checkFileIntegrity(MessageDigest sha) throws IOException {
    	byte[] hashBytes = new byte[input.readInt()];
        input.readFully(hashBytes);
        byte[] serverHashBytes = sha.digest(Files.readAllBytes(Paths.get("fileReceived.txt")));
        boolean compareHash = MessageDigest.isEqual(hashBytes, serverHashBytes);
        if (compareHash) {
            System.out.println("File transferred successfully");
        } else {
            System.out.println("File not transferred successfully");
        }
    }
        
}
