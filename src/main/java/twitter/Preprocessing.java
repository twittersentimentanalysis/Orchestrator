package twitter;

import com.vdurmont.emoji.*;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Class to preprocess tweet data before applying sentiment analysis.
 *
 * @author Ariadna de Arriba
 */
public class Preprocessing
{
    private static ArrayList<String> stopwords          = new ArrayList<>();
    private static Map<String, String> emojis           = new HashMap<>();
    private static Map<String, String> abbreviations    = new HashMap<>();

    /**
     * Main function to run the orchestrator.
     *
     * @param text Text to apply preprocessing.
     * @return Returns a string with cleaned text.
     */
    public String cleanText (String text)
    {
        text = text.toLowerCase();

        text = text.replace("\\n", " ");
        text = text.replace("\\t", " ");
        text = text.replace("\\", " ");

        text = removeUrl(text);
        text = removeMention(text);
        text = removeNumbers(text);
        text = removeSpecialCharacters(text);
        text = removeRepeatedCharacters(text);

        text = replaceEmojis(text);
        text = replaceAbbreviations(text);

        text = removeStopwords(text);
        text = removeTwoCharacterWords(text);
        text = removeSpaces(text);

        return text;
    }

    /** Remove any url from a string.
     *
     * @param text Text to remove url.
     * @return Returns text with url removed.
     */
    private String removeUrl(String text)
    {
        // rid of ? and & in urls since replaceAll can't deal with them
        text = text.replaceAll("\\?", " ").replaceAll("\\&", " ");

        String urlPattern = "((https?|ftp|gopher|telnet|file|Unsure|http):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern p = Pattern.compile(urlPattern,Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        int i = 0;
        while (m.find())
        {
            text = text.replaceAll(m.group(i).replaceAll("\\?", " ").replaceAll("\\&", " ")," ").trim();
            i++;
        }
        return text;
    }

    /** Remove any twitter mention (@xxxx) from a string.
     *
     * @param text Text to remove mention.
     * @return Returns text with mention removed.
     */
    private String removeMention(String text)
    {
        /*int mention = text.indexOf("@");

        while (mention != -1)
        {
            text = text.replace(text.substring(mention).split(" ")[0], "");
            mention = text.indexOf("@");
        }*/

        text = text.replaceAll("@([A-Za-z0-9_]+)", " ");

        return text;
    }

    /** Remove any number from a string.
     *
     * @param text Text to remove numbers.
     * @return Returns text with numbers removed.
     */
    private String removeNumbers(String text)
    {
        text = text.replaceAll("[0-9]", " ");
        return text;
    }

    /** Remove any special character from a string.
     *
     * @param text Text to remove special characters.
     * @return Returns text with special characters removed.
     */
    private String removeSpecialCharacters(String text)
    {
        text = text.replace(".", " ");
        text = text.replace("/", " ");
        text = text.replace("-", " ");
        text = text.replace(";", " ");
        text = text.replace("!", " ");
        text = text.replace(":", " ");
        text = text.replace("¿", " ");
        text = text.replace("?", " ");
        text = text.replace(",", " ");
        text = text.replace("(", " ");
        text = text.replace(")", " ");
        text = text.replace("[", " ");
        text = text.replace("]", " ");
        text = text.replace("\"", " ");
        text = text.replace("'", " ");
        text = text.replace("#", " ");
        text = text.replace("&", " ");
        text = text.replace("=", " ");
        text = text.replace("|", " ");
        text = text.replace(">", " ");
        text = text.replace("<", " ");
        text = text.replace("*", " ");
        text = text.replace("_", " ");
        text = text.replace("%", " ");
        text = text.replace("@", " ");
        text = text.replace("\\", " ");
        text = text.replace("{", " ");
        text = text.replace("}", " ");
        text = text.replace("^", " ");
        text = text.replace("$", " ");
        text = text.replace("+", " ");
        text = text.replace("*", " ");
        text = text.replace("~", " ");
        text = text.replace("`", " ");
        text = StringUtils.stripAccents(text);
        return text;
    }

