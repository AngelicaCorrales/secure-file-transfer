//package model;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class Server {

    public static void main(String[] args) {
    	System.setProperty("jdk.crypto.KeyAgreement.legacyKDF", "true");

        int puerto = 12345;

        try {

            ServerSocket servidorSocket = new ServerSocket(puerto);
            System.out.println("Esperando conexión del cliente...");
            Socket socket = servidorSocket.accept();
            System.out.println("Cliente conectado.");


            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());

    
            int clientPublicKeyLength = entrada.readInt();
            byte[] clientPublicKeyBytes = new byte[clientPublicKeyLength];
            entrada.readFully(clientPublicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientPublicKeyBytes);
            PublicKey clientPublicKey = keyFactory.generatePublic(x509KeySpec);


            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey serverPublicKey = keyPair.getPublic();
            byte[] publicKeyBytes = serverPublicKey.getEncoded();
            salida.writeInt(publicKeyBytes.length);
            salida.write(publicKeyBytes);

        
            PrivateKey privateKey=keyPair.getPrivate();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(clientPublicKey, true);

           
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hashedKeyBytes = sha.digest(keyAgreement.generateSecret());
            SecretKeySpec secretKeySpec = new SecretKeySpec(hashedKeyBytes, "AES");

           
            int ivLength = entrada.readInt();
            byte[] ivBytes = new byte[ivLength];
            entrada.readFully(ivBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivBytes));

            try (FileOutputStream archivoSalida = new FileOutputStream("archivo_recibido.txt")) {
                int bytesRead;
                while ((bytesRead = entrada.readInt()) >0) {
                    byte[] encryptedBytes = new byte[bytesRead];
                    entrada.readFully(encryptedBytes);
                    byte[] decryptedBytes = cipher.update(encryptedBytes);
                    archivoSalida.write(decryptedBytes);
                }
                byte[] encryptedBytes = new byte[entrada.readInt()];
                entrada.readFully(encryptedBytes);
                byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
                archivoSalida.write(decryptedBytes);
            }

            
            byte[] hashBytes = new byte[entrada.readInt()];
            entrada.readFully(hashBytes);
            byte[] serverHashBytes = sha.digest(Files.readAllBytes(Paths.get("archivo_recibido.txt")));
            boolean hashCorrecto = MessageDigest.isEqual(hashBytes, serverHashBytes);
            if (hashCorrecto) {
                System.out.println("El archivo se transfirió adecuadamente.");
            } else {
                System.out.println("El archivo no se transfirió correctamente.");
            }

           
            socket.close();
            servidorSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
