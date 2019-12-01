package JavaFXapp.ChatScene;

import JavaFXapp.ChatApp;
import Message.MessageTimeStamp;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Map;


public class ChatSceneController
{
    private static ChatSceneController instance;

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

    public ChatSceneController()
    {
        instance = this;
    }

    @FXML
    private void initialize() {}

    public static ChatSceneController getInstance()
    {
        return instance;
    }

    public void setUsersLoggedIn(List<String> userList)
    {
        ObservableList<String> observableUsers = FXCollections.observableArrayList(userList);
        usersLoggedIn.setItems(observableUsers);
    }

    @FXML
    public void displayChatBoxWithUser()
    {
        if(chatBox.isVisible())     //Ha volt már valaki kiválasztva, akkor elmentjük a vele való beszélgetést
        {
            //ChatApp.saveHistory(otherUser.getText());
        }
        otherUser.setText(usersLoggedIn.getSelectionModel().getSelectedItem()); //Felső labelt az új emberre állítjuk
       // displayMessagesFromMap(ChatApp.loadHistory(otherUser.getText()));   //A loadhistory mindenképp az összes üzenet
        //Mapjével tér vissza de a másik félhez tartozó lista lehet üres.
        ChatApp.TEMPintializeMapForUser(otherUser.getText());
        chatBox.setVisible(true);
    }

    public void displayMessagesFromMap(Map<String, List<MessageTimeStamp>> allMessages)
    {
        //Kiválasztjuk a másik félhez tartozó listát, observable listába tesszük, amit ki tudunk tenni a kiíró listába
        ObservableList<MessageTimeStamp> observableMessages = FXCollections.observableArrayList(allMessages.get(otherUser.getText()));
        Platform.runLater(() -> chat.setItems(observableMessages));
    }

    @FXML
    public void sendTextMessage()
    {
        if(inputField.getText() == null) return;    //Ha nincs semmi beírva, nem történik semmi
        ChatApp.sendTextMessage(inputField.getText());
        inputField.clear();
    }

    public String getOtherUser()
    {
        return otherUser.getText();
    }

    public void refreshChat()
    {

    }
}
