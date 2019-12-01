package JavaFXapp.registerScene;

import JavaFXapp.ChatApp;
import JavaFXapp.EnumScenes;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * A regisztrálós Scene Kontrollere
 */
public class RegisterSceneController
{
    private static RegisterSceneController instance;    //Tudjuk máshonnan is állítani az elemeket

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    @FXML
    private Button submitButton;

    @FXML
    private Label responseLabel;

    @FXML
    private Button backToLoginButton;

    public RegisterSceneController()
    {
        instance = this;
    }

    @FXML
    private void initialize() {}

    /**
     * Ha rányomtunk a submit button-re, ellenőrizzük a bemeneteket, és továbbítjuk a szerver felé
     */
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

    /**
     * Eredeti helyzetébe állítja a hibaüzeneteknek fenntartott Labelt.
     */
    @FXML
    private void resetResponseLabel()
    {
        responseLabel.setVisible(false);
    }

    /**
     * Megvizsgálja regex-el az adott stringet
     * @param input A vizsgálandó {@link String}
     * @return true, ha az egész {@link String} megfelel a regex-nek
     */
    private boolean checkRegex(String input)
    {
        return input.matches("^[\\w-áéíóőúű][\\w-\\sáéíóőúű]*$");     //1 betű vagy -, utána lehet szóköz is
    }

    /**
     * Visszaadja az osztályunk jelenleg is futó példányát
     * @return Az osztály példánya
     */
    public static RegisterSceneController getInstance()
    {
        return instance;
    }

    /**
     * A hibaüzeneteknek fenntartott {@link Label} szövegét lehet vele állítani
     * @param inputString Erre állítjuk a {@link Label}-t
     */
    public void writeResponseLabel(String inputString)
    {
        responseLabel.setText(inputString);
        responseLabel.setVisible(true);
    }

    /**
     * Ha rányomtunk a back to login gombra, visszatérünk a bejelentkezési képernyőre
     */
    @FXML
    public void backToLogin()
    {
        try
        {
            ChatApp.setNewScene(EnumScenes.LOGINSCENE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
