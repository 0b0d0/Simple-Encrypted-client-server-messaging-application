import java.io.*;
import java.net.*;
import java.net.http.WebSocket.Listener;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.security.spec.X509EncodedKeySpec;

import java.util.Scanner;
//importing to generate random id
import java.util.UUID;

/*need to make a symetric key with AES and encrypt it with that
 * thwn ecnrypt the key with rsa public key and send encrypted data with aes key
 * then
 */

//importing base 64 to convert bytes to strings
import Base64;
import main.java.keyManagement;

import javax.crypto.BadPaddingException;
//i think it should be the server creating the ids and not the client
//importing libraries for encryption
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.PublicKey;

/*trying to import function from another file */
//import main.java.keyManagement;


public class Client {
    //these values can be used anywhere in the code
    private static JTextArea messageBox;
    private static JTextArea textBox;
    private static DataOutputStream out;
 
    private static DataInputStream in;
    private static String userId;
    private static Socket socket;
    private static KeyPair pairs;
    private static PublicKey serverPublicKey;

    
    

    //generates random id
    static UUID uniqueId= UUID.randomUUID();
    //Hidden message for encryption
    
    
    
    public static void main(String[] args) {

        //randomly generates user id
        //converting uuid to string
        //use split or substring method to reduce large length of user ID
        String userIdentfier=uniqueId.toString().substring(0,5);
        
        //applying it be used as users ID
        userId=userIdentfier;
        
        //generates interface
        createUI();
        //methods call function to connect to server
        connectToServer();
    }

