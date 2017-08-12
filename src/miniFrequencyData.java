import com.sun.deploy.util.ArrayUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/*
 * Licensed under a public domain‐like license. See Main.java for license text.
 */

class Fact
{
    Fact(Integer count, String id)
    {
        this.count = count;
        this.id = id;
    }
    Integer count;
    String id;
}

class miniFrequencyData
{
    private HashMap<String, Integer> frequency = new HashMap<>();
    private HashMap<String, HashMap<String, Integer>> spellings = new HashMap<>();
    private HashMap<String, Integer> location = new HashMap<>();
    private HashMap<String, String> line = new HashMap<>();

    void addEvent(String id, Integer extra, String originalLine)
    {
        String name;
        String identity;
        if(Main.pull_out_spellings)
        {
            name = id.split("\t", 2)[0];
            identity = id.split("\t", 2)[1];
        }
        else if(Main.lexeme_only)
        {
            name = String.join("\t", Arrays.copyOf(id.split("\t", 5),4));
            identity = id.split("\t", 5)[4];
        }
        else
        {
            name = "";
            identity = id;
        }
        if(frequency.containsKey(identity))
            frequency.replace(identity, frequency.get(identity)+1);
        else
        {
            frequency.put(identity, 1);
            location.put(identity, extra);
            line.put(identity, originalLine);

        }
        
        if(Main.pull_out_spellings || Main.lexeme_only)
        {
            if(spellings.containsKey(identity))
            {
                HashMap<String, Integer> entry = spellings.get(identity);
                if(entry.containsKey(name))
                    entry.replace(name, entry.get(name)+1);
                else
                    entry.put(name, 1);
            }
            else
            {
                HashMap<String, Integer> entry = new HashMap<>();
                entry.put(name, 1);
                spellings.put(identity, entry);
            }
        }
    }

    ArrayList<Fact> getSortedFrequencyList()
    {
        ArrayList<Fact> mapping = new ArrayList<>();
        for(HashMap.Entry<String, Integer> v : frequency.entrySet())
        {
            Integer frequency = v.getValue();
            String identity = v.getKey();
            Integer location = this.location.get(v.getKey());
            String line = this.line.get(v.getKey());
            
            Fact fact = new Fact(frequency, identity);

            if(location >= 0)
                fact.id += "\t"+location.toString();

            if(line != null)
                fact.id += "\t"+line;
            
            if(Main.pull_out_spellings || Main.lexeme_only)
            {
                HashMap<String, Integer> my_spellings = spellings.get(identity);
                ArrayList<Fact> my_sorted_spellings = new ArrayList<>();
                
                for(HashMap.Entry<String, Integer> v2 : my_spellings.entrySet()) 
                    my_sorted_spellings.add(new Fact(v2.getValue(), v2.getKey()));
                
                my_sorted_spellings.sort((a, b) -> b.count - a.count);
                
                for(Fact f : my_sorted_spellings)
                    fact.id += "\t" + f.id + "\t" + f.count;
            }
            
            mapping.add(fact);
            
            //spellings.get(identity).entrySet().stream().sorted(Comparator.comparingInt(e -> e.getValue())).forEach(e -> fact.id += "\t" + e.getKey() + "\t" + e.getValue());
        }

        mapping.sort((a, b) -> b.count - a.count);
        return mapping;
    }
}