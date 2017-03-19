/**
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
    static HashSet<String> blacklist;
    private static void init_blacklist()
    {
        blacklist.add("ギー");
    }
    private static boolean blacklisted(String test)
    {

        return false;
    }
    private static String readline(InputStreamReader f)
    {
        String line = "";
        boolean didanything = false;
        while (true)
        {
            try
            {
                int c = f.read();
                //if(c < 0x20) break;
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
        {
            return line.trim();
        }
        else
            return null;
    }
    private static PrintStream out;
    public static void main(String[] args) throws UnsupportedEncodingException
    {
        out = new PrintStream(System.out, true, "UTF-8");

        Pattern id_re = Pattern.compile("wordId=([0-9]*)");
        Matcher id_m = id_re.matcher("");

        Pattern p_re = Pattern.compile("^[\\p{Punct} 　─]*$", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher p_m = p_re.matcher("");

        miniFrequencyData data = new miniFrequencyData();

        Tokenizer tokenizer = new Tokenizer.Builder()
            //.mode(Tokenizer.Mode.NORMAL)
            //.kanjiPenalty(3, 10000)
            //.otherPenalty(Integer.MAX_VALUE, 0)
            .build();

        InputStreamReader in;
        try
        {
            in = new InputStreamReader(new FileInputStream(args[0]), "UTF-8");
        }
        catch (FileNotFoundException e)
        {
            out.println("File not found.");
            return;
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            out.println("No file given");
            return;
        }

        String line;
        while ((line = readline(in)) != null)
        {
            //out.println(line);
            List<Token> tokens = tokenizer.tokenize(line);
            for (Token token : tokens)
            {
                // skip undesired terms

                if(!token.isKnown()) continue; // not from the dictionary
                if(p_m.reset(token.getSurface()).find()) continue; // punctuation
                //if(!id_m.reset(token.toString()).find()) continue; // failed to find numeric id

                if(token.getPartOfSpeechLevel1().contains("助詞")) continue; // particles
                if(token.getPartOfSpeechLevel1().contains("助動詞")) continue; // strict auxiliary verbs (ます, だ, etc)
                if(token.getPartOfSpeechLevel1().contains("感動詞")) continue; // interjection
                if(token.getPartOfSpeechLevel1().contains("接続詞")) continue; // conjunctions
                if(token.getPartOfSpeechLevel2().contains("固有名詞")) continue; // proper nouns
                if(token.getPartOfSpeechLevel1().contains("フィラー")) continue; // filler ejections
                if(token.getPartOfSpeechLevel1().contains("その他")) continue; // "other"
                if(token.getPartOfSpeechLevel1().contains("記号")) continue; // symbols
                if(token.getPartOfSpeechLevel1().contains("記号")) continue; // symbols

                String parts = token.getPartOfSpeechLevel1()+"\t"+token.getPartOfSpeechLevel2()+"\t"+token.getPartOfSpeechLevel3();//+"\t"+token.getPartOfSpeechLevel4() // 4 doesn't seem to be used in unidic
                if(parts.contains("形容詞	非自立")) continue; // dependent i-adjectives (いい from ていい etc)
                if(parts.contains("形容詞	接尾")) continue; // abnormal adjectives (ぽい etc)
                if(parts.contains("動詞	非自立")) continue; // dependent verbs (やる from てやる etc)
                if(parts.contains("動詞	接尾")) continue; // abnormal verbs (られる etc)

                // Old code to keep in VCS
                //String id = id_m.group(1);
                //String[] temp = {parts, token.getLemma(), token.getLemmaReadingForm(), token.getPronunciation(), token.getPronunciationBaseForm(), token.getWrittenForm(), token.getWrittenBaseForm(), token.getLanguageType()};
                //data.addEvent(id, token.getAllFeatures());
                //data.addEvent(id, identity);

                //Kana spelling   Kana pronunciation  Actual pronunciation   Term
                //イウ            ユウ                ユー                   いう
                //ケッコウ        ケッコウ            ケッコー               結構
                //トウ            トウ                トウ                   問う
                //getKanaBase(), getFormBase(), getPronunciationBaseForm(), getWrittenBaseForm()
                //everything else is affected in conjugation
                String[] temp = {parts, token.getWrittenBaseForm(), token.getFormBase(), token.getPronunciationBaseForm(), token.getAccentType(), token.getConjugationType(), token.getLanguageType()};
                String identity = StringUtils.join(temp,"\t");
                data.addEvent(identity);
            }
        }
        for(miniAlternativeFact fact : data.getSortedFrequencyList())
        {
            //out.println(fact.count+"\t"+fact.id+"\t"+fact.data);
            out.println(fact.count+"\t"+fact.id);
        }
    }
}
