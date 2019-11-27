package Message;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Message implements Serializable
{
    private MessageType type;
    private byte[] text;
    private String sender;
    private String receiver;

    public Message(MessageType type, byte[] text, String sender, String receiver)
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

    public byte[] getText()
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

    @Override
    public String toString()
    {
        return "Message{" +
                "type=" + type +
                ", text='" + new String(text, StandardCharsets.UTF_8) + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                '}';
    }
}
