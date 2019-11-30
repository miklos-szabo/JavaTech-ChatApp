package JavaFXapp.registerScene;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterSceneController
{
    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button submitButton;

    @FXML
    private Label responseLabel;

    public RegisterSceneController(){}

    @FXML
    private void initialize() {}

    @FXML
    private void submitClicked()
    {
        if(!checkRegex(usernameTextField.getText()) || usernameTextField.getText().equals("server"))
        {
            responseLabel.setText("Username has to match regex: ^[\\w-][\\w-\\s]*$ and can't be \"server\"");
            responseLabel.setVisible(true);
            return;
        }
        if(!checkRegex(passwordTextField.getText()))
        {
            responseLabel.setText("Password has to match regex: ^[\\w-][\\w-\\s]*$");
            responseLabel.setVisible(true);
            return;
        }
    }

    @FXML
    private void resetResponseLabel()
    {
        responseLabel.setVisible(false);
    }

    private boolean checkRegex(String input)
    {
        return input.matches("^[\\w-][\\w-\\s]*$");     //1 betű vagy -, utána lehet szóköz is
    }
}
