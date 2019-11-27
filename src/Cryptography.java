import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

public abstract class Cryptography
{
    private static final String SIGNATURE = "SHA256withRSA";
    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    private static Signature signature;
    private static KeyPairGenerator keyPairGenerator;
    private static KeyPair keyPair;

    public static KeyPair getKeyPair()
    {
        return keyPair;
    }

    private static Cipher cipher;
    private static byte[] encryptedText = null;


    public static void init()   //A szerver main függvényében hívjuk meg
    {
        try
        {
            signature = Signature.getInstance(SIGNATURE);
            keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
            cipher = Cipher.getInstance(TRANSFORMATION);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
    }

    //Kliens oldalon hívjuk meg
    public static void initClient(KeyPair keys)
    {
        try
        {
            signature = Signature.getInstance(SIGNATURE);
            keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(2048);
            keyPair = keys;
            cipher = Cipher.getInstance(TRANSFORMATION);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException e)
        {
            e.printStackTrace();
        }
    }


    public static byte[] encryptString(String string)
    {
        try
        {
            cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
            byte[] input = string.getBytes();
            cipher.update(input);
            encryptedText = cipher.doFinal();
        }
        catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e)
        {
            e.printStackTrace();
        }
        return encryptedText;
    }

    public static String decryptToString(byte[] input)
    {
        try
        {
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            byte[] decryptedText = cipher.doFinal(input);
            return new String(decryptedText, StandardCharsets.UTF_8);
        }
        catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
