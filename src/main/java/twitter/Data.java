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

    public static void getAccuracyParallelDots() throws IOException
    {
        System.out.println("START HERE");
        SentimentAnalysisAPI staticSentimentAnalysisAPIs = new SentimentAnalysisAPI();
        Properties staticProperties = ConfigProperties.readProperties();
        try (CSVReader reader = new CSVReader(new FileReader("data/twitter/testing-emotion-dataset-nlp.csv")))
        {

            float diag = 220;
            float all = 1620;
            List<String[]> lines = reader.readAll();
            String[] line;
            int index = 969;
            int[][] cf = {{134, 204, 43}, {71, 315, 66}, {65, 281, 118}};

            //while ((line = reader.readNext()) != null)
            while ((line = lines.get(index)) != null)
            {
                String text = line[3];
                String emotion = line[4];
                System.out.println("TEXT " + line[3]);
                System.out.println("EMOTION " + line[4]);

                // Get emotion from ParallelDots
                String testingEmotionsString = staticSentimentAnalysisAPIs.getEmotion(text, staticProperties.getProperty("ml_tool"), staticProperties.getProperty("ml_tool_api-key"));

                JsonObject json = JsonParser.parseString(testingEmotionsString).getAsJsonObject();
                JsonObject emotionJson = json.getAsJsonObject("emotion");

                System.out.println(emotionJson.toString());

                float bored = parseFloat(emotionJson.get("Bored").toString());
                float angry = parseFloat(emotionJson.get("Angry").toString());
                float sad = parseFloat(emotionJson.get("Sad").toString());
                float fear = parseFloat(emotionJson.get("Fear").toString());
                float happy = parseFloat(emotionJson.get("Happy").toString());
                float excited = parseFloat(emotionJson.get("Excited").toString());

                Map emotions = new HashMap<Float, String>();
                emotions.put(bored, "bored");
                emotions.put(angry, "angry");
                emotions.put(sad, "sad");
                emotions.put(fear, "fear");
                emotions.put(happy, "happy");
                emotions.put(excited, "excited");

                float max = Math.max(Math.max(Math.max(Math.max(Math.max(bored, angry), sad), fear), happy), excited);

                String predicted = emotions.get(max).toString();
                System.out.println("PREDICTED " + predicted);

                if (emotion.equals(predicted))
                {
                    diag++;
                    if (emotion.equals("happy")) cf[0][0]++;
                    else if (emotion.equals("sad")) cf[1][1]++;
                    else if (emotion.equals("angry")) cf[2][2]++;
                }
                else
                {
                    if (emotion.equals("happy"))
                    {
                        if (predicted.equals("sad")) cf [0][1]++;
                        else if (predicted.equals("angry")) cf[0][2]++;
                    }
                    else if (emotion.equals("sad"))
                    {
                        if (predicted.equals("happy")) cf[1][0]++;
                        else if (predicted.equals("angry")) cf[1][2]++;
                    }
                    else if (emotion.equals("angry"))
                    {
                        if (predicted.equals("happy")) cf[2][0]++;
                        else if (predicted.equals("sad")) cf[2][1]++;
                    }
                }

                System.out.println("MAX: " + max + " DIAG: " + diag);
                index++;

                int i = 0; int j = 0;
                System.out.println("CONFUSION MATRIX: ");
                for (i = 0; i < cf.length; i++) {
                    for (j = 0; j < cf[i].length; j++) {
                        System.out.print(cf[i][j] + " ");
                    }
                    System.out.println();
                }

                System.out.println("INDEX: " + index);
            }
            float accuracy = diag/all;
            System.out.println("Accuracy: " + accuracy);
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
        String polarity = jsonObj.get("polarity").getAsString();

        String[] lineCSV = new String[]{text, cleanText, polarity};
        System.out.println(Arrays.toString(lineCSV));

        writerInput.writeNext(lineCSV, false);           // Writing data to the csv file
        writerInput.flush();                                           // Flushing data from writer to file
    }
}