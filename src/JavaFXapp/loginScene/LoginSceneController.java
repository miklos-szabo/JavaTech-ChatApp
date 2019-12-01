package JavaFXapp.loginScene;

import JavaFXapp.ChatApp;
import JavaFXapp.EnumScenes;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginSceneController
{
    private static LoginSceneController instance;

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

    public LoginSceneController()
    {
        instance = this;
    }

    @FXML
    private void initialize() {}

    @FXML
    private void submitClicked()
    {
        if(!checkRegex(usernameTextField.getText()) || !checkRegex(passwordTextField.getText()))
        {
            responseLabel.setText("Username and password has to match regex: ^[\\w-áéíóőúű][\\w-\\sáéíóőúű]*$ ");
            responseLabel.setVisible(true);
            return;
        }
        ChatApp.sendLoginMessage(usernameTextField.getText(), passwordTextField.getText());
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
            ChatApp.setNewScene(EnumScenes.REGISTERSCENE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean checkRegex(String input)
    {
        return input.matches("^[\\w-áéíóőúű][\\w-\\sáéíóőúű]*$");     //1 betű vagy -, utána lehet szóköz is
    }

    public void writeResponseLabel(String s)
    {
        responseLabel.setText(s);
        responseLabel.setVisible(true);
    }

    public static LoginSceneController getInstance()
    {
        return instance;
    }
}
