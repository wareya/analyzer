import java.io.*;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * Licensed under a public domain‐like license. See Main.java for license text.
 */

public class ConsoleMain extends Main {
    private static String last_message = "";
    public static void main(String[] args)
    {
        try
        {
            //PrintStream(System.out, true, "UTF-8");
            out = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            System.out.println("Failed to open output as UTF-8.");
            return;
        }

        ArrayDeque<String> arguments = new ArrayDeque<>();
        arguments.addAll(Arrays.asList(args));

        if (arguments.peekFirst().matches("(-h)|(--help)"))
        {
            println(out, "Usage: java -jar analyzer.jar <corpus.txt> (-[fdswlpn] )*");
            println(out, "\tcorpus.txt: must be in utf-8. cannot be named \"-h\" or \"--help\".");
            println(out, "\t-f: disable user filters (userfilters.csv)");
            println(out, "\t-d: disable user dictionary (userdict.csv)");
            println(out, "\t-s: strip 〈〉 (but not their contents) and enable 《》 furigana culling (incl. contents) (operates at the code unit level, before parsing)");
            println(out, "\t-w: disable 'only in dictionary' filter");
            println(out, "\t-p: disable punctuation filter");
            println(out, "\t-k: words with kanji only");
            println(out, "\t-c: count lines and export index of the first line a term shows up in");
            println(out, "\t-l: pull out spellings to additional columns (merge respellings of words)");
            println(out, "\t-x: lexme mode (pull out spellings, pronunciations, and accents, overrides/replaces -l)");
            println(out, "\t-a: Append original line of the first time a term shows up in");
            println(out, "\t-r: Include furigana reading with sentence");
            println(out, "\t-rc: Also include cloze html tags to mark the keyword in the sentence");
            println(out, "\t-i<number>: index of sentence for TSV input, no space between -i and number");
            println(out, "\t-m<number>: deduplicate lines longer than this (negative to disable) (disabled by default)");
            println(out, "Options must be stated separately (-p -d), not bundled (-pd)");
            println(out, "");
            println(out, "Output goes to standard output. Use > to output to a file.");
        }
        else
        {
            String filename = arguments.removeFirst();

            while(!(arguments.size() == 0))
            {
                String argument = arguments.removeFirst();
                if(argument.equals("-p")) filter_punctuation_enabled = false;
                if(argument.equals("-w")) filter_dictionary_enabled = false;
                if(argument.equals("-f")) enable_userfilter = false;
                if(argument.equals("-d")) enable_userdictionary = false;
                if(argument.equals("-s")) skip_furigana_formatting = true;
                if(argument.equals("-k")) filter_kanji_only = true;
                if(argument.equals("-c")) enable_linecounter = true;
                if(argument.equals("-l")) pull_out_spellings = true;
                if(argument.equals("-x")) lexeme_only = true;
                if(argument.equals("-a")) enable_append_line = true;
                if(argument.equals("-r")) enable_sentence_reading = true;
                if(argument.equals("-rc")) enable_sentence_reading_cloze = true;
                if(argument.matches("^-i\\d+$"))
                {
                    Pattern pattern = Pattern.compile("^-i(\\d+)$");
                    Matcher matcher = pattern.matcher(argument);
                    if(matcher.find())
                        sentence_index = Integer.parseInt(matcher.group(1));
                }
                if(argument.matches("^-m\\d+$"))
                {
                    Pattern pattern = Pattern.compile("^-m(\\d+)$");
                    Matcher matcher = pattern.matcher(argument);
                    if(matcher.find())
                        deduplicate_longer_than = Integer.parseInt(matcher.group(1));
                }
            }
            try
            {
                run(filename, out, (text,length)->
                {
                    String mini_text = text.split(":")[0];
                    if(mini_text.equals(last_message))
                        System.err.printf("\r");
                    else
                        System.err.printf("\n");
                    if(length == 0.0)
                        System.err.printf(text);
                    else if(length > 0.0)
                        System.err.printf(text + " %.02f%%", length*100);
                    else if(!text.equals("Done"))
                        System.err.printf(text + " ...");
                    last_message = mini_text;
                });
            }
            catch(IOException e)
            { /**/ }
        }
        try
        {
            out.flush();
        }
        catch (IOException e) { /* */ }
    }
}
