import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable
{
    private static final int PORT = 2600;
    private static final int POOL_SIZE = 10;

    private ExecutorService clientPool;
    private static ConcurrentHashMap<String, ServerThread> usersLoggedIn;


    public static void main(String[] args)
    {
        DBUtilities.InitDB();
        new Server().run();
        DBUtilities.closeDB();
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
                System.out.println("Waiting for connection");
                clientPool.execute(new ServerThread(serverSocket.accept()));
                System.out.println("Connected");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            clientPool.shutdown();
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
}
