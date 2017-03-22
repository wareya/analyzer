import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wareya on 2017/03/19.
 */

class miniAlternativeFact
{
    miniAlternativeFact(Integer count, String id)
    {
        this.count = count;
        this.id = id;
    }
    Integer count;
    String id;
}

public class miniFrequencyData
{
    private HashMap<String, Integer> frequency = new HashMap<>();
    private HashMap<String, Integer> location = new HashMap<>();
    void addEvent(String id, Integer extra)
    {
        if(frequency.containsKey(id))
            frequency.replace(id, frequency.get(id)+1);
        else
        {
            frequency.put(id, 1);
            location.put(id, extra);
        }
    }

    ArrayList<miniAlternativeFact> getSortedFrequencyList()
    {
        ArrayList<miniAlternativeFact> mapping = new ArrayList<>();
        for(HashMap.Entry<String, Integer> v : frequency.entrySet())
        {
            Integer frequency = v.getValue();
            String identity = v.getKey();
            Integer location = this.location.get(v.getKey());

            if(location >= 0)
                mapping.add(new miniAlternativeFact(frequency, identity+"\t"+location.toString()));
            else
                mapping.add(new miniAlternativeFact(frequency, identity));
        }

        mapping.sort((a, b) -> b.count - a.count);
        return mapping;
    }
}