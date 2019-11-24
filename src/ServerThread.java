import Message.Message;
import Message.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;

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
        switch (message.getType())
        {
            case REGISTER:
            {
                try
                {
                    if(DBUtilities.register(message.getSender(), message.getText().hashCode()))
                        reply(new Message(MessageType.OK, "Successfully registered!", "server", ""));
                    else
                        reply(new Message(MessageType.ERROR, "Username already taken!", "server", ""));
                }
                catch (SQLException e)
                {
                    reply(new Message(MessageType.ERROR, "Database not found!", "server", ""));
                }

            } break;
            case LOGIN:
            {
                try
                {
                    if(DBUtilities.login(message.getSender(), message.getText().hashCode()))
                    {
                        reply(new Message(MessageType.OK, "Successfully Logged in! Welcome " + message.getSender() + "!", "server", ""));
                        this.clientUsername = message.getSender();
                        Server.addUser(message.getSender(), this);
                    }
                    else
                        reply(new Message(MessageType.ERROR, "Wrong username or password!", "server", ""));
                }
                catch (SQLException e)
                {
                    reply(new Message(MessageType.ERROR, "Database not found!", "server", ""));
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
                    reply(new Message(MessageType.ERROR, message.getReceiver() + " isn't logged in!", "server", ""));
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
        }
        catch (IOException e)
        {
            e.printStackTrace();    //TODO felhasználó kilépett -> exception
        }
    }
}
