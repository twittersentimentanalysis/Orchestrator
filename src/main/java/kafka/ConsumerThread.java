package kafka;

import config.ConfigProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.simple.parser.ParseException;
import twitter.Data;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

/** Class that contains the methods to run a Kafka consumer thread.
 *
 * @author Ariadna de Arriba
 */
public class ConsumerThread extends Thread
{
    private KafkaConsumer<String, String> kafkaConsumer;
    private final String topicName;
    private final String groupId;
    private final Properties properties = ConfigProperties.readProperties();
    private final Data data = new Data();

    /** Constructor
     *
     * @param topicName Kafka topic name.
     * @param groupId kafka group id.
     */
    public ConsumerThread(String topicName, String groupId)
    {
        this.topicName = topicName;
        this.groupId = groupId;
    }

    /** Run Kafka consumer thread
     */
    public void run()
    {
        Properties configProperties = new Properties();
        configProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        configProperties.put(ConsumerConfig.CLIENT_ID_CONFIG, "simple");
        configProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        //Figure out where to start processing messages from
        kafkaConsumer = new KafkaConsumer<>(configProperties);
        kafkaConsumer.subscribe(Arrays.asList(topicName));

        //Start processing messages
        try
        {
            TopicPartition topicPartition = new TopicPartition(properties.getProperty("kafka_topic"), 0);
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofSeconds(10));
            kafkaConsumer.seekToBeginning(Collections.singleton(topicPartition));
            analyzeRecords(records);

            while (true)
            {
                records = kafkaConsumer.poll(Duration.ofMillis(100));
                analyzeRecords(records);
            }
        } catch (WakeupException ex)
        {
            System.out.println("Exception caught " + ex.getMessage());
        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            kafkaConsumer.close();
            System.out.println("After closing KafkaConsumer");
        }
    }

    /** Get Kafka consumer.
     *
     * @return Returns an instance of KafkaConsumer
     */
    public KafkaConsumer<String, String> getKafkaConsumer() throws NullPointerException
    {
        return this.kafkaConsumer;
    }

    /** Read kafka records (tweets in this case) and apply sentiment analysis or collect data to train a machine
     * learning model.
     *
     * @param records Records read.
+     * @throws ParseException {@link ParseException caused by writeCsv or predictEmotionFromKafka methods.}}
     */
    private void analyzeRecords(ConsumerRecords<String, String> records) throws IOException, ParseException
    {
        for (ConsumerRecord<String, String> record : records)
        {
            if (properties.getProperty("goal").equals("collect-data"))              data.writeCsv(record.value());
            else if (properties.getProperty("goal").equals("sentiment-analysis"))   data.predictEmotionFromKafka(record.value());
        }
    }
}