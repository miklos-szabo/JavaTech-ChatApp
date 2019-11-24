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
    private static ConcurrentHashMap<String, ServerThread> UsersLoggedIn;


    public static void main(String[] args)
    {
        DBUtilities.InitDB();
        new Server().run();
    }

    public Server()
    {
        clientPool = Executors.newFixedThreadPool(POOL_SIZE);
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
            DBUtilities.closeDB();
        }
    }

    public static void addUser(String username, ServerThread serverThread)
    {
        UsersLoggedIn.put(username, serverThread);
    }

    public static void removeUser(String username)
    {
        UsersLoggedIn.remove(username);
    }

    //Visszaadja az adott nevű felhasználó serverThread-jét, ezzel tudunk majd üzenetet küldeni
    public static ServerThread findUser(String username)
    {
        return UsersLoggedIn.get(username);
    }
}
