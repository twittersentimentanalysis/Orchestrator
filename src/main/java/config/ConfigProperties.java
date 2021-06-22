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

        try
        {
            properties.load(ConfigProperties.class.getResourceAsStream("/config.properties"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return properties;
    }
}
