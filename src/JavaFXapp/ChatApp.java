package JavaFXapp;

import Client.Client;
import JavaFXapp.ChatScene.ChatSceneController;
import JavaFXapp.Properties.AppProperties;
import Message.MessageTimeStamp;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

/**
 * A JavaFX alkalmazás fő osztálya
 */
public class ChatApp extends Application
{
    private static Stage primaryStage;
    private static EnumScenes currentScene;
    private final String propertiesPath = "properties.txt";
    private static final String CSSPath = "JavaFXapp/style.css";

    private static Client client;

    public static void main(String[] args)
    {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception
    {
        AppProperties.load(propertiesPath);     //Beolvassuk a propertyket
        ChatApp.primaryStage = primaryStage;   //Elmentjük a primary stage-et, hogy máshonnan tudjunk scene-t váltani
        primaryStage.setWidth(AppProperties.getDoubleProperty("width", 600));
        primaryStage.setHeight(AppProperties.getDoubleProperty("height", 400));
        primaryStage.setTitle("Chat App");
        setNewScene(EnumScenes.LOGINSCENE);     //Login képernyővel kezdünk
        primaryStage.show();

        Thread clientThread = new Thread(client);
        clientThread.setDaemon(true);
        clientThread.start();

    }

    @Override
    public void init()
    {
        client = new Client(2600);
    }

    @Override
    public void stop() throws Exception
    {
        if(currentScene == EnumScenes.CHATSCENE)
            if(ChatSceneController.getInstance().isChatBoxVisible())    //Külön kell venni, ha chat előtt lépünk ki, exception
                saveHistory(ChatSceneController.getInstance().getOtherUser());  //Elmentjük a történetet kiépéskor

        AppProperties.setProperty("width", primaryStage.getWidth());
        AppProperties.setProperty("height", primaryStage.getHeight());
        AppProperties.store(propertiesPath);    //Elmentjük a propertyket kilépéskor
    }

    /**
     * Beállít egy új {@link Scene}-t
     * @param newScene Az új {@link Scene}, {@link EnumScenes} formában
     * @throws Exception
     */
    public static void setNewScene(EnumScenes newScene) throws Exception
    {
        if(newScene.equals(currentScene)) return;   //Ha ugyanarra tennénk, semmi nem történik
        switch(newScene)
        {
            case LOGINSCENE:
            {
                Parent root = FXMLLoader.load(ChatApp.class.getResource("loginScene/loginScene.fxml"));
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                scene.getStylesheets().add(CSSPath);
                currentScene = EnumScenes.LOGINSCENE;
                break;
            }
            case REGISTERSCENE:
            {
                Parent root = FXMLLoader.load(ChatApp.class.getResource("registerScene/registerScene.fxml"));
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                scene.getStylesheets().add(CSSPath);
                currentScene = EnumScenes.REGISTERSCENE;
                break;
            }
            case CHATSCENE:
            {
                Parent root = FXMLLoader.load(ChatApp.class.getResource("ChatScene/ChatScene.fxml"));
                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                scene.getStylesheets().add(CSSPath);
                currentScene = EnumScenes.CHATSCENE;
                break;
            }
        }
    }

    /**
     * Lekérdezi, hogy jelenleg melyik {@link Scene} van használatban
     * @return a jelenleg használt {@link Scene}, {@link EnumScenes} formában
     */
    public static EnumScenes getCurrentScene()
    {
        return currentScene;
    }

    /**
     * Visszaadja az alkalmazás {@link Stage}-ét
     * @return A primary stage
     */
    public static Stage getPrimaryStage()
    {
        return primaryStage;
    }

    /**
     * Továbbküldi a bejelentkező üzenetet a kliens thread felé.
     * Fő célja a scene controller és a kliens thread közötti kapcsolat megvalósítása.
     * (Kliens a ChatAppból látszik, viszont a kontrollerből küldenénk az üzenetet)
     * @param username Felhasználónév
     * @param password Jelszó, {@link String}-ként
     */
    public static void sendLoginMessage(String username, String password)
    {
        client.sendMessage(client.createLoginMessage(username, password));
    }

    /**
     * Továbbküldi a regisztrációs üzenetet a kliens thread felé.
     * Fő célja a scene controller és a kliens thread közötti kapcsolat megvalósítása.
     * (Kliens a ChatAppból látszik, viszont a kontrollerből küldenénk az üzenetet)
     * @param username Felhasználónév
     * @param password Jelszó, {@link String}-ként
     */
    public static void sendRegisterMessage(String username, String password)
    {
        client.sendMessage(client.createRegisterMessage(username, password));
    }

    /**
     * Továbbküldi a Chat történet törlési kérelmet a kliens thread felé.
     * Fő célja a scene controller és a kliens thread közötti kapcsolat megvalósítása.
     * (Kliens a ChatAppból látszik, viszont a kontrollerből küldenénk az üzenetet)
     * @param otherUser A másik felhasználó, a vele történt beszélgetést törölnénk
     */
    public static void clearHistory(String otherUser)
    {
        client.clearHistory(otherUser);
    }

    /**
     * Továbbküldi a Chat történet mentési kérelmet a kliens thread felé.
     * Fő célja a scene controller és a kliens thread közötti kapcsolat megvalósítása.
     * (Kliens a ChatAppból látszik, viszont a kontrollerből küldenénk az üzenetet)
     * @param otherUser A másik felhasználó, a vele történt beszélgetést mentenénk
     */
    public static void saveHistory(String otherUser)
    {
        client.saveHistory(otherUser);
    }

    /**
     * Továbbküldi a Chat történet betöltési kérelmet a kliens thread felé.
     * Fő célja a scene controller és a kliens thread közötti kapcsolat megvalósítása.
     * (Kliens a ChatAppból látszik, viszont a kontrollerből küldenénk az üzenetet)
     * @param otherUser A másik felhasználó, a vele történt beszélgetést töltenénk be
     * @return A kliens összes beszélgetésének {@link java.util.HashMap}-je
     */
    public static Map<String, List<MessageTimeStamp>> loadHistory(String otherUser)
    {
        return client.loadHistory(otherUser);
    }

    /**
     * Továbbküldi a küldendő üzenetet a kliens thread felé.
     * Fő célja a scene controller és a kliens thread közötti kapcsolat megvalósítása.
     * (Kliens a ChatAppból látszik, viszont a kontrollerből küldenénk az üzenetet)
     * @param text Az üzenet, {@link String}-ként
     */
    public static void sendTextMessage(String text)
    {
        client.sendMessage(client.createTextMessage(text));
    }
}
