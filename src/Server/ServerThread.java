package Server;

import Cryptography.Cryptography;
import Database.DBUtilities;
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

/**
 * A Server.ServerThread osztály akkor jön létre, amikor csatlakozik egy új kliens.
 * Minden klienshez külön példány tartozik.
 * Ez az osztály valósítja meg a kliennsel való kapcsolatot.
 */
public class ServerThread implements Runnable
{
    private Socket clientSocket;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String clientUsername;
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    /**
     * Lekérdezi a példányhoz kapcsolódó felhasználó nevét
     * @return A felhasználó neve
     */
    public String getClientUsername()
    {
        return clientUsername;
    }

    /**
     * Default konstruktor, a kapott {@link Socket}-et elmenti a saját privát változójába
     * @param clientSocket Az ehhez a példányhoz kapcsolódó kliens {@link Socket}-je
     */
    public ServerThread(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
    }

    /**
     * Itt kommunikálunk a klienssel, ez hívódik meg, amikor egy kliens csatlakozik.
     * Első dologként elküldjük a titkosítás kulcsát, hogy tudja kódolni és dekódolni az üzeneteket.
     * Ezután pedig várjuk a felhasználó küldött üzeneteit.
     * Ha a felhasználó bezárja az alkalmazást, eltávolítjuk őt a bejelentkezett felhasználók közül,
     * és elküldjük minden maradék felhasználónak az új felhasználó-listát, végül bezárjuk a kliens {@link Socket}-jét.
     */
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
                new Thread(() -> handleMessage(msg)).start();       //Lehet nem szükséges új threadben indítani
            }
        }
        catch (SocketException e)   //Akkor történik, ha kilép a felhasználó
        {
            if(clientUsername != null)  //Ha bejelentkezés után lépett ki
            {
                Server.removeUser(clientUsername);
                Server.broadcastUsers();
                LOGGER.log(Level.INFO, clientUsername + " has logged out and has been removed from lists");
                LOGGER.log(Level.INFO, "Server.ServerThread of " + clientUsername + " has been destroyed");
            }
            else LOGGER.log(Level.INFO, "Thread of not logged in user has been destroyed");
        }
        catch (ClassNotFoundException e)
        {
            LOGGER.log(Level.SEVERE, "Class not found!");
        }
        catch (IOException e)
        {
            LOGGER.log(Level.SEVERE, "IOException in Server.ServerThread!");
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

    /**
     * A beérkező üzeneteket itt kezeljük.
     * @param message A beérkezett üzenet
     */
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
                        reply(createOKREGISTERMessage("Successfully registered!"));
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
                        reply(createOKLOGINMessage("Successfully Logged in! Welcome " + message.getSender() + "!"));
                        this.clientUsername = message.getSender();  //Elmentjük a felhasználónevet
                        Server.addUser(message.getSender(), this);  //Hozzáadjuk őt a bejelentkezett felhasználókhoz
                        LOGGER.log(Level.INFO, "Added " + clientUsername + " to the users logged in");
                        Server.broadcastUsers();     //Elküldjük a jelenleg bejelentkezve levő felhasználókat mindenkinek
                        LOGGER.log(Level.INFO, "Sent users logged in to " + clientUsername);
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
                    //Megekeressük a fogadó serverThread-jét, ami elküldi a kliensének az üzenetet
                    Server.findUser(message.getReceiver()).reply(message);
                    //Magunknak is elküldjük az üzenetet, küldő szerint másféle módon írjuk ki
                    message.setReceiver(clientUsername);    //Logolásnál jó infók jelenkenek meg
                    reply(message);
                }
                catch(NullPointerException ex)  //Ha nem találtuk meg a felhasználót a listában
                {
                    reply(createErrorMessage(message.getReceiver() + " isn't logged in!"));
                }
            }
        }
    }

    /**
     * Ő küldi el a kliensének az üzeneteket.
     * @param message A küldendő üzenet
     */
    public void reply(Message message)
    {
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

    /**
     * Error típusú üzenet létrehozása
     * @param text Az Error üzenet szövege, mi volt a hiba
     * @return A létrehozott {@link Message} objektum
     */
    public Message createErrorMessage(String text)
    {
        return new Message(MessageType.ERROR, Cryptography.encryptString(text), "server", "");
    }

    /**
     * OKREGISTER típusú üzenet létrehozása
     * @param text Az OKREGISTER üzenet törzse, mi történt, ami sikeres volt, pl. bejelentkezés
     * @return A létrehozott {@link Message} objektum
     */
    public Message createOKREGISTERMessage(String text)
    {
        return new Message(MessageType.OKREGISTER, Cryptography.encryptString(text), "server", "");
    }

    /**
     * OKLOGIN típusú üzenet létrehozása
     * @param text Az OKLOGIN üzenet törzse
     * @return A létrehozott {@link Message} objektum
     */
    public Message createOKLOGINMessage(String text)
    {
        return new Message(MessageType.OKLOGIN, Cryptography.encryptString(text), "server", "");
    }

    /**
     * USERS típusú üzenet létrehozása, amiben a felhasználók listája van
     * @return A létrehozott {@link UserListMessage} objektum
     */
    public UserListMessage createUsersMessage()
    {
        return new UserListMessage(Server.getLoggedInUsers());
    }
}
