import java.io.InputStreamReader;
import java.util.Scanner;

public class Server
{
    public static void main(String[] args)
    {
        DBUtilities.InitDB();

        Scanner sc = new Scanner(new InputStreamReader(System.in));
        String username = sc.nextLine();
        String password = sc.nextLine();

        DBUtilities.login(username, password.hashCode());

        DBUtilities.closeDB();
    }
}
