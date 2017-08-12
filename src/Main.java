/*
This software is dual-licensed to the public domain and under the following
license: you are granted a perpetual, irrevocable license to copy, modify,
publish, and distribute this file as you see fit.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS
OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
THIS SOFTWARE.
 */

import com.atilika.kuromoji.unidic.kanaaccent.Token;
import com.atilika.kuromoji.unidic.kanaaccent.Tokenizer;
import com.atilika.kuromoji.util.StringUtils;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class FilterInfo {
    String word = "";
    String a = "";
    String b = "";
    String c = "";
    String d = "";
    int mode_word = 0;
    int mode_a = 0;
    int mode_b = 0;
    int mode_c = 0;
    int mode_d = 0;
}

public class Main
{
    public static void main(String[] args)
    {
        ArrayDeque<String> arguments = new ArrayDeque<>();
        arguments.addAll(Arrays.asList(args));
        if(arguments.size() == 0)
            GUIMain.main(args);
        else
            ConsoleMain.main(args);
    }

    // program state

    static boolean filter_dictionary_enabled = true;
    static boolean filter_punctuation_enabled = true;
    static boolean skip_furigana_formatting = false;
    static boolean enable_linecounter = false;
    static boolean enable_userfilter = true;
    static boolean enable_userdictionary = true;
    static boolean filter_kanji_only = false;
    static int sentence_index = -1;
    static boolean enable_append_line = false;
    static boolean enable_sentence_reading = false;

    static boolean pull_out_spellings = false;
    static boolean lexeme_only = false;

    // to force utf-8 output on windows
    static BufferedWriter out;

    private static Pattern p_re = Pattern.compile("^[\\p{Punct} 　─]*$", Pattern.UNICODE_CHARACTER_CLASS);
    private static Matcher p_m = p_re.matcher("");
    private static ArrayList<FilterInfo> filters = new ArrayList<>();
    private static HashSet<String> basic_filters = new HashSet<>();
    
    private static FilterInfo filter_builder(String str)
    {
        if(str == null || str.equals("")) return null;
        String[] parts = str.split(",");
        FilterInfo info = new FilterInfo();
        if(parts.length > 0)
        {
            info.word = parts[0];
            if(!info.word.equals(""))
                info.mode_word = 1;
        }
        if(parts.length > 1)
        {
            info.a = parts[1];
            if(!info.a.equals(""))
                info.mode_a = 1;
        }
        if(parts.length > 2)
        {
            info.b = parts[2];
            if(!info.b.equals(""))
                info.mode_b = 1;
        }
        if(parts.length > 3)
        {
            info.c = parts[3];
            if(!info.c.equals(""))
                info.mode_c = 1;
        }
        if(parts.length > 4)
        {
            info.d = parts[4];
            if(!info.d.equals(""))
                info.mode_d = 1;
        }
        return info;
    }
    private static void init_filter() throws IOException
    {
        InputStreamReader userfilters = new InputStreamReader(new FileInputStream("userfilters.csv"), "UTF-8");
        String line;
        while ((line = readline(userfilters, true)) != null)
        {
            FilterInfo filter = filter_builder(line);
            if(filter.mode_word == 1 &&
               filter.mode_a == 0 &&
               filter.mode_b == 0 &&
               filter.mode_c == 0 &&
               filter.mode_d == 0) 
                basic_filters.add(filter.word); // hashmap lookup is much faster than looping through an ArrayList
            else
                filters.add(filter);
        }
        userfilters.close();
    }
    
    private static boolean filtered(Token token)
    {
        // not in dictionary
        if(filter_dictionary_enabled && !token.isKnown() && !token.isUser()) return true;
        
        // is punctuation
        if(filter_punctuation_enabled && p_m.reset(token.getSurface()).find()) return true;

        if(filter_kanji_only && !token.getWrittenBaseForm().matches("[\\u4e00-\\u9faf]+.*")) return true;

        // undesirable term
        
        if(!enable_userfilter) return false;
        
        if(basic_filters.contains(token.getWrittenBaseForm()))
            return true;
        for(FilterInfo f : filters)
        {
            if(f == null) continue;
            boolean bad_word = true;
            if(f.mode_word == 0 && f.mode_a == 0 && f.mode_b == 0 && f.mode_c == 0 && f.mode_d == 0) continue;
            if(f.mode_word == 1 && !token.getWrittenBaseForm().equals(f.word)) bad_word = false;
            if(f.mode_a == 1 && !token.getPartOfSpeechLevel1().equals(f.a)) bad_word = false;
            if(f.mode_b == 1 && !token.getPartOfSpeechLevel2().equals(f.b)) bad_word = false;
            if(f.mode_c == 1 && !token.getPartOfSpeechLevel3().equals(f.c)) bad_word = false;
            if(f.mode_d == 1 && !token.getPartOfSpeechLevel4().equals(f.d)) bad_word = false;
            if(bad_word) return true;
        }
        
        return false;
    }
    private static String readline(InputStreamReader f, boolean noformat)
    {
        String line = "";
        boolean didanything = false;
        while (true)
        {
            try
            {
                int c = f.read();
                if(skip_furigana_formatting && c == 0x300A && !noformat) // 《
                {
                    while(true)
                    {
                        c = f.read();
                        if(c < 0) break;
                        if(c == 0x300B) // 》
                        {
                            c = f.read();
                            break;
                        }
                    }
                }
                if(skip_furigana_formatting && (c == 0x3008 || c == 0x3009 ) && !noformat) continue; //〈〉
                if(c < 0) break;
                line += (char)c;
                didanything = true;
                if(c == '\n')
                    break;
            }
            catch(IOException e)
            {
                break;
            }
        }
        if(didanything)
            return line.trim();
        else
            return null;
    }

