import org.sqlite.SQLiteException;

import java.sql.*;

public class DBUtilities
{
    private static Connection connection;

    public static void InitDB()
    {
        try
        {
            connection = DriverManager.getConnection("jdbc:sqlite:ChatAppDB.db");
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void closeDB()
    {
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void register(String username, int passwordHash)
    {
        try(PreparedStatement st = connection.prepareStatement("INSERT into User(Username, Password) VALUES (?, ?)"))
        {
            st.setString(1, username);
            st.setInt(2, passwordHash);
            st.executeUpdate();
        }
        catch(SQLiteException ex)
        {
            System.out.println("Username already taken!");  //TODO értelmes kiírás
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean login(String username, int passwordHash)
    {
        try(PreparedStatement st = connection.prepareStatement("select Username, Password from User" +
                                                                " where Username = ? and Password = ?"))
        {
            st.setString(1, username);
            st.setInt(2, passwordHash);
            ResultSet resultSet = st.executeQuery();
            if(resultSet.next())
            {
                System.out.println("Successfully logged in! Welcome, " + resultSet.getString("Username"));  //TODO értelmes kiírás
                return true;
            }
            else System.out.println("Wrong username or password!");  //TODO értelmes kiírás
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

}
