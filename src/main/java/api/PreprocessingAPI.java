package api;

import com.google.gson.Gson;
import okhttp3.*;
import org.json.simple.JSONObject;

import java.io.IOException;

/** Class that contains the methods to connect with Twitter Preprocessing API
 *
 * @author Ariadna de Arriba
 */
public class PreprocessingAPI
{
    private final String url = "http://0.0.0.0:8390/api/v1/preprocessing";

    /** Make a request to ML tool for sentiment analysis.
     *
     * @param text Text to analyze.
     * @param apiKey Api key to authorize preprocessing tool.
     * @return Returns input text preprocessed.
     */
    public String cleanText(String text, String apiKey) throws IOException
    {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("text", text);
        String json = jsonObject.toString();

        RequestBody requestBody = RequestBody.create(
                json,
                MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-api-key", apiKey)
                .post(requestBody)
                .build();

        Response response = client.newCall(request).execute();

        String jsonResponse = response.body().string();
        return jsonResponse;
    }
}