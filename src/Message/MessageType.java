package Message;

/**
 * Üzenettípusok enum-ja
 */
public enum MessageType
{
    REGISTER,       //Regisztrációhoz
    LOGIN,          //Bejelentkezéshez
    TEXT,           //Szöveges üzenet küldéséhez
    ERROR,          //Hiba küldéséhez
    OK,             //Ha valami sikeres volt
    USERS           //Felhasználó lista küldéséhez
}
