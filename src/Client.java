//package model;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class Client {

    public static void main(String[] args) {
    	System.setProperty("jdk.crypto.KeyAgreement.legacyKDF", "true");

        if (args.length != 1) {
            System.out.println("Uso: java Cliente <nombre_archivo>");
            return;
        }

        String nombreArchivo = args[0];
        String servidorIP = "localhost";
        int puerto = 12345;

        try {
            Socket socket = new Socket(servidorIP, puerto);
            System.out.println("Connected");
            
            DataInputStream entrada = new DataInputStream(socket.getInputStream());
            DataOutputStream salida = new DataOutputStream(socket.getOutputStream());

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DiffieHellman");
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PublicKey clientePublicKey = keyPair.getPublic();
            byte[] publicKeyBytes = clientePublicKey.getEncoded();
            salida.writeInt(publicKeyBytes.length);
            salida.write(publicKeyBytes);

            int serverPublicKeyLength = entrada.readInt();
            byte[] serverPublicKeyBytes = new byte[serverPublicKeyLength];
            entrada.readFully(serverPublicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(serverPublicKeyBytes);
            PublicKey serverPublicKey = keyFactory.generatePublic(x509KeySpec);

            PrivateKey privateKey=keyPair.getPrivate();
            KeyAgreement keyAgreement = KeyAgreement.getInstance("DiffieHellman");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(serverPublicKey, true);

            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hashedKeyBytes = sha.digest( keyAgreement.generateSecret());
            SecretKeySpec secretKeySpec = new SecretKeySpec(hashedKeyBytes, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] ivBytes = cipher.getIV();
            salida.writeInt(ivBytes.length);
            salida.write(ivBytes);

            try (FileInputStream archivoEntrada = new FileInputStream(nombreArchivo)) {
                byte[] buffer = new byte[64];
                int bytesRead;
                while ((bytesRead = archivoEntrada.read(buffer)) != -1) {
                    byte[] encryptedBytes = cipher.update(buffer, 0, bytesRead);
                    salida.writeInt(encryptedBytes.length);
                    salida.write(encryptedBytes);
                }
                byte[] encryptedBytes = cipher.doFinal();
                salida.writeInt(encryptedBytes.length);
                salida.write(encryptedBytes);
            }

 
            byte[] hashBytes = sha.digest(Files.readAllBytes(Paths.get(nombreArchivo)));
            salida.writeInt(hashBytes.length);
            salida.write(hashBytes);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
