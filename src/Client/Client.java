package Client;

import Cryptography.Cryptography;
import JavaFXapp.ChatApp;
import JavaFXapp.ChatScene.ChatSceneController;
import JavaFXapp.EnumScenes;
import JavaFXapp.loginScene.LoginSceneController;
import JavaFXapp.registerScene.RegisterSceneController;
import Message.Message;
import Message.MessageTimeStamp;
import Message.MessageType;
import Message.UserListMessage;
import javafx.application.Platform;

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
        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Bejelentkező üzenet létrehozása, tehát LOGIN {@link MessageType} típussal.
     * @param username A felhasználónév
     * @param password A jelszó {@link String}-ként
     * @return A létrehozott üzenet
     */
    public Message createLoginMessage(String username, String password)
    {
        this.username = username; //Beállítjuk itt a kliens felhasználónevét, ha nem sikerül a bejelentkezés,
                                //újrapróbálkozásnál megint állítódik.
        //A jelszó a text mezőben kerül továbbításra
        return new Message(MessageType.LOGIN, Cryptography.encryptString(Integer.toString(password.hashCode())), username, "");
    }

    /**
     * Regisztráló üzenet létrehozása, tehát REGISTER {@link MessageType} típussal.
     * @param username A felhasználónév
     * @param password A jelszó, {@link String}-ként
     * @return A létrehozott üzenet
     */
    public Message createRegisterMessage(String username, String password)
    {
        return new Message(MessageType.REGISTER, Cryptography.encryptString(Integer.toString(password.hashCode())), username, "");
    }

    /**
     * Szöveges üzenet létrehozása, tehát TEXT {@link MessageType} típussal.
     * @param text Az üzenet, {@link String}-ként
     * @return A létrehozott üzenet
     */
    public Message createTextMessage(String text)
    {
        return new Message(MessageType.TEXT, Cryptography.encryptString(text), username, ChatSceneController.getInstance().getOtherUser());
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
            Platform.runLater(() ->
                    ChatSceneController.getInstance().setUsersLoggedIn(users));
            //TODO ha a kiválasztott ember nincs ott az új listában, kiír valamit a szerver
            return;
        }
        if(message.getType() == MessageType.OKREGISTER)
        {
            Platform.runLater(() ->
            {
                try
                {
                    ChatApp.setNewScene(EnumScenes.LOGINSCENE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
        else if(message.getType() == MessageType.OKLOGIN)
        {
            Platform.runLater(() ->
            {
                try
                {
                    ChatApp.setNewScene(EnumScenes.CHATSCENE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            });
        }
        else if(message.getType() == MessageType.ERROR)
        {
            switch (ChatApp.getCurrentScene())
            {
                case REGISTERSCENE:
                    //Itt panaszkodik, hogy nem alap threaden vagyunk, kell runlater
                    Platform.runLater(() ->
                            RegisterSceneController.getInstance().writeResponseLabel(Cryptography.decryptToString(message.getText())));
                    break;
                case LOGINSCENE:
                    //Itt nem panaszkodik, pedig szó szerint ugyanaz a kód...
                    LoginSceneController.getInstance().writeResponseLabel(Cryptography.decryptToString(message.getText())); break;
                case CHATSCENE: //TODO
            }
        }
        else if(message.getType() == MessageType.TEXT)
        {
            putMessage(new MessageTimeStamp(message), ChatSceneController.getInstance().getOtherUser());
            ChatSceneController.getInstance().displayMessagesFromMap(allMessages);
        }
        //System.out.println(message.getSender() + ": " + Cryptography.decryptToString(message.getText()));
    }

    /**
     * Ha nem létezik még, létrehozzuk az üzenetek {@link HashMap}-jében a másik félhez tartozó bejegyzést,
     * egyébként kiürítjük azt.
     * @param otherUser A másik fél
     */
    public void initializeMessageMapForUser(String otherUser)
    {
        if(!allMessages.containsKey(otherUser))     //Ha nincs még a mapben a beszélgetés címzettje, akkor beletesszük
            allMessages.put(otherUser, new ArrayList<>());
        else allMessages.get(otherUser).clear();    //Egyébként pedig kiürítjük
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
     * A txt fájl a 2 fél nevét viseli, {@link MessageTimeStamp} típusú üzeneteket ment
     * @param otherUser A másik fél
     */
    public void saveHistory(String otherUser)
    {
        try
        {
            File file = new File("/txts/" + username + otherUser + ".txt");
            file.createNewFile();
            ObjectOutputStream fileOS = new ObjectOutputStream(new FileOutputStream(file));
            fileOS.writeObject(allMessages.get(otherUser));     //Kiírjuk a 2 ember közötti beszélgetést fájlba
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Betölti a másik féllel való beszélgetést fájlból és visszaadja az összes üzenet map-jét
     * @param otherUser A másik fél
     * @return Az összes üzenet mapje, ami vagy megváltozott, vagy nem
     */
    public Map<String, List<MessageTimeStamp>> loadHistory(String otherUser)
    {
        try
        {
            File file = new File("..\\..\\txts\\Bélaadmin.txt");
            //File file = new File("/txts/" + username + otherUser + ".txt");
            file.createNewFile();
            ObjectInputStream fileIS = new ObjectInputStream(new FileInputStream(file));
            try
            {
                initializeMessageMapForUser(otherUser);
                allMessages.get(otherUser).addAll((ArrayList<MessageTimeStamp>) fileIS.readObject());
                return allMessages;
            }
            catch(ClassCastException e)
            {
                return allMessages;     //Ha nem sikerül, akkor is ezt adjuk vissza, csak üres lista lesz.
            }

        }
        catch (IOException | ClassNotFoundException e)
        {
            e.printStackTrace();
            return allMessages;
        }
    }
}
