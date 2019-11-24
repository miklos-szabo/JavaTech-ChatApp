package Message;

import java.io.Serializable;

public class Message implements Serializable
{
    private MessageType type;
    private String text;
    private String sender;
    private String receiver;

    public Message(MessageType type, String text, String sender, String receiver)
    {
        this.type = type;
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
    }

    public MessageType getType()
    {
        return type;
    }

    public String getText()
    {
        return text;
    }

    public String getSender()
    {
        return sender;
    }

    public String getReceiver()
    {
        return receiver;
    }
}
