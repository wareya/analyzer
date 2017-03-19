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
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
    // to force utf-8 output on windows
    private static PrintStream out;

    // disable to make "academic" frequency lists (keep enabled for flashcards)
    private static boolean blacklist_enabled = true;
    private static HashSet<String> blacklist = new HashSet<>();

    private static void init_blacklist()
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
        blacklist.add("兆"); // myriad^3
        blacklist.add("京"); // myriad^4
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

        // hoshimemo
        blacklist.add("ヒバリ");
    // jargon
        blacklist.add("スクワッター");
        blacklist.add("聖杯");
        blacklist.add("機関");
        blacklist.add("望遠");
    // parsing mysteries
        blacklist.add("チャー");
        blacklist.add("師");
        blacklist.add("洋");
        blacklist.add("師");
        blacklist.add("崎");
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
        blacklist.add("ぽかっ  ");

    }
    private static boolean blacklisted(String test)
    {
        return blacklist_enabled && (blacklist.contains(test.trim()));
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
    public static void main(String[] args) throws UnsupportedEncodingException
    {
        out = new PrintStream(System.out, true, "UTF-8");

        Pattern p_re = Pattern.compile("^[\\p{Punct} 　─]*$", Pattern.UNICODE_CHARACTER_CLASS);
        Matcher p_m = p_re.matcher("");

        miniFrequencyData data = new miniFrequencyData();

        init_blacklist();
        Tokenizer tokenizer = new Tokenizer.Builder().build();

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

                // not all of these work on the unidic tags, but some of them do, and the broken ones will work for the ipadic tags if you end up using ipadic for some reason
                if(token.getPartOfSpeechLevel1().contains("助詞")) continue; // particles
                if(token.getPartOfSpeechLevel1().contains("助動詞")) continue; // strict auxiliary verbs (ます, だ, etc)
                if(token.getPartOfSpeechLevel1().contains("感動詞")) continue; // interjection
                if(token.getPartOfSpeechLevel1().contains("接続詞")) continue; // conjunctions
                if(token.getPartOfSpeechLevel2().contains("固有名詞")) continue; // proper nouns
                if(token.getPartOfSpeechLevel1().contains("フィラー")) continue; // filler ejections
                if(token.getPartOfSpeechLevel1().contains("その他")) continue; // "other"
                if(token.getPartOfSpeechLevel1().contains("記号")) continue; // symbols
                if(token.getPartOfSpeechLevel1().contains("記号")) continue; // symbols

                String parts = token.getPartOfSpeechLevel1()+"\t"+token.getPartOfSpeechLevel2()+"\t"+token.getPartOfSpeechLevel3();
                if(parts.contains("形容詞	非自立")) continue; // dependent i-adjectives (いい from ていい etc)
                if(parts.contains("形容詞	接尾")) continue; // abnormal adjectives (ぽい etc)
                if(parts.contains("動詞	非自立")) continue; // dependent verbs (やる from てやる etc)
                if(parts.contains("動詞	接尾")) continue; // abnormal verbs (られる etc)

                if(blacklisted(token.getWrittenBaseForm())) continue;

                //Kana spelling   Kana pronunciation  Actual pronunciation   Term
                //イウ            ユウ                ユー                   いう
                //ケッコウ        ケッコウ            ケッコー               結構
                //トウ            トウ                トウ                   問う
                //getKanaBase(), getFormBase(), getPronunciationBaseForm(), getWrittenBaseForm()
                //everything else is affected in conjugation

                // record event

                String[] temp = {parts, token.getWrittenBaseForm(), token.getFormBase(), token.getPronunciationBaseForm(), token.getAccentType(), token.getConjugationType(), token.getLanguageType()};
                String identity = StringUtils.join(temp,"\t");
                data.addEvent(identity);
            }
        }
        for(miniAlternativeFact fact : data.getSortedFrequencyList())
            out.println(fact.count+"\t"+fact.id);
    }
}
