package rpg_npcs;

import java.util.ArrayList;
import java.util.HashMap;
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
    
    public Map<T, Integer> getEntryMap() {
    	Map<T, Integer> entryMap = new HashMap<T, Integer>();
    	
    	int lastWeight = 0;
    	for (Entry entry : entries) {
    		entryMap.put(entry.object, entry.accumulatedWeight - lastWeight);
    		lastWeight = entry.accumulatedWeight;
		}
    	
    	return entryMap;
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
    	double targetWeight = rand.nextDouble() * accumulatedWeight;
        
        return findEntryWithWeight(targetWeight);
    }
    
    public T findEntryWithWeight(double targetWeight) {
    	// Special case for targetWeight == 0 as it would normally due to bounds select the -1st element
    	if (targetWeight == 0 && size() > 0) {
			return entries.get(0).object;
		}
    	
    	return findEntryWithWeight(targetWeight, 0, entries.size() - 1);
    }
    
    private T findEntryWithWeight(double targetWeight, int start, int end) {
    	// Binary search
    	if (start > end) {
			return null;
		}
    	
    	int middle = (start + end) / 2;
    	
    	int prevWeight = (middle - 1) < 0 ? 0 : entries.get(middle - 1).accumulatedWeight;
    	
    	if (targetWeight <= prevWeight) {
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
