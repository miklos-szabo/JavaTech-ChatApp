package Message;

import java.io.Serializable;

/**
 * Egy üzenetet megvalósító osztály
 */
public class Message implements Serializable
{
    private MessageType type;
    private byte[] text;
    private String sender;
    private String receiver;

    /**
     * Konstruktor, ez használandó mindenhol
     * @param type Az üzenet {@link MessageType} típusa.
     * @param text A szöveg, már {@link Cryptography.Cryptography} osztállyal titkosított verziója
     * @param sender A küldö felhasználóneve
     * @param receiver A fogadó felhasználóneve
     */
    public Message(MessageType type, byte[] text, String sender, String receiver)
    {
        this.type = type;
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
    }

    /**
     * Az üzenet típusát adja vissza
     * @return Az üzenet {@link MessageType} típusa
     */
    public MessageType getType()
    {
        return type;
    }

    /**
     * Az üzenet, továbbra is tikosított verzióját adja vissza
     * @return Az üzenet szövege, még mindig titkosítva
     */
    public byte[] getText()
    {
        return text;
    }

    /**
     * A küldőt adja vissza
     * @return A küldő felhasználóneve
     */
    public String getSender()
    {
        return sender;
    }

    /**
     * A fogadót adja vissza
     * @return A fogadó felhasználóneve
     */
    public String getReceiver()
    {
        return receiver;
    }

    /**
     * Beállítja a fogadót
     * @param receiver Az új fogadó felhasználóneve
     */
    public void setReceiver(String receiver)
    {
        this.receiver = receiver;
    }

    /**
     * Stringet készít az üzenetből, text nélkül, az amúgy is titkosítva van
     * @return Az üzenet {@link String}-jes
     */
    @Override
    public String toString()
    {
        return "Message{" +
                "type=" + type +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                '}';
    }
}
