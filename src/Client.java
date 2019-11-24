import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client implements Runnable
{
    private String username;
    private int port;

    public Client(int port)
    {
        this.port = port;
    }

    public static void main(String[] args)
    {
        new Client(2600).run();
    }

    @Override
    public void run()
    {
        try(Socket socket = new Socket())
        {
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), 2600));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
