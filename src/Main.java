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

    static boolean blacklist_enabled = true;
    static boolean filter_dictionary_enabled = true;
    static boolean filter_type_enabled = true;
    static boolean filter_punctuation_enabled = true;
    static boolean special_blacklist_enabled = false;
    static boolean skip_furigana_formatting = false;
    static boolean enable_linecounter = false;

    // to force utf-8 output on windows
    static BufferedWriter out;

    private static Pattern p_re = Pattern.compile("^[\\p{Punct} 　─]*$", Pattern.UNICODE_CHARACTER_CLASS);
    private static Matcher p_m = p_re.matcher("");
    private static HashSet<String> blacklist = new HashSet<>();
    private static HashSet<String> filter_a = new HashSet<>(); // first PoS term
    private static HashSet<String> filter_b = new HashSet<>(); // second PoS term
    private static HashSet<String> filter_c = new HashSet<>(); // combined first+second PoS term

    private static void init_filter()
    {
        // not all of these work on the unidic tags, but some of them do, and the broken ones will work for the ipadic tags if you end up using ipadic for some reason
        filter_a.add("助詞");
        filter_a.add("助動詞");
        filter_a.add("感動詞");
        filter_a.add("接続詞");
        filter_a.add("固有名詞");
        filter_a.add("フィラー");
        filter_a.add("その他");
        filter_a.add("記号");

        filter_b.add("固有名詞");

        filter_c.add("形容詞	非自立");
        filter_c.add("形容詞	接尾");
        filter_c.add("動詞	非自立");
        filter_c.add("動詞	接尾");
    }
    private static boolean filtered(Token token)
    {
        // not in dictionary
        if(filter_dictionary_enabled && !token.isKnown()) return true;
        // is punctuation
        if(filter_punctuation_enabled && p_m.reset(token.getSurface()).find()) return true;
        // undesirable term
        if(!filter_type_enabled) return false;
        for(String s : filter_a)
            if(token.getPartOfSpeechLevel1().contains(s)) return true;
        for(String s : filter_b)
            if(token.getPartOfSpeechLevel1().contains(s)) return true;
        for(String s : filter_c)
            if((token.getPartOfSpeechLevel1()+"\t"+token.getPartOfSpeechLevel2()+"\t"+token.getPartOfSpeechLevel3()).contains(s)) return true;
        return false;
    }
    private static void init_blacklist()
    {
        if(blacklist_enabled)
        {
        // weekdays
            blacklist.add("日曜");
            blacklist.add("月曜");
            blacklist.add("火曜");
            blacklist.add("水曜");
            blacklist.add("木曜");
            blacklist.add("金曜");
            blacklist.add("土曜");
            blacklist.add("日曜日");
            blacklist.add("月曜日");
            blacklist.add("火曜日");
            blacklist.add("水曜日");
            blacklist.add("木曜日");
            blacklist.add("金曜日");
            blacklist.add("土曜日");
        // months
            blacklist.add("一月");
            blacklist.add("二月");
            blacklist.add("三月");
            blacklist.add("四月");
            blacklist.add("五月");
            blacklist.add("六月");
            blacklist.add("七月");
            blacklist.add("八月");
            blacklist.add("九月");
            blacklist.add("十月");
            blacklist.add("十一月");
            blacklist.add("十二月");
        // monthdays
            blacklist.add("一日");
            blacklist.add("二日");
            blacklist.add("三日");
            blacklist.add("四日");
            blacklist.add("五日");
            blacklist.add("六日");
            blacklist.add("七日");
            blacklist.add("八日");
            blacklist.add("九日");
            blacklist.add("十日");
            blacklist.add("十一日");
            blacklist.add("十二日");
            blacklist.add("十三日");
            blacklist.add("十四日");
            blacklist.add("十五日");
            blacklist.add("十六日");
            blacklist.add("十七日");
            blacklist.add("十八日");
            blacklist.add("十九日");
            blacklist.add("二十日");
            blacklist.add("二十一日");
            blacklist.add("二十二日");
            blacklist.add("二十三日");
            blacklist.add("二十四日");
            blacklist.add("二十五日");
            blacklist.add("二十六日");
            blacklist.add("二十七日");
            blacklist.add("二十八日");
            blacklist.add("二十九日");
            blacklist.add("三十 日");
            blacklist.add("三十一日");
        // numbers
            blacklist.add("0");
            blacklist.add("1");
            blacklist.add("2");
            blacklist.add("3");
            blacklist.add("4");
            blacklist.add("5");
            blacklist.add("6");
            blacklist.add("7");
            blacklist.add("8");
            blacklist.add("9");
            blacklist.add("０");
            blacklist.add("１");
            blacklist.add("２");
            blacklist.add("３");
            blacklist.add("４");
            blacklist.add("５");
            blacklist.add("６");
            blacklist.add("７");
            blacklist.add("８");
            blacklist.add("９");
            blacklist.add("〇");
            blacklist.add("一");
            blacklist.add("二");
            blacklist.add("三");
            blacklist.add("四");
            blacklist.add("五");
            blacklist.add("六");
            blacklist.add("七");
            blacklist.add("八");
            blacklist.add("九");
            blacklist.add("十"); // 10
            blacklist.add("百"); // 100
            blacklist.add("千"); // 1000
            blacklist.add("万"); // 10000 (myriad)
            blacklist.add("億"); // myriad^2
            //blacklist.add("兆"); // myriad^3 // same as a normal common word
            //blacklist.add("京"); // myriad^4 // same as a normal common word
            blacklist.add("垓"); // myriad^5
        // basic counters
            blacklist.add("一つ");
            blacklist.add("二つ");
            blacklist.add("三つ");
            blacklist.add("四つ");
            blacklist.add("五つ");
            blacklist.add("六つ");
            blacklist.add("七つ");
            blacklist.add("八つ");
            blacklist.add("九つ");
        }

        if(special_blacklist_enabled)
        {
        // VN-specific names
            // ingarock
            blacklist.add("ギー");
            blacklist.add("ドクター");
            // magicha
            blacklist.add("謳歌");
            // fate
            blacklist.add("セイバー");
            blacklist.add("マスター");
            blacklist.add("キャスター");
            blacklist.add("ライダー");
            blacklist.add("ランサー");
            blacklist.add("アサシン");
            // hoshimemo
            blacklist.add("ヒバリ");
        // jargon
            blacklist.add("スクワッター");
            blacklist.add("聖杯");
            blacklist.add("機関");
            blacklist.add("望遠");
            blacklist.add("クリッター");
            blacklist.add("オルゴール");
            blacklist.add("マグ");
            blacklist.add("スワスチカ");
            blacklist.add("プラネタリウム");
            blacklist.add("アルパカ");
            blacklist.add("メニュー");
            blacklist.add("ミスター");
        // parsing mysteries
            blacklist.add("チャー");
            blacklist.add("カー");
            blacklist.add("クル");
            blacklist.add("クル");
            blacklist.add("師");
            blacklist.add("洋");
            blacklist.add("師");
            blacklist.add("崎");
            blacklist.add("ベイ");
            blacklist.add("トリフ");
        // special particles
            blacklist.add("お");
            blacklist.add("ご");
            blacklist.add("御");
            blacklist.add("さん");
            blacklist.add("ちゃん");
            blacklist.add("たん");
            blacklist.add("くん");
            blacklist.add("そっ");
            blacklist.add("ふっ");
            blacklist.add("ぽかっ");
        }
    }
    private static boolean blacklisted(String test)
    {
        return blacklist.contains(test.trim());
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
                if(skip_furigana_formatting && c == 0x300A) // 《
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
                if(skip_furigana_formatting && (c == 0x3008 || c == 0x3009 )) continue; //〈〉
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

    static void run(String in_name, BufferedWriter out, BiConsumer<String, Double> update)
    {
        // first count the lines in the input file
        update.accept("Counting input lines", -1.0);

        Integer line_count = 0;
        InputStreamReader in;
        try
        {
            InputStreamReader test = new InputStreamReader(new FileInputStream(in_name), "UTF-8");
            while (readline(test) != null) line_count++;

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
            update.accept("File access error occured while counting lines.", 0.0);
            return;
        }

        miniFrequencyData data = new miniFrequencyData();

        init_filter();
        init_blacklist();

        Tokenizer tokenizer = new Tokenizer.Builder().build();

        String line;
        Integer line_index = 0;

        while ((line = readline(in)) != null)
        {
            update.accept("Parsing file: " + line_index.toString() + "/" + line_count.toString(), line_index/(double)line_count);
            List<Token> tokens = tokenizer.tokenize(line);
            for (Token token : tokens)
            {
                // skip undesired terms

                if(filtered(token)) continue;
                if(blacklisted(token.getWrittenBaseForm())) continue;

                // record event

                String parts = token.getPartOfSpeechLevel1()+"\t"+token.getPartOfSpeechLevel2()+"\t"+token.getPartOfSpeechLevel3();

                String[] temp = {token.getWrittenBaseForm(), token.getFormBase(), token.getPronunciationBaseForm(), token.getAccentType(), token.getLanguageType(), parts, token.getConjugationType()};
                String identity = StringUtils.join(temp,"\t");
                if(enable_linecounter)
                    data.addEvent(identity, line_index);
                else
                    data.addEvent(identity, -1);
            }
            line_index++;
        }
        update.accept("Writing output", 1.0);
        for(miniAlternativeFact fact : data.getSortedFrequencyList())
            println(out, fact.count+"\t"+fact.id);
        update.accept("Done", -1.0);

        try
        {
            in.close();
        }
        catch (IOException e)
        {
            update.accept("File access error occured while counting lines.", 0.0);
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
