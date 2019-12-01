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

public class ChatApp extends Application
{
    private static Stage primaryStage;
    private static EnumScenes currentScene;
    private final String propertiesPath = "properties.txt";

    private static Client client;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        AppProperties.load(propertiesPath);
        ChatApp.primaryStage = primaryStage;   //Elmentjük a primary stage-et, hogy máshonnan tudjunk scene-t váltani
        primaryStage.setWidth(AppProperties.getDoubleProperty("width", 600));
        primaryStage.setHeight(AppProperties.getDoubleProperty("height", 400));
        primaryStage.setTitle("Chat App");
        setNewScene(EnumScenes.LOGINSCENE);
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
            if(ChatSceneController.getInstance().isChatBoxVisible())    //Külön kell venni, ha chat előt lépünk ki, exception
                saveHistory(ChatSceneController.getInstance().getOtherUser());

        AppProperties.setProperty("width", primaryStage.getWidth());
        AppProperties.setProperty("height", primaryStage.getHeight());
        AppProperties.store(propertiesPath);
    }

    public static void setNewScene(EnumScenes newScene) throws Exception
    {
        if(newScene.equals(currentScene)) return;
        switch(newScene)
        {
            case LOGINSCENE:
            {
                Parent root = FXMLLoader.load(ChatApp.class.getResource("loginScene/loginScene.fxml"));
                primaryStage.setScene(new Scene(root));
                currentScene = EnumScenes.LOGINSCENE;
                break;
            }
            case REGISTERSCENE:
            {
                Parent root = FXMLLoader.load(ChatApp.class.getResource("registerScene/registerScene.fxml"));
                primaryStage.setScene(new Scene(root));
                currentScene = EnumScenes.REGISTERSCENE;
                break;
            }
            case CHATSCENE:
            {
                Parent root = FXMLLoader.load(ChatApp.class.getResource("ChatScene/ChatScene.fxml"));
                primaryStage.setScene(new Scene(root));
                currentScene = EnumScenes.CHATSCENE;
                break;
            }
        }
    }

    public static void sendLoginMessage(String username, String password)
    {
        client.sendMessage(client.createLoginMessage(username, password));
    }

    public static void sendRegisterMessage(String username, String password)
    {
        client.sendMessage(client.createRegisterMessage(username, password));
    }

    public static EnumScenes getCurrentScene()
    {
        return currentScene;
    }

    public static void clearHistory(String otherUser)
    {
        client.clearHistory(otherUser);
    }

    public static void saveHistory(String otherUser)
    {
        client.saveHistory(otherUser);
    }

    public static Map<String, List<MessageTimeStamp>> loadHistory(String otherUser)
    {
        return client.loadHistory(otherUser);
    }

    public static void sendTextMessage(String text)
    {
        client.sendMessage(client.createTextMessage(text));
    }

    //TODO temporary, loadHistoryban fog megtörténni
    public static void TEMPintializeMapForUser(String otherUser)
    {
        client.initializeMessageMapForUser(otherUser);
    }
}
