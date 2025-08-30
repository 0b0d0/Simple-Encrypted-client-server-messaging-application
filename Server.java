import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.*;
import javax.crypto.BadPaddingException;
//importing libraries for encryption
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.PrivateKey;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.ECGenParameterSpec;

//import PEM handler store keys in PEM FORMAT
import javax.xml.bind.DatatypeConverter;
//for encoding decryoted bytes message into string
import java.nio.charset.StandardCharsets;

/*trying to import function from another file */
import main.java.keyManagement;



public class Server {
    private static final int PORT = 5050;
    //list of clients
    //making it public to test something
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());


    //making function to generate keys
    public static KeyPair generateRSAKKeyPair() throws IOException, NoSuchAlgorithmException,
     NoSuchProviderException{
        //chooses algorithm
        KeyPairGenerator generate= KeyPairGenerator.getInstance("RSA");
        generate.initialize(2048);
        return generate.generateKeyPair();
            
        }
        
        //making function to write to file
        public static void saveKeysInFiles(){
            //keys need to be stored in a pem file
            try{
                //trying to get a certificate

                KeyPair keyPair= generateRSAKKeyPair();
                //wrting key to file 
                //using keypair value holder

                
                FileOutputStream writePublicKey= new FileOutputStream("publicKey.txt");
                //making sure it is in PEM format

                //writePublicKey.write(pKey);
                //use get encode method and writing public key
                writePublicKey.write(keyPair.getPublic().getEncoded());
                writePublicKey.close();

                //writing private key
                FileOutputStream writePrivateKey= new FileOutputStream("privateKey.txt");
                //ensuring key in text is in PEM FORMAT
                
                //use get encode method and writing private key to store the value
                writePrivateKey.write(keyPair.getPrivate().getEncoded());
                writePrivateKey.close();
            } catch(IOException| NoSuchAlgorithmException| NoSuchProviderException e){
                System.out.println("Error occured trying to write key to file");
            }
        }
        


    //instead of displaying this to terminal display to client gui
    public static void main(String[] args){

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            //waits for clients to connect
            System.out.println("Server started, waiting for clients...");
            
            //calling function save keys in the files
            //function works
            saveKeysInFiles();

            while (true) {
                //Listenes for incoming client requests never stos listening
                Socket clientSocket = serverSocket.accept(); //accepts new clients when they connect
                //get ip address of clientsocket
                System.out.println("New client connected");
                
                
                //new thread
                //handles client
                //object for each client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler); //adds client to list
                //makes thread to start - .start()
                clientHandler.start();
            

            }
        } catch (IOException | IllegalThreadStateException e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }
    //helps display plain text messages to many clients
    public static void broadcastMessage(String message, ClientHandler sender) {
        synchronized (clients) {
            //searhes through each item in list called clients
            //as it loops it sends the message to each client
            for (ClientHandler client : clients) {
                //checks if client is not sender
                if (client != sender) {
                    /*calls the clients string named message */
                    client.sendMessage(message);
                    
                }
            }
        }
    }

    //helps display plain text messages to many clients
    public static void broadcastDecryptedMessage(String message, ClientHandler sender) {
        synchronized (clients) {
            //searhes through each item in list called clients
            //as it loops it sends the message to each client
            //client is equal to clients
            for (ClientHandler client : clients) {
                if(client!=sender){
                    client.sendDecrypted(message);
            }
            
                 
            }
        }
    }

    //Helps remove client
    public static void removeClient(ClientHandler client) {
        synchronized (clients) {
            //removes client
            clients.remove(client);
            System.out.println(clients.size());//checking length to see if clients are being removed
            //System.out.println(client.getUserId() + " disconnected");
            //Server.broadcastMessage(client.getUserId() + " disconnected", client);
        }
    }
}

// Needed to maintain connection of more than one client
class ClientHandler extends Thread  {
    //attributes of class
    private Socket socket;
    //trying to value to hold function that makes keys
    
    private DataOutputStream out;
    private DataInputStream in;
    
    private String userId;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // Initialize input and output streams
            //this is for reading and sending data
            //out sends data and in reads data
             out= new DataOutputStream(socket.getOutputStream());
            in= new DataInputStream(socket.getInputStream());

            //message to display client has joined the chat to all users
            // this stores the id of the client
            Server.broadcastMessage(" A new user has joined the chat!", this);
            

            //reading message from client
            String input;
            while ((input = in.readUTF()) != null) {
                //breaks if client sends Over
                if(input.equalsIgnoreCase("Over")) {
                    break;
                }
            
                /*read data input and convert to bytes and pass it into this method*/
                //Server.broadcastMessage(input, this);
                Server.broadcastDecryptedMessage(input, this);
                
                /*trying to show decrypted message 
                by calling sendDecrypt in the broadcast method*/
                
            }
        } catch (IOException e) {
            System.err.println("Error with client" + this + ": " + e.getMessage());
            //removes client connection
        } 
        
       //closes sockect connection and stops it from sending and reading data 
        finally {
            try {
                
                Server.removeClient(this);
                if (socket != null) socket.close();
                if (in != null) in.close();
                if (out != null) out.close();
            
            } catch (IOException e) {
                System.err.println("Error closing client connection: " + e.getMessage());
            }
        }
        
    }
    // Method to send a message to this client
    //this one displays on the gui
    public void sendMessage(String message) {
        try {
            //out.writeUTF(message); // Send the message to the clients
            out.writeUTF(message);
            out.flush();

        } catch (IOException e) {
            System.err.println("Error sending message to " + userId + ": " + e.getMessage());
        }
    }

   

    

//The method broadcastDecryptedMessage(byte[], ClientHandler) in the type Server is not applicable for the arguments (String, ClientHandler)
    //making method to display decrypted message
    //change this to byte
    public void sendDecrypted(String message){
        try{
            //calling private key function to store its value
    PrivateKey key= keyManagement.getPrivateKey("privateKey.txt");
            //decode data before decrypting it
            byte [] decodedData= Base64.getDecoder().decode(message.getBytes());
        /*intialise cipher before i can use it */
        //choose algorithm
            Cipher cipherThis= Cipher.getInstance("RSA");
            //decrypting decoded data
            cipherThis.init(Cipher.DECRYPT_MODE,key);

        //convert decrypted byte to string so it can be sent out from writeUTF
            String oringalData= new String(cipherThis.doFinal(decodedData));
            out.writeUTF(oringalData);
            //out.flush();

            

//need these exceptions for decryption
        } catch(Exception|IOError e){
            System.err.println("Error sending message4: " + e.getMessage());
            
        }
    }
}