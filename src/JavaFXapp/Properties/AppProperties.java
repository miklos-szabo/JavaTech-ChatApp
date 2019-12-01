package JavaFXapp.Properties;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Saját Property osztály, hogy tudjunk double-t tárolni
 */
public class AppProperties
{
    public static final Properties instance = new Properties();

    /**
     * Eltárolja a {@link Properties} instance-ben az adott párt
     * @param key Kulcs
     * @param value Érték
     */
    public static void setProperty(String key, double value)
    {
        instance.setProperty(key, Double.toString(value));
    }

    /**
     * Visszaadja az adott kulcsú propertyt, ha nem találja a kulcsot, a defaultValue-t adja vissza
     * @param key kulcs
     * @param defaultValue Visszatérési érték, ha nem találja a kulcsot
     * @return A kulcshoz tartozó érték
     */
    public static double getDoubleProperty(String key, double defaultValue)
    {
        try
        {
            return Double.parseDouble(instance.getProperty(key, Double.toString(defaultValue)));
        }
        catch(NumberFormatException e)
        {
            throw new PropertyTypeMismatch();
        }
    }

    /**
     * Eltárolja a beállított propertyket egy fájlban
     * @param path a fájl elérési útvonala
     * @throws IOException
     */
    public static void store(String path) throws IOException
    {
        instance.store(new FileOutputStream(path), "");
    }

    /**
     * Betölti a propertyket az adott fájlból
     * @param path A fájl elérési útvonala
     * @throws IOException
     */
    public static void load(String path) throws IOException
    {
        instance.load(new FileInputStream(path));
    }
}
