package api;

import config.ConfigProperties;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Properties;

/** Class that contains the methods to connect with Sentiment Analysis API
 *
 * @author Ariadna de Arriba
 */
public class SentimentAnalysisAPI
{
    private final Properties properties = ConfigProperties.readProperties();
    private final String baseUrl = properties.getProperty("host") + ":8080/api/";

    /** Make a request to ML tool for sentiment analysis.
     *
     * @param text Text to analyze.
     * @param tool Machine learning tool to do sentiment analysis.
     * @param apiKey Api key to authorize the machine learning tool.
     * @return Returns a string that contains a json with each of six emotions weighted.
     */
    public String getEmotion(String text, String tool, String apiKey) throws IOException
    {
        String url = baseUrl + "emotion?tool=" + tool;
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", text);
        String json = jsonObject.toString();

        RequestBody requestBody = RequestBody.create(
                json,
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", apiKey)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /** Make a request to translator api.API to translate a block of text.
     *
     * @param text Text to translate
     * @param translator Translator api.API to translate the text.
     * @param from Source language code.
     * @param to Target language code.
     * @param apiKey Api-key to authorize translator api.API.
     * @return Returns a string that contains a json with text translated and source and target language codes.
     */
    public String translate(String text, String translator, String from, String to, String apiKey) throws IOException, ParseException
    {
        String url = baseUrl + "translator?translator=" + translator;
        OkHttpClient client = new OkHttpClient();

        JSONObject transObj = new JSONObject();
        JSONObject langObj = new JSONObject();

        transObj.put("text", text);
        transObj.put("language", langObj);

        langObj.put("from", from);
        langObj.put("to", to);

        String json = transObj.toString();

        RequestBody requestBody = RequestBody.create(
                json,
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", apiKey)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();

        JSONParser parser = new JSONParser();
        JSONObject jsonBody = (JSONObject) parser.parse(response.body().string());

        return jsonBody.get("translation").toString();
    }
}