    /** Remove repeated characters from a word on a string.
     *
     * @param text Text to remove repeated characters.
     * @return Returns text with repeated characters removed.
     */
    private String removeRepeatedCharacters(String text)
    {
        text = text.replaceAll ("(\\w)\\1{2,}", "$1");
        return text;
    }

    /** Read csv file with emojis and their text representation.
     */
    private void readEmojis()
    {
        try
        {
            try (BufferedReader arq = new BufferedReader(new FileReader("data/preprocessing/emojis.csv")))
            {
                while(arq.ready())
                {
                    String line = arq.readLine();
                    line = line.toLowerCase();

                    String[] values = line.split(",");
                    emojis.put(values[0], values[6]);
                }
            }
        } catch(IOException e)
        {
            System.out.println("Error reading emojis");
            System.exit(0);
        }
    }

    /** Replace any emoji for its text representation from a string.
     *
     * @param text Text to replace emojis.
     * @return Returns text with emojis replaced.
     */
    private String replaceEmojis(String text)
    {
        readEmojis();
        List<String> textEmojis = EmojiParser.extractEmojis(text);

        for (String emoji : textEmojis)
        {
            text = text.replace(emoji, emojis.get(emoji));
        }

        return text;
    }

    /** Read txt file with abbreviations and their meaning.
     */
    private void readAbbreviations()
    {
        try
        {
            try (BufferedReader arq = new BufferedReader(new FileReader("data/preprocessing/abbreviations.txt")))
            {
                while(arq.ready())
                {
                    String line = arq.readLine();
                    line = line.toLowerCase();

                    String[] values = line.split(",");
                    abbreviations.put(values[0], values[1]);
                }
            }
        } catch(IOException e)
        {
            System.out.println("Error reading abbreviations");
            System.exit(0);
        }
    }

    /** Replace any spanish abbreviation for its meaning from a string.
     *
     * @param text Text to replace abbreviations.
     * @return Returns text with abbreviations replaced.
     */
    private String replaceAbbreviations(String text)
    {
        readAbbreviations();

        for (String key : abbreviations.keySet())
        {
            if (text.matches(".*\\b" + key + "\\b.*"))
            {
                text = text.replaceAll("\\b" + key + "\\b", abbreviations.get(key));
            }
        }

        return text;
    }

    /** Read txt file with spanish stopwords.
     */
    private void readStopwords()
    {
        try
        {
            try (BufferedReader arq = new BufferedReader(new FileReader("data/preprocessing/stopwords.txt")))
            {
                while(arq.ready())
                {
                    String line = arq.readLine();
                    line = line.toLowerCase();
                    stopwords.add(line);
                }
            }
        } catch(IOException e)
        {
            System.out.println("Error reading stopwords");
            System.exit(0);
        }
    }

    /** Remove any spanish stopword from a string.
     *
     * @param text Text to remove spanish stopwords.
     * @return Returns text with spanish stopwords removed.
     */
    private String removeStopwords(String text)
    {
        readStopwords();

        for (String key : stopwords)
        {
            if (text.matches(".*\\b" + key + "\\b.*"))
            {
                text = text.replaceAll("\\b" + key + "\\b", " ");
            }
        }

        return text;
    }

    /** Remove any word with less than two characters from a string.
     *
     * @param text Text to remove words with two or less characters.
     * @return Returns text with two or less characters removed.
     */
    private String removeTwoCharacterWords(String text)
    {
        text = text.replaceAll("\\b[a-z]{1,2}\\b", " ");

        return text;
    }

    /** Remove any extra space from a string.
     *
     * @param text Text to remove extra spaces.
     * @return Returns text with extra spaces removed.
     */
    private String removeSpaces(String text)
    {
        text = text.replaceAll("\\s{2,}", " ");
        return text;
    }
}