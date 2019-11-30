package JavaFXapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChatApp extends Application
{
    private static Stage primaryStage;
    private static Scenes currentScene;

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
}
