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
    void addEvent(String id)
    {
        if(frequency.containsKey(id))
            frequency.replace(id, frequency.get(id)+1);
        else
            frequency.put(id, 1);
    }

    ArrayList<miniAlternativeFact> getSortedFrequencyList()
    {
        ArrayList<miniAlternativeFact> mapping = new ArrayList<>();
        for(HashMap.Entry<String, Integer> v : frequency.entrySet())
            mapping.add(new miniAlternativeFact(v.getValue(), v.getKey()));

        mapping.sort((a, b) -> b.count - a.count);
        return mapping;
    }
}