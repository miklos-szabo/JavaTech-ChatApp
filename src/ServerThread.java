import Message.Message;
import Message.MessageType;
import Message.UserListMessage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread implements Runnable
{
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String clientUsername;
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);



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
            LOGGER.log(Level.INFO, "Key sent");

            while(true)
            {
                Message msg = (Message) inputStream.readObject();   //Blokkoló olvasás
                Runnable msgHandler = () -> handleMessage(msg);
                new Thread(msgHandler).start();
            }
        }
        catch (SocketException e)   //Akkor történik, ha kilép a felhasználó
        {
            if(clientUsername != null)
            {
                Server.removeUser(clientUsername);
                Server.broadcastUsers();
                LOGGER.log(Level.INFO, clientUsername + " has logged out and has been removed from lists");
                LOGGER.log(Level.INFO, "ServerThread of " + clientUsername + " has been destroyed");
            }
            else LOGGER.log(Level.INFO, "Thread of not logged in user has been destroyed");
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.log(Level.SEVERE, "Class not found!");
        }
        catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "IOException in ServerThread!");
        }
        finally
        {
            try
            {
                clientSocket.close();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    //Szerverhez beérkező üzenet kezelése
    public void handleMessage(Message message)
    {
        LOGGER.log(Level.INFO, "Received: " + message);
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
                        LOGGER.log(Level.INFO, "Added " + clientUsername + " to the users logged in");
                        outputStream.writeObject(createUsersMessage());     //Elküldjük a jelenleg bejelentkezve levő felhasználókat
                        LOGGER.log(Level.INFO, "Sent users logged in to " + clientUsername);
                    }
                    else
                        reply(createErrorMessage("Wrong username or password!"));
                }
                catch (SQLException e)
                {
                    reply(createErrorMessage("Database not found!"));
                }
                catch (IOException e)
                {
                    e.printStackTrace();
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
                    message.setSender("server");
                    message.setReceiver(clientUsername);    //Logolásnál jó infók jelenkenek meg
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
        try
        {
            outputStream.writeObject(message);
            LOGGER.log(Level.INFO, "Sent: " + message);
        }
        catch (IOException e)
        {
            e.printStackTrace();
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

    public UserListMessage createUsersMessage()
    {
        return new UserListMessage(Server.getLoggedInUsers());
    }


}
