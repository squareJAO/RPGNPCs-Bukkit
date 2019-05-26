package rpg_npcs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;

public class WeightedSet <T extends Object> {
    private class Entry {
    	int accumulatedWeight;
        T object;
    }

    private List<Entry> entries = new ArrayList<>();
    private int accumulatedWeight;
    private Random rand = new Random();

    public void addEntry(T object, int weight) {
        accumulatedWeight += weight;
        Entry e = new Entry();
        e.object = object;
        e.accumulatedWeight = accumulatedWeight;
        entries.add(e);
    }
    
    public <Q extends Object> WeightedSet<Q> zip(Map<T, Q> map) {
    	WeightedSet<Q> newWeightedSet = new WeightedSet<Q>();
    	
    	// Copy over weights
    	newWeightedSet.accumulatedWeight = accumulatedWeight;
    	newWeightedSet.rand = rand;
    	
    	// Map entries
    	for (Entry entry : entries) {
    		Q newObject = map.get(entry.object);
    		
    		if (newObject == null) {
    			Bukkit.getLogger().info("Mapping does not contain " + entry.object.toString());
			}
    		
    		WeightedSet<Q>.Entry newEntry = newWeightedSet.new Entry();
    		newEntry.object = newObject;
    		newEntry.accumulatedWeight = entry.accumulatedWeight;
    		newWeightedSet.entries.add(newEntry);
		}
    	
    	return newWeightedSet;
    }
    
    public int size() {
    	return entries.size();
    }

    public T getRandom() {
    	double targetWeight = rand.nextDouble() * accumulatedWeight * 0.99999;
        
        return findEntryWithWeight(targetWeight);
    }
    
    private T findEntryWithWeight(double targetWeight) {
    	return findEntryWithWeight(targetWeight, 0, entries.size() - 1);
    }
    
    private T findEntryWithWeight(double targetWeight, int start, int end) {
    	// Binary search
    	
    	if (start > end) {
			return null;
		}
    	
    	int middle = (start + end) / 2;
    	
    	int prevWeight = (middle - 1) < 0 ? 0 : entries.get(middle - 1).accumulatedWeight;
    	
    	if (targetWeight < prevWeight) {
			return findEntryWithWeight(targetWeight, start, middle - 1);
		}
    	
    	int currentWeight = entries.get(middle).accumulatedWeight;
    	
    	if (targetWeight > currentWeight) {
			return findEntryWithWeight(targetWeight, middle + 1, end);
		} else {
			return entries.get(middle).object;
		}
    }
}
