/*this file is for getting the public and private key
 * I made this file to reduce the lines of code since i was copying the same thing thing
 * into both files
 */
package main.java;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
/*importing things to generate keys */
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.ECGenParameterSpec;


public class keyManagement {
    //trying to get private key from file
    public static PrivateKey getPrivateKey(String fileName) throws IOException,
     NoSuchAlgorithmException, InvalidKeySpecException{
        //convert file to bytes to read the bytes in it
        byte[] keyBytes=Files.readAllBytes(Paths.get(fileName));
        //encodes the bytes file
        //needs to be in pkc encode
        PKCS8EncodedKeySpec spec= new PKCS8EncodedKeySpec(keyBytes);
        //Getting algorithm for key maker
        KeyFactory kf= KeyFactory.getInstance("RSA");
        //generates private key
        return kf.generatePrivate(spec);
    }

    //trying to get public key from file
    public static PublicKey getPublicKey(String fileName)throws IOException,
     NoSuchAlgorithmException, InvalidKeySpecException{
        //reads file data iin bytes and encodes it
        byte[] keyBytes=Files.readAllBytes(Paths.get(fileName));
        X509EncodedKeySpec spec= new X509EncodedKeySpec(keyBytes);
        //Getting algorithm for key maker
        KeyFactory kf= KeyFactory.getInstance("RSA");
        //generates public key
        return kf.generatePublic(spec);
    }

    public static void main(String [] args){

    }
    
}
