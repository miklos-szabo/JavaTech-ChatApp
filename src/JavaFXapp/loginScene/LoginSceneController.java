package JavaFXapp.loginScene;

import JavaFXapp.ChatApp;
import JavaFXapp.EnumScenes;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Bejelentkezési képernyő kontrollere
 */
public class LoginSceneController
{
    private static LoginSceneController instance;   //Tudjunk máshonnan is állítani elemeket

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

    /**
     * Ha rákattintottunk a submit gombra, ellenőrizzük, hogy nem akarnak-e SQL injectiont végrehajtani ellenünk,
     * majd továbbítjuk az adatokat a szervernek ellenőrzésre
     */
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

    /**
     * A válasz számára fenntartott {@link Label}t alaphelyzetbe állítja
     */
    @FXML
    private void resetResponseLabel()
    {
        responseLabel.setVisible(false);
    }

    /**
     * Ha rákattintottunk a register gombra, áttérünk a Regisztrálós Scene-re
     */
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
     * A hibaüzeneteknek fenntartott {@link Label} szövegét lehet vele állítani
     * @param inputString Erre állítjuk a {@link Label}-t
     */
    public void writeResponseLabel(String inputString)
    {
        responseLabel.setText(inputString);
        responseLabel.setVisible(true);
    }

    /**
     * Visszaadja az osztályunk jelenleg is futó példányát
     * @return Az osztály példánya
     */
    public static LoginSceneController getInstance()
    {
        return instance;
    }
}
