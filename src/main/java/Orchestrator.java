import config.ConfigProperties;
import kafka.ConsumerThread;
import twitter.Data;

import java.io.IOException;
import java.util.Properties;

/** Main class to run the orchestrator that mainly reads tweets from kafka and applies sentiment analysis.
 *
 * @author Ariadna de Arriba
 */
public class Orchestrator
{
    private static Properties properties = ConfigProperties.readProperties();

    /** Main function to run the orchestrator.
     *
     * @param argv Function argument vector.
     * @throws InterruptedException {@link InterruptedException caused trying to get data from Kafka.}
     */
    public static void main(String[] argv) throws InterruptedException, IOException
    {

        String topicName = properties.getProperty("kafka_topic");
        String groupId = properties.getProperty("group_id");

        ConsumerThread consumerRunnable = new ConsumerThread(topicName, groupId);
        consumerRunnable.start();

        consumerRunnable.getKafkaConsumer().wakeup();
        System.out.println("Stopping consumer .....");
        consumerRunnable.join();
    }
}