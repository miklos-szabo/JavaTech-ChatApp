package Database;

import org.sqlite.SQLiteException;

import java.sql.*;

public class DBUtilities
{
    private static Connection connection;

    /**
     * Inicializálja az adatbázist - létrehozza a kapcsolatot
     */
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

    /**
     * Lezárja az adatbázis kapcsolatot
     */
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

    /**
     * Regisztrációs próbálkozást valósít meg.
     * @param username A felhasználónév, regex-en már átesett
     * @param passwordHash A jelszó hash kódja
     * @return Sikeres volt-e a regisztráció?
     * @throws SQLException Ha nem találja az adatbázist
     */
    public static boolean register(String username, int passwordHash) throws SQLException
    {
        try(PreparedStatement st = connection.prepareStatement("INSERT into User(Username, Password) VALUES (?, ?)"))
        {
            st.setString(1, username);      //TODO regex
            st.setInt(2, passwordHash);
            st.executeUpdate();
            return true;        //Ha sikerült a regisztráció, igaz
        }
        catch(SQLiteException ex)
        {
            return false;       //Ha nem sikerült a regisztráció, itt leszünk
        }
    }

    /**
     * Bejelentkezési próbálkozást valósít meg
     * @param username A felhasználónév, regex-en már átasett
     * @param passwordHash A jelszó hash kódja
     * @return Sikeres volt-e a regisztráció?
     * @throws SQLException Ha nem találja az adatbázist
     */
    public static boolean login(String username, int passwordHash) throws SQLException
    {
        try (PreparedStatement st = connection.prepareStatement("select Username, Password from User" +
                " where Username = ? and Password = ?"))
        {
            st.setString(1, username);  //TODO regex
            st.setInt(2, passwordHash);
            ResultSet resultSet = st.executeQuery();
            return(resultSet.next());      //Ha van benne elem, igaz, ha nem, hamis
        }
    }
}
