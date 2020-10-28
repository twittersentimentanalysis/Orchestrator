package twitter;

import api.API;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVWriter;
import config.ConfigProperties;
import kotlin.text.Regex;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.Properties;

/** Class to manipulate data such as write a csv file containing a collection of tweets to train a model or wirte a csv
 * file with the result of sentiment analysis.
 *
 * @author Ariadna de Arriba
 */
public class Data
{
    private CSVWriter writerInput;
    private CSVWriter writerOutput;
    private Properties properties;
    private API api;
    private Preprocessing preprocessing = new Preprocessing();

    /** Constructor - initializes the api and properties.
     */
    public Data()
    {
        api = new API();
        properties = ConfigProperties.readProperties();

        try
        {
            writerOutput = new CSVWriter(new FileWriter("data/twitter/output-tweets.csv"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {   //Instantiating the CSVWriter classes
            writerInput = new CSVWriter(new FileWriter("data/twitter/training-tweets.csv"));

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /** Applies sentiment analysis into a tweet read it from Kafka server.
     *  The result at the end of execution is a csv file containing the text of all tweets and a json string for each one
     *  with six emotions weighted (sentiment analysis result).
     *
     * @param tweet Tweet to apply sentiment analysis.
     * @throws IOException {@link IOException caused by an error during the call to apply sentiment analysis or translate the text.}
     * @throws ParseException {@link ParseException caused by an error during the call to apply sentiment analysis or translate the text.}
     */
    public void predictEmotionFromKafka(String tweet) throws IOException, ParseException
    {
        JsonObject json = JsonParser.parseString(tweet).getAsJsonObject();

        String text = json.get("text").toString();
        String lang = json.get("lang").getAsString();

        String cleanedText = preprocessing.cleanText(text);

        if (!lang.equals("es"))
        {   // Translate text if necessary
            cleanedText = api.translate(cleanedText, properties.getProperty("translator"), lang, properties.getProperty("tweets_lang"),properties.getProperty("translator_api-key"));
        }

        // Get emotions from text in Spanish
        String emotion = api.getEmotion(cleanedText, properties.getProperty("ml_tool"), properties.getProperty("ml_tool_api-key"));

        String[] lineCSV = new String[]{text, cleanedText, emotion};               // Writing data to a csv file

        System.out.println(emotion);
        System.out.println();

        writerOutput.writeNext(lineCSV, false);         // Writing data to the csv file
        writerOutput.flush();                                         // Flushing data from writer to file
    }

    /** Writes a tweet into csv file to collect a group of tweets and apply sentiment analysis with the objective of
     *  making a csv containing all the tweets to train a model.
     *
     * @param tweet Tweet to write into csv file and translate if necessary.
     * @throws IOException {@link IOException caused by an error during the call to translate the text.}
     * @throws ParseException {@link ParseException caused by an error during the call to translate the text.}
     */
    public void writeCsv(String tweet) throws IOException, ParseException
    {
        JsonObject json = JsonParser.parseString(tweet).getAsJsonObject();
        String text = json.get("text").toString();
        String lang = json.get("lang").getAsString();

        String cleanedText = preprocessing.cleanText(text);

        if (!lang.equals("es"))
        {   // Translate text if necessary
            cleanedText = api.translate(cleanedText, properties.getProperty("translator"), lang, properties.getProperty("tweets_lang"),properties.getProperty("translator_api-key"));
        }

        String[] lineCSV = new String[]{text, cleanedText};
        System.out.println(lineCSV.toString());

        writerInput.writeNext(lineCSV, false);           // Writing data to the csv file
        writerInput.flush();                                           // Flushing data from writer to file
    }
}