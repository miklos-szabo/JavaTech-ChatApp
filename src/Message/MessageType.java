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
    OKREGISTER,     //Sikeres regisztráviónál
    OKLOGIN,        //Sikeres bejelentkezés
    USERS           //Felhasználó lista küldéséhez
}
