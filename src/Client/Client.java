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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * A kapcsolatot valósítja meg a szerveroldallal.
     * Csatlakozunk a szerverhez, kiolvassuk az adott kulcsot a titkosításhoz, ami alapján inicializáljuk azt,
     * majd figyeljük a szerver által küldött üzeneteket.
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
            catch (SocketException e)   //Ha a szerver leáll, miközben a kliens működik, kezdőképernyő, hibaüzenet
            {
                System.out.println("Server died!");
                Platform.runLater(() ->
                {
                    try
                    {
                        ChatApp.setNewScene(EnumScenes.LOGINSCENE);
                    }
                    catch (Exception e1)
                    {
                        e1.printStackTrace();
                    }
                    LoginSceneController.getInstance().writeResponseLabel("Server died! Restart the app!");
                });
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
     * Elküldi a szervernek a paraméterként kapott üzenetet.
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
                    Platform.runLater(() ->
                            LoginSceneController.getInstance().writeResponseLabel(Cryptography.decryptToString(message.getText())));
                     break;
                case CHATSCENE: //Ha időközben kijelentkezett a másik fél, servertől üzenetet kapunk az üzenetekbe, server küldővel
                    Platform.runLater(() ->
                            {
                                allMessages.get(ChatSceneController.getInstance().getOtherUser()).add(new MessageTimeStamp(message));
                                ChatSceneController.getInstance().displayMessagesFromMap(allMessages);  //Frissítjük a listát
                            });
            }
        }
        else if(message.getType() == MessageType.TEXT)
        {
            putMessage(new MessageTimeStamp(message), ChatSceneController.getInstance().getOtherUser());
            //Ha az van kiválasztva, akitől az üzenetet kaptuk, akkor frissítjük a kirajzolást.
            //Ha nem az van kiválasztva, majd akkor frissül, ha kiválasztjuk az embert.
            if(ChatSceneController.getInstance().getOtherUser().equals(message.getSender())
                || message.getSender().equals(message.getReceiver()))   //Vagy ha saját üzenetünket kaptuk vissza a szervertől
                ChatSceneController.getInstance().displayMessagesFromMap(allMessages);
        }
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
        if(message.getSender().equals(message.getReceiver()))   //Tehát ha a saját üzenetünket küldi megunknak a szerver
        {
            //allMessages Map-ben megkeressük a másik emberhez tartozó listát, és beletesszük az üzenetet
            allMessages.get(otherUser).add(message);
        }
        //Ha mástól kaptuk az üzenetet, és még nem volt kiválasztva, tehát még nincs inicializálva az összes üzenet
                //mapjében az ő bejegyzése,
        else if(!allMessages.containsKey(message.getSender()))
        {
            loadHistory(message.getSender());   //Akkor betöltjük a történetet
            //allMessages Map-ben megkeressük a másik emberhez tartozó listát, és beletesszük az üzenetet
            allMessages.get(message.getSender()).add(message);  //És a történet után tesszük az üzenetet
        }
        else allMessages.get(message.getSender()).add(message);  //Alapesetben a feladó bejegyzéséhez hozzátesszük az üzenetet
        //Ha mástól kapunk üzenetet, akkor a sender megegyezik az otherUser-rel
    }

    /**
     * Törli a másik féllel való beszélgetés történetét, frissíti a kijelzést
     * @param otherUser A másik fél
     */
    public void clearHistory(String otherUser)
    {
        allMessages.get(otherUser).clear();
        ChatSceneController.getInstance().displayMessagesFromMap(allMessages);
    }

    /**
     * Elmenti a másik féllel való beszélgetés történetét, azaz beállítja az üzenetek map bejegyzését.
     * A txt fájl a 2 fél nevét viseli, {@link MessageTimeStamp} típusú üzeneteket ment
     * @param otherUser A másik fél
     */
    public void saveHistory(String otherUser)
    {
        try
        {
            ObjectOutputStream fileOS = new ObjectOutputStream(new FileOutputStream("txts/" + username + otherUser + ".txt"));
            fileOS.writeObject(allMessages.get(otherUser));     //Kiírjuk a 2 ember közötti beszélgetést fájlba
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Betölti a másik féllel való beszélgetést fájlból az üzenetetk map-be és visszaadja az összes üzenet map-jét
     * @param otherUser A másik fél
     * @return Az összes üzenet mapje
     */
    public Map<String, List<MessageTimeStamp>> loadHistory(String otherUser)
    {
        if(!allMessages.containsKey(otherUser))     //Ha kapott már az ember üzenetet, akkor be vannak töltve a korábbi üzenetek
        {                                           //Ha még nem kapott, betöltjük a fájlban levőt
            try
            {
                initializeMessageMapForUser(otherUser); //Ne legyen nullptr, üresre állítjuk a mapben az otherUserhez tartozó listát
                ObjectInputStream fileIS = new ObjectInputStream(new FileInputStream("txts/" + username + otherUser + ".txt"));
                try
                {
                    allMessages.get(otherUser).addAll((ArrayList<MessageTimeStamp>) fileIS.readObject());
                    return allMessages;
                }
                catch(ClassCastException e)
                {
                    return allMessages;     //Ha nem sikerül, akkor is ezt adjuk vissza, csak üres lista lesz.
                }

            }
            catch(FileNotFoundException ex)
            {
                return allMessages; //Ha még nem beszéltünk egy emberrel, ez a fájl nem létezik, ilyenkor csak visszatérünk
            }
            catch (IOException | ClassNotFoundException e)
            {
                e.printStackTrace();
                return allMessages;
            }
        }
        return allMessages;
    }
}
