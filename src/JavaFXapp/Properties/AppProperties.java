package JavaFXapp.Properties;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class AppProperties
{
    public static final Properties instance = new Properties();

    public static void setProperty(String key, double value)
    {
        instance.setProperty(key, Double.toString(value));
    }

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

    public static void store(String path) throws IOException
    {
        instance.store(new FileOutputStream(path), "");
    }

    public static void load(String path) throws IOException
    {
        instance.load(new FileInputStream(path));
    }
}
