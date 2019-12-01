package JavaFXapp.registerScene;

import JavaFXapp.ChatApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterSceneController
{
    private static RegisterSceneController instance;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button submitButton;

    @FXML
    private Label responseLabel;

    public RegisterSceneController()
    {
        instance = this;
    }

    @FXML
    private void initialize() {}

    @FXML
    private void submitClicked()
    {
        if(!checkRegex(usernameTextField.getText()) || usernameTextField.getText().equals("server"))
        {
            responseLabel.setText("Username has to match regex: ^[\\w-áéíóőúű][\\w-\\sáéíóőúű]*$ and can't be \"server\"");
            responseLabel.setVisible(true);
            return;
        }
        if(!checkRegex(passwordTextField.getText()))
        {
            responseLabel.setText("Password has to match regex: ^[\\w-áéíóőúű][\\w-\\sáéíóőúű]*$");
            responseLabel.setVisible(true);
            return;
        }
        ChatApp.sendRegisterMessage(usernameTextField.getText(), passwordTextField.getText());
    }

    @FXML
    private void resetResponseLabel()
    {
        responseLabel.setVisible(false);
    }

    private boolean checkRegex(String input)
    {
        return input.matches("^[\\w-áéíóőúű][\\w-\\sáéíóőúű]*$");     //1 betű vagy -, utána lehet szóköz is
    }

    public static RegisterSceneController getInstance()
    {
        return instance;
    }

    public void writeResponseLabel(String s)
    {
        responseLabel.setText(s);
        responseLabel.setVisible(true);
    }
}
