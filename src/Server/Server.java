package Server;

import Cryptography.Cryptography;
import Database.DBUtilities;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A server osztály egy console app, ami megvalósítja a chat app szerverét.
 */
public class Server implements Runnable
{
    private static final int PORT = 2600;
    private static final int POOL_SIZE = 10;

    private ExecutorService clientPool;
    private static ConcurrentHashMap<String, ServerThread> usersLoggedIn; //<felhasználónév, hozzá tartozó Server.ServerThread>
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Ennek elindításával indul a szerver, inicializálja az adatbázist és a {@link Cryptography} titkosítást.
     * @param args .
     */
    public static void main(String[] args)
    {
        DBUtilities.InitDB();
        Cryptography.init();
        new Server().run();
    }

    /**
     * Default konstruktor, inicializálja az {@link ExecutorService}-t, és üres {@link ConcurrentHashMap}-et állít be
     * a bejelentkezve levő felhasználókra
     */
    public Server()
    {
        clientPool = Executors.newFixedThreadPool(POOL_SIZE);
        usersLoggedIn = new ConcurrentHashMap<>();
    }


    /**
     * Várjuk a csatlakozásokat az adott portra, és elindítjük a {@link ServerThread}-jeiket
     * Hiba esetén, ill. kilépéskor lezárjuk az {@link ExecutorService}-t és az adatbázist.
     */
    @Override
    public void run()
    {
        try (ServerSocket serverSocket = new ServerSocket(PORT))
        {
            while(true)
            {
                LOGGER.log(Level.INFO, "Waiting for connection");
                clientPool.execute(new ServerThread(serverSocket.accept()));
                LOGGER.log(Level.INFO, "New connection");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            clientPool.shutdown();
            DBUtilities.closeDB();
        }
    }

    /**
     * Hozzáad egy felhasználót, és a {@link ServerThread}-jét
     *  a bejelentkezve levő felhasználók {@link ConcurrentHashMap}-jéhez
     * @param username A felhasználó username-je
     * @param serverThread A felhasználó {@link ServerThread}-je
     */
    public static void addUser(String username, ServerThread serverThread)
    {
        usersLoggedIn.put(username, serverThread);
    }

    /**
     * Eltávolít egy felhasználót a bejelentkezve levő felhasználók {@link ConcurrentHashMap}-jéből
     * @param username A felhasználó username-je
     */
    public static void removeUser(String username)
    {
        usersLoggedIn.remove(username);
    }

    /**
     * Visszaadja az adott nevű felhasználó {@link ServerThread}-jét, azzel tudunk majd üzenetet küldeni
     * @param username A felhasználó username-je
     * @return A keresett felhasználó serverThread-je -- lehet null, kezelendő exception
     */
    public static ServerThread findUser(String username)
    {
        return usersLoggedIn.get(username);
    }

    /**
     * Visszaadja a jelenleg bejelentkezve levő felhasználók listáját {@link ArrayList}-ként
     * @return A jelenleg bejelentkezve levő felhasználók listája
     */
    public static List<String> getLoggedInUsers()
    {
        return new ArrayList<>(usersLoggedIn.keySet());     //kulcsok setjéből csinál listát
    }

    /**
     * Elküldi minden, jelenleg bejelentkezett felhasználónak a jelenleg bejelentkezett felhasználók listáját
     */
    public static void broadcastUsers()
    {
        for(ServerThread userThread : usersLoggedIn.values())
        {
            //Ha valaki kijelentkezik, elküldjük mindenkinek a frissített bejelentkezve levő felhasználókat
            //TODO ha a jelenleg kiválasztott ember nincs ebben a listában, ki lesz írva a képernyőre
            userThread.reply(userThread.createUsersMessage());
        }
    }
}