    private static void createUI() {
    // Create the main frame
    JFrame message = new JFrame("NetCluster - Messaging Interface");
    message.setSize(600, 450);
    message.setLocationRelativeTo(null);
    message.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    message.setLayout(new BorderLayout());
    message.getContentPane().setBackground(new Color(30, 50, 80));

    /*trying to change jframe icon */
    ImageIcon img= new ImageIcon("powerSign.jpg");
    message.setIconImage(img.getImage());

    // User panel
    JPanel userPanel = new JPanel();
    userPanel.setBackground(new Color(30, 50, 80));
    userPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    JLabel userLabel = new JLabel("User ID: " + userId);
    userLabel.setForeground(Color.WHITE);
    userPanel.add(userLabel);
    message.add(userPanel, BorderLayout.NORTH);

    // Message box
    messageBox = new JTextArea("Message Box\n");
    messageBox.setEditable(false);
    messageBox.setFont(new Font("Arial", Font.PLAIN, 14));
    messageBox.setBackground(Color.LIGHT_GRAY);
    messageBox.setLineWrap(true);
    messageBox.setWrapStyleWord(true);
    JScrollPane scrollBar = new JScrollPane(messageBox);
    message.add(scrollBar, BorderLayout.CENTER);

    // Text box for typing messages
    textBox = new JTextArea();
    textBox.setFont(new Font("Arial", Font.BOLD, 16)); // Increased font size
    textBox.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Color.GRAY, 2), // Thicker border
        BorderFactory.createEmptyBorder(5, 5, 5, 5) // Padding
    ));
    textBox.setBackground(Color.WHITE); // Lighter background for better visibility
    message.add(textBox, BorderLayout.SOUTH);
    
    // Send button
    JButton send = new JButton("Send");
    send.setFont(new Font("Arial", Font.BOLD, 14));
    send.setBackground(new Color(70, 130, 180));
    send.setForeground(Color.WHITE);
    send.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    send.addActionListener(e -> sendMessage());

    // Panel for the send button
    JPanel buttonPanel = new JPanel();
    buttonPanel.setBackground(new Color(30, 50, 80));
    buttonPanel.add(send);
    message.add(buttonPanel, BorderLayout.EAST);

    // Key listener for the text box
    textBox.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                e.consume(); // Prevent the newline character
                sendMessage();
            }
        }
    });

    // Window listener for closing
    message.addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            closeConnection();
        }
    });

    

    // Ensure components are visible and enabled
    send.setVisible(true);
    textBox.setVisible(true);
    message.setVisible(true);
}

    
    private static void connectToServer() {
        try {
            //socket connects to the chosen ip address
            socket = new Socket("localhost", 5050);  // Use class-level socket
            //this is for reading and sending data
            out= new DataOutputStream(socket.getOutputStream());
        
            in= new DataInputStream(socket.getInputStream());
           
            //This is for threading
            //deals with trying to connect to server
            //Continuously Reads messages from the server

        Thread listener = new Thread(() -> {
                try {
                    String Text;
                    while ((Text = in.readUTF()) != null) {
                        final String received = Text;
                    
                        SwingUtilities.invokeLater(() -> {
                            messageBox.append(received + "\n");
                        });
                    }
                } catch (IOException e) {
                    System.err.println("Connection closed: " + e.getMessage());
                }
            });
            listener.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to server", "Error", JOptionPane.ERROR_MESSAGE);
        }

    }
    
   
    //making digital signature to verify plaintext data
    private static byte [] makeDigitalSig(byte[] input) throws Exception{
    
        //CHOOSING HASHING ALGORITHM
        Signature sign= Signature.getInstance("MD5withRSA");
        //sign key with private key
        PrivateKey signKey= keyManagement.getPrivateKey("privateKey.txt");
        //passing function of public key to be signed
        //has be done with privaqte key so i will pass the file as argument
        sign.initSign(signKey);
        sign.update(input);
        //returning signature
        return sign.sign();
    }

    //verify signature
    public static boolean verify(byte [] input) throws Exception{
        byte [] signatureToVerify=makeDigitalSig(input);
        //making public key variable
        PublicKey publicKey=keyManagement.getPublicKey("publicKey.txt");
        //choosing algorithm
        Signature signature= Signature.getInstance("MD5withRSA");
        //verifiying public key 
        signature.initVerify(publicKey);
        //updating input bytes
        signature.update(input);
        //returning signature to verify
        return signature.verify(signatureToVerify);
    }
     
    private static void sendMessage() {
        try {
            //Generate a RSA key pair
            PublicKey publicK= keyManagement.getPublicKey("publicKey.txt");
            //creating cipher for encryption 
            Cipher cipher = Cipher.getInstance("RSA");            
            //choosing to hide data with public key
            //cipher.init(Cipher.ENCRYPT_MODE, pairs.getPublic());
            cipher.init(Cipher.ENCRYPT_MODE, publicK);

            String message=textBox.getText().trim();
            String messageWithUserId= userId+": "+message;
            
            if (!message.isEmpty()) {
                //write utf deals with strings
                //store encrypted data as bytes then convert to string
                //DISPLAYS MESSSAGE TO SERVER END

                byte [] encryptedData=cipher.doFinal(messageWithUserId.getBytes());
                String encodedData= java.util.Base64.getEncoder().encodeToString(encryptedData);

                 //adding boolean statement to see if signature is true to send data to server
                 byte [] messageInBytes=message.getBytes();
                boolean verifySignature= verify(messageInBytes);
                /*signature verifies the integrity of the data being sent */
                if(verifySignature==true){
                    //out.writeUTF(messageWithUserId);
                    out.writeUTF(encodedData);
                    //converts textbox string to bytes
                    
                    messageBox.append(messageWithUserId+"\n");//testing something
                    out.flush();
                
                    textBox.setText("");
                }
            }


            
            // | is or for boolean
            //I had more exceptions but some of them were already caught in other areas of code so I deleted them
        } catch (Exception|IOError e) {
            System.err.println("Error sending message: " + e.getMessage());
        }

    }
    //return user id
    public String getUserId() {
        return userId;
    }

    //closes connection

    private static void closeConnection() {
        try {
            if (out != null) {
                out.write("Over".getBytes());
                out.flush();
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
            //if there was an error this gets diplayed
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
