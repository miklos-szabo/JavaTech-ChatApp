package Client;

import Cryptography.Cryptography;
import Message.Message;
import Message.MessageTimeStamp;
import Message.MessageType;
import Message.UserListMessage;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyPair;
import java.util.*;

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
    private Map<String, List<MessageTimeStamp>> allMessages;

    private String destination;
    private boolean isLoggedIn = false;     //Ha sikerült bejelentkezni, ezzel lépünk ki a while ciklusból


    /**
     * Konstruktor, az adott porttal inicializál, üzenetek {@link HashMap}-ét is inicializáljuk
     * @param port A port
     */
    public Client(int port)
    {
        this.port = port;
        allMessages = new HashMap<>();
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

    /**
     * Létrehozzuk az üzenetek {@link HashMap}-jében a másik félhez tartozó bejegyzést, ha nem létezik még
     * @param otherUser A másik fél
     */
    public void initializeMessageMapForUser(String otherUser)
    {
        if(!allMessages.containsKey(otherUser))     //Ha nincs még a mapben a beszélgetés címzettje, akkor beletesszük
            allMessages.put(otherUser, new ArrayList<>());
    }

    /**
     * Hozzáadjuk a tárolt üzenetekhez az üzenetet
     * @param message A tárolandó üzenet
     * @param otherUser A másik fél, akivel beszélünk
     */
    public void putMessage(MessageTimeStamp message, String otherUser)
    {
        //allMessages Map-ben megkeressük a másik emberhez tartozó listát, és beletesszük az üzenetet
        allMessages.get(otherUser).add(message);
    }

    /**
     * Törli a másik féllel való beszélgetés történetét
     * @param otherUser A másik fél
     */
    public void clearHistory(String otherUser)
    {
        allMessages.get(otherUser).clear();
    }

    /**
     * Elmenti a másik féllel való beszélgetés történetét.
     * A txt fájl a 2 fél nevét viseli
     * @param otherUser A másik fél
     */
    public void saveHistory(String otherUser)
    {
        try
        {
            ObjectOutputStream fileOS = new ObjectOutputStream(new FileOutputStream(username + otherUser + ".txt"));
            fileOS.writeObject(allMessages.get(otherUser));     //Kiírjuk a 2 ember közötti beszélgetést fájlba
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Betölti a másik féllel való beszélgetést fájlból
     * @param otherUser A másik fél
     * @return Sikeres volt-e? (Fájl meghibásodhatott, ill. üres lehetett)
     */
    public boolean loadHistory(String otherUser)
    {
        try
        {
            ObjectInputStream fileIS = new ObjectInputStream(new FileInputStream(username + otherUser + ".txt"));
            try
            {
                allMessages.get(otherUser).addAll((ArrayList<MessageTimeStamp>) fileIS.readObject());
                return true;
            }
            catch(ClassCastException e)
            {
                return false;
            }

        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
