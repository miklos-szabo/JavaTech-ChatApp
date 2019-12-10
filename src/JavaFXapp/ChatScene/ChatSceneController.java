package JavaFXapp.ChatScene;

import JavaFXapp.ChatApp;
import Message.MessageTimeStamp;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A chat Scene kontrollere
 */
public class ChatSceneController
{
    private static ChatSceneController instance;    //Tudjunk máshonnan is állítani elemeket

    @FXML
    private ListView<String> usersLoggedIn;

    @FXML
    private ListView<MessageTimeStamp> chat;

    @FXML
    private TextArea inputField;

    @FXML
    private Label otherUser;

    @FXML
    private VBox chatBox;

    @FXML
    private Button clearHistoryButton;

    @FXML
    private Button fileButton;

    public ChatSceneController()
    {
        instance = this;
    }

    @FXML
    private void initialize() {}

    /**
     * Visszaadja az osztályunk jelenleg is futó példányát
     * @return Az osztály példánya
     */
    public static ChatSceneController getInstance()
    {
        return instance;
    }

    /**
     * Beállítja a jelenleg bejelentkezve levő felhasználók listáját a kapott lista elemeire
     * @param userList Ennek a listának az elemeire szeretnénk beállítani a {@link ListView}-nkat
     */
    public void setUsersLoggedIn(List<String> userList)
    {
        //Először observable listába kell tanni az elemeket, ezt lehet listviewnak átadni
        ObservableList<String> observableUsers = FXCollections.observableArrayList(userList);
        usersLoggedIn.setItems(observableUsers);
    }

    /**
     * Amikor rákattintunk egy emeberre a bejelentkezett felhasználók közül,
     * ha valami már ki volt választva, akkor ott elmentjük a történetet,
     * ezután a felső, beszédpartnert jelző labelt állítjuk,
     * majd betöltjük a vele levő history-t.
     */
    @FXML
    public void displayChatBoxWithUser()
    {
        if(chatBox.isVisible())     //Ha volt már valaki kiválasztva, akkor elmentjük a vele való beszélgetést
        {
            ChatApp.saveHistory(otherUser.getText());
        }
        otherUser.setText(usersLoggedIn.getSelectionModel().getSelectedItem()); //Felső labelt az új emberre állítjuk
        displayMessagesFromMap(ChatApp.loadHistory(otherUser.getText()));   //A loadhistory mindenképp az összes üzenet
                                                        //Mapjével tér vissza de a másik félhez tartozó lista lehet üres.
        chatBox.setVisible(true);
    }

    /**
     * Kiírjuk a paraméter {@link Map}-ben levő üzeneteket, amik a másik félhez tartoznak
     * @param allMessages Az összes üzenet {@link Map}-je
     */
    public void displayMessagesFromMap(Map<String, List<MessageTimeStamp>> allMessages)
    {
        //Kiválasztjuk a másik félhez tartozó listát, observable listába tesszük, amit ki tudunk tenni a kiíró listába
        ObservableList<MessageTimeStamp> observableMessages = FXCollections.observableArrayList(allMessages.get(otherUser.getText()));
        Platform.runLater(() -> chat.setItems(observableMessages));
    }

    /**
     * Ha rákattintunk a Send gombra, elküldjük a szerver felé az üzenetet, üres üzenetet nem küldünk.
     * Végül kitöröljük a bemeneti mezőben levő üzenetet
     */
    @FXML
    public void sendTextMessage()
    {
        if(inputField.getText() == null) return;    //Ha nincs semmi beírva, nem történik semmi
        ChatApp.sendTextMessage(inputField.getText());
        inputField.clear();
    }

    /**
     * Visszaadja a jelenleg kiválasztott másik embert, akivel éppen beszélünk
     * @return A másik fél
     */
    public String getOtherUser()
    {
        return otherUser.getText();
    }

    /**
     * Kitörli a jelenleg kiválasztott másik emberrel történt chat history-t
     */
    @FXML
    public void clearHistory()
    {
        ChatApp.clearHistory(otherUser.getText());
        chat.refresh();
    }

    /**
     * Látható-e éppen a fő chatbox, vagy még nem választottunk ki senkit
     * @return true, ha látható a chatBox
     */
    public boolean isChatBoxVisible()
    {
        return chatBox.isVisible();
    }

    /**
     * Ha megnyomjuk a fájl gombot, felugrik az ablak, ahol fájlt választhatunk,
     * és üzenetben elküldjük az elérési útját
     */
    @FXML
    public void chooseSendFile()
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file to send!");
        File file = fileChooser.showOpenDialog(ChatApp.getPrimaryStage());
        if(file != null) ChatApp.sendTextMessage(file.getPath());
    }

    /**
     * Ha kiválasztunk a listában egy elemet, akkor az üzenetet a vágólapra másoljuk,
     * ha ez egy fájl, intézőbe bemásolva meg lehet nyitni a fájlt
     */
    public void toClipboard()
    {
        StringSelection stringSelection = new StringSelection(chat.getSelectionModel().getSelectedItem().getDecodedText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);
    }
}
