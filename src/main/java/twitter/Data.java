package twitter;

import api.PreprocessingAPI;
import api.SentimentAnalysisAPI;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import config.ConfigProperties;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

/** Class to manipulate data such as write a csv file containing a collection of tweets to train a model or write a csv
 * file with the result of sentiment analysis.
 *
 * @author Ariadna de Arriba
 */
public class Data
{
    private CSVWriter writerInput;
    private CSVWriter writerOutput;
    private final Properties properties;
    private final SentimentAnalysisAPI sentimentAnalysisAPI;
    private final PreprocessingAPI preprocessingAPI;


    /** Constructor - initializes the api and properties.
     */
    public Data()
    {
        preprocessingAPI = new PreprocessingAPI();
        sentimentAnalysisAPI = new SentimentAnalysisAPI();
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
     */
    public void predictEmotionFromKafka(String tweet) throws IOException, ParseException
    {
        JsonObject json = JsonParser.parseString(tweet).getAsJsonObject();

        String text = json.get("text").toString();
        String lang = json.get("lang").getAsString();

        if (!lang.equals("es"))
        {   // Translate text if necessary
            text = sentimentAnalysisAPI.translate(text, properties.getProperty("translator"), lang, properties.getProperty("tweets_lang"),properties.getProperty("translator_api-key"));
        }

        // Preprocess text
        String cleanText = preprocessingAPI.cleanText(text, properties.getProperty("preprocessing_api-key"));

        // Get emotions from text in Spanish
        String emotion = sentimentAnalysisAPI.getEmotion(cleanText, properties.getProperty("ml_tool"), properties.getProperty("ml_tool_api-key"));

        String[] lineCSV = new String[]{text, emotion};               // Writing data to a csv file

        System.out.println(emotion);
        System.out.println();

        writerOutput.writeNext(lineCSV, false);         // Writing data to the csv file
        writerOutput.flush();                                         // Flushing data from writer to file
    }

    /** Writes a tweet into csv file to collect a group of tweets and apply sentiment analysis with the objective of
     *  making a csv containing all the tweets to train a model.
     *
     * @param tweet Tweet to write into csv file and translate if necessary.
     */
    public void writeCsv(String tweet) throws IOException, ParseException
    {
        JsonObject json = JsonParser.parseString(tweet).getAsJsonObject();
        String text = json.get("text").toString();
        String lang = json.get("lang").getAsString();

        if (!lang.equals("es"))
        {   // Translate text if necessary
            text = sentimentAnalysisAPI.translate(text, properties.getProperty("translator"), lang, properties.getProperty("tweets_lang"),properties.getProperty("translator_api-key"));
        }

        // Preprocess text
        String jsonResponse = preprocessingAPI.cleanText(text, properties.getProperty("preprocessing_api-key"));
        System.out.println(jsonResponse);
        JsonObject jsonObj = JsonParser.parseString(jsonResponse.toString()).getAsJsonObject();
        String cleanText = jsonObj.get("text").toString().replace("\"", "");;

        String[] lineCSV = new String[]{text, cleanText};
        System.out.println(Arrays.toString(lineCSV));

        writerInput.writeNext(lineCSV, false);           // Writing data to the csv file
        writerInput.flush();                                           // Flushing data from writer to file
    }
}