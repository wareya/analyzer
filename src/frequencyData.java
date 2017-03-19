import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by wareya on 2017/03/18.
*/


class alternativeFact
{
    alternativeFact(Integer count, String data, String id)
    {
        this.count = count;
        this.data = data;
        this.id = id;
    }
    Integer count;
    String data;
    String id;
}

public class frequencyData
{
    private HashMap<String, Integer> frequency = new HashMap<>();
    private HashMap<String, String> namings = new HashMap<>();
    void addEvent(String id, String data)
    {
        if(frequency.containsKey(id))
            frequency.replace(id, frequency.get(id)+1);
        else
            frequency.put(id, 1);
        if(!namings.containsKey(id))
            namings.put(id, data);
    }

    ArrayList<alternativeFact> getSortedFrequencyList()
    {
        ArrayList<alternativeFact> mapping = new ArrayList<>();
        for(HashMap.Entry<String, Integer> v : frequency.entrySet())
            mapping.add(new alternativeFact(v.getValue(), namings.get(v.getKey()), v.getKey()));

        mapping.sort((a, b) -> b.count - a.count);
        return mapping;
    }
}