    private static InputStream userdict = null;
    static void run(String in_name, BufferedWriter out, BiConsumer<String, Double> update) throws IOException, Exception
    {
        if(enable_userdictionary)
        {
            try
            {
                userdict = new FileInputStream("userdict.csv");
            }
            catch (IOException e)
            {
                userdict = null;
                update.accept("Failed to load user dictionary", -1.0);
                return;
            }
        }
        
        if(enable_userfilter)
        {
            update.accept("Loading user filter", -1.0);
            try
            {
                init_filter();
            }
            catch (UnsupportedEncodingException e)
            {
                update.accept("Failed to open userfilters.csv as UTF-8.", 0.0);
                return;
            }
            catch (IOException e)
            {
                update.accept("File access error occurred when initializing user filters.", 0.0);
                return;
            }
        }
        // first count the lines in the input file
        update.accept("Counting input lines", -1.0);

        Integer line_count = 0;
        InputStreamReader in;
        try
        {
            InputStreamReader test = new InputStreamReader(new FileInputStream(in_name), "UTF-8");
            while (readline(test, false) != null) line_count++;

            test.close();

            in = new InputStreamReader(new FileInputStream(in_name), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            update.accept("Failed to open input as UTF-8.", 0.0);
            return;
        }
        catch (IOException e)
        {
            update.accept("File access error occurred while counting lines.", 0.0);
            return;
        }

        miniFrequencyData data = new miniFrequencyData();

        Tokenizer tokenizer;
        if(userdict != null)
        {
            update.accept("Initializing kuromoji with user dictionary", -1.0);
            tokenizer = new Tokenizer.Builder().userDictionary(userdict).build();
        }
        else
        {
            update.accept("Initializing kuromoji without user dictionary", -10.0);
            tokenizer = new Tokenizer.Builder().build();
        }

        String line;
        Integer line_index = 0;

        while ((line = readline(in, false)) != null)
        {
            String text = line;
            if (sentence_index > -1) {
                String[] split = line.split("\\t");
                if (split.length > sentence_index) {
                    text = split[sentence_index];
                } else {
                    throw new Exception("Sentence index out of range");
                }
            }

            // update UI less often with very long input files
            if (line_count > 100000)
            {
                if(line_index % 491 == 0)
                    update.accept("Parsing file: " + line_index.toString() + "/" + line_count.toString(), line_index/(double)line_count);
            }
            else if (line_count > 10000)
            {
                if(line_index % 17 == 0)
                    update.accept("Parsing file: " + line_index.toString() + "/" + line_count.toString(), line_index/(double)line_count);
            }
            else
                update.accept("Parsing file: " + line_index.toString() + "/" + line_count.toString(), line_index/(double)line_count);
            List<Token> tokens = tokenizer.tokenize(text);
            for (Token token : tokens)
            {
                // skip undesired terms

                if(filtered(token)) continue;
                
                // record event

                String parts = token.getPartOfSpeechLevel1()+"\t"+token.getPartOfSpeechLevel2()+"\t"+token.getPartOfSpeechLevel3();

                String[] temp = {token.getWrittenBaseForm(), token.getFormBase(), token.getPronunciationBaseForm(), token.getAccentType(), token.getLanguageType(), parts, token.getConjugationType(), token.getLemma(), token.getLemmaReadingForm()};
                String identity = StringUtils.join(temp,"\t");

                int eventLineIndex = -1;
                if(enable_linecounter)
                    eventLineIndex = line_index;

                List<String> extraFieldsList = new ArrayList<String>();

                if(enable_sentence_reading) {
                    StringBuilder cloze = new StringBuilder();

                    for (Token clozeToken : tokens)
                    {
                        StringBuilder word = new StringBuilder();
                        boolean isCurrentToken = token.getSurface() == clozeToken.getSurface();
                        if (isCurrentToken) {
                            word.append("<span class=\"cloze\">");
                        }
                        word.append(Utils.toFurigana(clozeToken));
                        if (isCurrentToken) {
                            word.append("</span>");
                        }

                        cloze.append(word);
                    }
                    extraFieldsList.add(cloze.toString());
                }

                if(enable_append_line) {
                    extraFieldsList.add(line);
                }

                StringJoiner extraFields = new StringJoiner("\t");
                extraFieldsList.forEach(extraField -> extraFields.add(extraField));

                data.addEvent(identity, eventLineIndex, extraFields.toString());
            }
            line_index++;
        }
        update.accept("Writing output", 1.0);
        for(Fact fact : data.getSortedFrequencyList())
            println(out, fact.count+"\t"+fact.id);
        update.accept("Done", -1.0);

        try
        {
            in.close();
        }
        catch (IOException e)
        {
            update.accept("File access error occurred while counting lines.", 0.0);
        }
        
        if(userdict != null)
        {
            userdict.close();
            userdict = null;
        }
    }

    static void println(BufferedWriter output, String text)
    {
        try
        {
            output.write(text+"\n");
        }
        catch (IOException e) { /* */ }
    }
}
