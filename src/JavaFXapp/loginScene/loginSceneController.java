package JavaFXapp.loginScene;

import JavaFXapp.ChatApp;
import JavaFXapp.Scenes;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class loginSceneController
{
    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button submitButton;

    @FXML
    private Button registerButton;

    @FXML
    private Label responseLabel;

    public loginSceneController(){}

    @FXML
    private void initialize() {}

    @FXML
    private void submitClicked()
    {
        if(!checkRegex(usernameTextField.getText()) || !checkRegex(passwordTextField.getText()))
        {
            responseLabel.setText("Username and password has to match regex: ^[\\w-][\\w-\\s]*$ ");
            responseLabel.setVisible(true);
            return;
        }
    }

    @FXML
    private void resetResponseLabel()
    {
        responseLabel.setVisible(false);
    }

    @FXML
    private void registerClicked()
    {
        try
        {
            ChatApp.setNewScene(Scenes.REGISTERSCENE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkRegex(String input)
    {
        return input.matches("^[\\w-][\\w-\\s]*$");     //1 betű vagy -, utána lehet szóköz is
    }
}
