package slide;

import java.util.ArrayList;
import java.util.HashMap;

public class CountHashMap<E> extends HashMap<E, Integer>{
    ArrayList<E> keyArray = new ArrayList<E>();
    public void add(E key){add(key, 1);}
    public void add(E key, Integer value){
        if(containsKey(key)){value += get(key);
        }else{keyArray.add(key);}
        put(key, value);
    }
}