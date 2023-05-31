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
    public static  final String SERVER_IP = "localhost";

    private Cipher cipher;
    private DataInputStream input;
	private DataOutputStream output;

    public void start(String[] args) {
    	System.setProperty("jdk.crypto.KeyAgreement.legacyKDF", "true");

        checkArguments(args);

        String FILE_NAME = args[0];

        try {
            connecting();
            
            initializeDataStream();

            //Generated pair keys (public and private)
            KeyPair keyPair=generateClientKeys();
            PublicKey clientPublicKey = keyPair.getPublic();
            PrivateKey clientPrivateKey=keyPair.getPrivate();

            //Send key to server
            sendPublicKeyToServer(clientPublicKey);

            //Diffie Hellman Key exchange
        	PublicKey serverPublicKey = getServerPublicKey();

            KeyAgreement keyAgreement=negotiatedKeyDiffieHellman(clientPrivateKey,serverPublicKey);

            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            
            //Decrypt
            SecretKeySpec secretKeySpec = keyToDecrypt(keyAgreement,sha);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
           
            sendIV(cipher,secretKeySpec);

            sendingFile(cipher, FILE_NAME);

            //Sending hash to server
            sendingHashToServer(sha,FILE_NAME);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void checkArguments(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Cliente <nombre_archivo>");
            return;
        }
    }

    private void connecting() throws IOException {
        socket = new Socket(SERVER_IP, PORT);
        System.out.println("Connected");
   }

    private void initializeDataStream() throws IOException {
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
    }

    private KeyPair generateClientKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.generateKeyPair();
   }

   private void sendPublicKeyToServer(PublicKey clientePublicKey) throws IOException {
        byte[] publicKeyBytes = clientePublicKey.getEncoded();
        output.writeInt(publicKeyBytes.length);
        output.write(publicKeyBytes);
    }
   

   private PublicKey getServerPublicKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        int serverPublicKeyLength = input.readInt();
        byte[] serverPublicKeyBytes = new byte[serverPublicKeyLength];
        input.readFully(serverPublicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serverPublicKeyBytes);
        return keyFactory.generatePublic(x509KeySpec);
    }

    private KeyAgreement negotiatedKeyDiffieHellman(PrivateKey clientPrivateKey, PublicKey serverPublicKey) throws NoSuchAlgorithmException, InvalidKeyException {
    	KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
        keyAgreement.init(clientPrivateKey);
        keyAgreement.doPhase(serverPublicKey, true);
        return keyAgreement;
    }

    private SecretKeySpec keyToDecrypt(KeyAgreement keyAgreement, MessageDigest sha) throws NoSuchAlgorithmException {
    	byte[] hashedKeyBytes = sha.digest( keyAgreement.generateSecret());
        SecretKeySpec secretKeySpec = new SecretKeySpec(hashedKeyBytes, "AES");
        return new SecretKeySpec(hashedKeyBytes, "AES");
    }

    private void sendIV(Cipher cipher, SecretKeySpec secretKeySpec) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] ivBytes = cipher.getIV();
        output.writeInt(ivBytes.length);
        output.write(ivBytes);
    }

    private void sendingFile(Cipher cipher, String file) throws IOException, IllegalBlockSizeException, BadPaddingException {
    	try (FileInputStream fileSent = new FileInputStream(file)) {
            byte[] buffer = new byte[64];
            int bytesRead;
            while ((bytesRead = fileSent.read(buffer)) != -1) {
                byte[] encryptedBytes = cipher.update(buffer, 0, bytesRead);
                output.writeInt(encryptedBytes.length);
                output.write(encryptedBytes);
            }
            byte[] encryptedBytes = cipher.doFinal();
            output.writeInt(encryptedBytes.length);
            output.write(encryptedBytes);
        }
    }

    private void sendingHashToServer(MessageDigest sha, String file) throws IOException{
    	byte[] hashBytes = sha.digest(Files.readAllBytes(Paths.get(file)));
        output.writeInt(hashBytes.length);
        output.write(hashBytes);
    }
}
