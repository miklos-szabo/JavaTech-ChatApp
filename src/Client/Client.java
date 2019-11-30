package Client;

import Cryptography.Cryptography;
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

/**
 * A kliens oldalt valósítja meg
 */
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


    /**
     * Kontruktor, az adott porttal inicializál.
     * @param port A port
     */
    public Client(int port)
    {
        this.port = port;
    }

    //TODO valószínűleg nem kell majd ez, FX alkalmazásban hívjuk meg a tartalmat, új threadként
    public static void main(String[] args)
    {
        new Client(2600).run();
    }

    /**
     * A kapcsolatot valósítja meg a szerveroldallal.
     * Csatlakozunk a szerverhez, kiolvassuk az adott kulcsot a titkosításhoz, ami alapján inicializáljuk azt,
     * majd figyeljük a szerver által küldött üzeneteket és a bemenetet.
     */
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
                    System.out.println("Server died!"); //TODO nem kéne meghalni az alkalmazásnak
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

    /**
     * Bejelentkező üzenet létrehozása, tehát LOGIN {@link MessageType} típussal.
     * @param text A jelszó, {@link String}-ként
     * @return A létrehozott üzenet
     */
    public Message createLoginMessage(String text)
    {
        //A jelszó a text mezőben kerül továbbításra
        return new Message(MessageType.LOGIN, Cryptography.encryptString(Integer.toString(text.hashCode())), username, "");
    }

    /**
     * Regisztráló üzenet létrehozása, tehát REGISTER {@link MessageType} típussal.
     * @param text A jelszó, {@link String}-ként
     * @return A létrehozott üzenet
     */
    public Message createRegisterMessage(String text)
    {
        return new Message(MessageType.REGISTER, Cryptography.encryptString(Integer.toString(text.hashCode())), username, "");
    }

    /**
     * Szöveges üzenet létrehozása, tehát TEXT {@link MessageType} típussal.
     * @param text Az üzenet, {@link String}-ként
     * @return A létrehozott üzenet
     */
    public Message createTextMessage(String text)
    {
        return new Message(MessageType.TEXT, Cryptography.encryptString(text), username, destination);
    }

    /**
     * Elküldi a szervernek paraméterként kapott üzenetet.
     * @param message A küldendő üzenet
     */
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

    /**
     * Kezeli a szervertől érkező üzeneteket
     * @param message A szervertől érkező üzenet
     */
    public void handleResponse(Message message)
    {
        if(message instanceof UserListMessage)  //Ha a felhasználók listáját kapjuk meg, tároljuk
        {
            users = ((UserListMessage) message).getUsers();
            System.out.println(users);      //TODO nyilván nem ez lesz
            return;
        }
        if(message.getType() == MessageType.OK) isLoggedIn = true; //Ezzel lépünk ki a bejelentkezős while ciklusból
        System.out.println(message.getSender() + ": " + Cryptography.decryptToString(message.getText()));
    }
}
