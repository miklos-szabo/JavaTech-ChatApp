import Message.Message;
import Message.MessageType;
import Message.UserListMessage;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyPair;
import java.util.List;
import java.util.Scanner;

public class Client implements Runnable
{
    private String username;
    private int port;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket socket = new Socket();
    private KeyPair encriptionKey;
    private List<String> users;

    private String text;
    private String destination;
    private boolean isLoggedIn = false;     //Ha sikerült bejelentkezni, ezzel lépünk ki a while ciklusból



    public Client(int port)
    {
        this.port = port;
    }

    public static void main(String[] args)
    {
        new Client(2600).run();
    }

    @Override
    public void run()
    {
        try
        {
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), port));
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());

            encriptionKey = (KeyPair)inputStream.readObject();      //Beolvassuk a titkosítás kulcsát
            Cryptography.initClient(encriptionKey);

            //Szerver oldalról üzenetet fogad és feldolgoz
            Runnable serverComm = () ->
            {
                try
                {
                    while(true)
                    {
                        Message serverMessage = (Message) inputStream.readObject();
                        handleResponse(serverMessage);
                    }
                }
                catch (SocketException e)
                {
                    System.out.println("Server died!");
                }
                catch (ClassNotFoundException | IOException e)
                {
                    e.printStackTrace();
                }
            };

            new Thread(serverComm).start();

            //Kliens oldalról beolvas és küld a szervernek
            Runnable clientComm = () ->
            {
                try(Scanner scanner = new Scanner(new InputStreamReader(System.in)))
                {
                    while(!isLoggedIn)
                    {
                        System.out.println("Log in!");
                        username = scanner.nextLine();
                        sendMessage(createLoginMessage(scanner.nextLine()));
                    }

                    while(true)
                    {
                        System.out.println("Send text!");
                        destination = scanner.nextLine();
                        sendMessage(createTextMessage(scanner.nextLine()));
                    }
                }
            };
            new Thread(clientComm).start();

        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public Message createLoginMessage(String text)
    {
        //A jelszó a text mezőben kerül továbbításra
        return new Message(MessageType.LOGIN, Cryptography.encryptString(Integer.toString(text.hashCode())), username, "");
    }

    public Message createRegisterMessage(String text)
    {
        return new Message(MessageType.REGISTER, Cryptography.encryptString(Integer.toString(text.hashCode())), username, "");
    }

    public Message createTextMessage(String text)
    {
        return new Message(MessageType.TEXT, Cryptography.encryptString(text), username, destination);
    }

    public void sendMessage(Message message)
    {
        //TODO saját thread?
        try
        {
            outputStream.writeObject(message);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void handleResponse(Message message)
    {
        if(message instanceof UserListMessage)
        {
            users = ((UserListMessage) message).getUsers();
            System.out.println(users);
            return;
        }
        if(message.getType() == MessageType.OK) isLoggedIn = true;
        System.out.println(message.getSender() + ": " + Cryptography.decryptToString(message.getText()));
    }
}
