package Message;

import java.util.List;

public class UserListMessage extends Message
{
    private List<String> users;

    public UserListMessage(List<String> users)
    {
        super(MessageType.USERS, "".getBytes(), "server", "");
        this.users = users;
    }

    public List<String> getUsers()
    {
        return users;
    }
}
