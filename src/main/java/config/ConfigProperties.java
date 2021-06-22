package config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/** Configuration class for properties file
 *
 * @author Ariadna de Arriba
 */
public class ConfigProperties
{
    /** Read properties file.
     *
     * @return Returns an instance of this class.
     */
    public static Properties readProperties()
    {
        Properties properties = new Properties();
        FileInputStream inputStream = null;

        try
        {
            inputStream = new FileInputStream("config.properties");
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        try
        {
            properties.load(inputStream);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return properties;
    }
}
