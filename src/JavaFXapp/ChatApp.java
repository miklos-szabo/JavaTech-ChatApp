package JavaFXapp;

import Client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatApp extends Application
{
    private static Stage primaryStage;
    private static Scenes currentScene;

    private static Client client;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        ChatApp.primaryStage = primaryStage;   //Elmentjük a primary stage-et, hogy máshonnan tudjunk scene-t váltani
        primaryStage.setTitle("Chat App");
        setNewScene(Scenes.LOGINSCENE);
        primaryStage.show();

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        executorService.execute(client);
//        Platform.runLater(client);
    }

    @Override
    public void init() throws Exception
    {
        client = new Client(2600);
    }

    public static void setNewScene(Scenes newScene) throws Exception
    {
        if(newScene.equals(currentScene)) return;
        switch(newScene)
        {
            case LOGINSCENE:
            {
                Parent root = FXMLLoader.load(ChatApp.class.getResource("loginScene/loginScene.fxml"));
                primaryStage.setScene(new Scene(root));
                currentScene = Scenes.LOGINSCENE;
                break;
            }
            case REGISTERSCENE:
            {
                Parent root = FXMLLoader.load(ChatApp.class.getResource("registerScene/registerScene.fxml"));
                primaryStage.setScene(new Scene(root));
                currentScene = Scenes.REGISTERSCENE;
                break;
            }
            case CHATSCENE:
            {
                Parent root = FXMLLoader.load(ChatApp.class.getResource("ChatScene/ChatScene.fxml"));
                primaryStage.setScene(new Scene(root));
                currentScene = Scenes.CHATSCENE;
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
}
