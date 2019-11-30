import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server implements Runnable
{
    private static final int PORT = 2600;
    private static final int POOL_SIZE = 10;

    private ExecutorService clientPool;
    private static ConcurrentHashMap<String, ServerThread> usersLoggedIn;
    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


    public static void main(String[] args)
    {
        DBUtilities.InitDB();
        Cryptography.init();
        new Server().run();
    }

    public Server()
    {
        clientPool = Executors.newFixedThreadPool(POOL_SIZE);
        usersLoggedIn = new ConcurrentHashMap<>();
    }

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

    public static void addUser(String username, ServerThread serverThread)
    {
        usersLoggedIn.put(username, serverThread);
    }

    public static void removeUser(String username)
    {
        usersLoggedIn.remove(username);
    }

    //Visszaadja az adott nevű felhasználó serverThread-jét, ezzel tudunk majd üzenetet küldeni
    public static ServerThread findUser(String username)
    {
        return usersLoggedIn.get(username);
    }

    public static List<String> getLoggedInUsers()
    {
        return new ArrayList<>(usersLoggedIn.keySet());
    }

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
