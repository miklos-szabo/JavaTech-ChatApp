package Message;

import java.util.List;

/**
 * Felhasználók listáját lehet vele üzenetbe foglalni és küldeni
 */
public class UserListMessage extends Message
{
    private List<String> users;

    /**
     * Konstruktor, ezt használjuk mindig.
     * @param users Bejelentkezve levő felhasználók listája
     */
    public UserListMessage(List<String> users)
    {
        super(MessageType.USERS, "".getBytes(), "server", "");
        this.users = users;
    }

    /**
     * Visszaadja a bejelentkezett felhasználók listáját
     * @return A bejelentkezett felhasználók listája
     */
    public List<String> getUsers()
    {
        return users;
    }
}
