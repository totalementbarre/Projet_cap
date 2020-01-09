package database;

import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class DatabaseFiller {
    public static void main(String[] args) {
        // TODO see https://howtodoinjava.com/security/java-aes-encryption-example/ for cipher

        DBConnection dbConnection = new DBConnection();
        Session session = dbConnection.getSessionFactory().getCurrentSession();

        int selX = 2;
        int selY = 3;
        String password = "token";
        // TODO ADD X AND Y TO HASH


        String hashedPassword = hashPassword(selX, selY, password);
        UserInfos sebInfo = new UserInfos("seb", hashedPassword, "f32b7c00", null, selX, selY);
        Transaction transaction = session.beginTransaction();
        session.persist(sebInfo);
        transaction.commit();

        session.close();
    }

    public static String hashPassword(int selX, int selY, String password) {
        String result = "";

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update((selY + password).getBytes());
            byte[] digest = messageDigest.digest();
            String myHash = DatatypeConverter
                    .printHexBinary(digest).toUpperCase();

            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update((selX + myHash).getBytes());
            digest = messageDigest.digest();
            result = DatatypeConverter
                    .printHexBinary(digest).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String hashPasswordWithSeed(int seed, String hashedPassword) {
        String result = "";
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update((seed + hashedPassword).getBytes());
            byte[] digest = messageDigest.digest();
            result = DatatypeConverter
                    .printHexBinary(digest).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String encrypt(byte[] dataToEncrypt, String key) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key.toCharArray());
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(dataToEncrypt));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static byte[] decrypt(String strToDecrypt, String key) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key.toCharArray());
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}
