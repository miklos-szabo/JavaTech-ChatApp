import Message.Message;
import Message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Objects;

public class ServerThread implements Runnable
{
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String clientUsername;

    public String getClientUsername()
    {
        return clientUsername;
    }

    public ServerThread(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run()
    {
        try
        {
            this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            this.inputStream = new ObjectInputStream(clientSocket.getInputStream());

            outputStream.writeObject(Cryptography.getKeyPair());    //Amikor a kliens csatlakozik, elküldjük a titkosítás kulcsát

            while(true)
            {
                Message msg = (Message) inputStream.readObject();   //Blokkoló olvasás
                Runnable msgHandler = () -> handleMessage(msg);
                new Thread(msgHandler).start();
            }
            //TODO kijelentkezett felhasználó törlése
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Class not found!" + e.getMessage());
        }
    }

    //Szerverhez beérkező üzenet kezelése
    public void handleMessage(Message message)
    {
        System.out.println("Received: " + message);
        switch (message.getType())
        {
            case REGISTER:
            {
                try
                {
                    if(DBUtilities.register(message.getSender(),
                            Integer.parseInt(Objects.requireNonNull(Cryptography.decryptToString(message.getText())))))
                        reply(createOKMessage("Successfully registered!"));
                    else
                        reply(createErrorMessage("Username already taken!"));
                }
                catch (SQLException e)
                {
                    reply(createErrorMessage("Database not found!"));
                }

            } break;
            case LOGIN:
            {
                try
                {
                    if(DBUtilities.login(message.getSender(),
                            Integer.parseInt(Objects.requireNonNull(Cryptography.decryptToString(message.getText())))))
                    {
                        reply(createOKMessage("Successfully Logged in! Welcome " + message.getSender() + "!"));
                        this.clientUsername = message.getSender();
                        Server.addUser(message.getSender(), this);
                    }
                    else
                        reply(createErrorMessage("Wrong username or password!"));
                }
                catch (SQLException e)
                {
                    reply(createErrorMessage("Database not found!"));
                }
            } break;
            case TEXT:
            {
                try
                {
                    //A fogadó serverThread-je elküldi a kliensének az üzenetet
                    Server.findUser(message.getReceiver()).reply(message);
                    //Magunknak is elküldjük az üzenetet, küldő szerint másféle módon írjuk ki
                    //TODO küldő szerinti másféle kiírási mód
                    reply(message);
                }
                catch(NullPointerException ex)
                {
                    reply(createErrorMessage(message.getReceiver() + " isn't logged in!"));
                }
                break;
            }
        }
    }

    public void reply(Message message)
    {
        //TODO saját thread?
        //TODO titkosítás
        try
        {
            outputStream.writeObject(message);
            System.out.println("Sent: " + message);
        }
        catch (IOException e)
        {
            e.printStackTrace();    //TODO felhasználó kilépett -> exception
        }
    }

    public Message createErrorMessage(String text)
    {
        return new Message(MessageType.ERROR, Cryptography.encryptString(text), "server", "");
    }

    public Message createOKMessage(String text)
    {
        return new Message(MessageType.OK, Cryptography.encryptString(text), "server", "");
    }


}
