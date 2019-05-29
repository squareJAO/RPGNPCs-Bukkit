package rpg_npcs.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import rpg_npcs.WeightedSet;

public class WeightedSetTest {

	@Test
	public void sizeTest() {
		// Check 0 to 100
		for (int i = 0; i < 100; i++) {
			WeightedSet<Integer> set = new WeightedSet<Integer>(); 
			
			for (int j = 0; j < i; j++) {
				set.addEntry(j, 1);
			}
			
			assertEquals(set.size(), i);
		}
	}

	@Test
	public void emptyTest() {
		// Check an empty set works as intended
		WeightedSet<Integer> set = new WeightedSet<Integer>();
		
		assertEquals(0, set.size());
		assertEquals(null, set.getRandom());
		
		Map<Integer, Integer> emptyMap = set.getEntryMap();
		assertEquals(0, emptyMap.size());
	}

	@Test
	public void get1Test() {
		// Check a single item set works as intended
		WeightedSet<Integer> set1 = new WeightedSet<Integer>();
		set1.addEntry(100, 1);
		
		assertEquals(Integer.valueOf(100), set1.getRandom());
	}

	@Test
	public void getTest() {
		// Check a collection of larger sets
		for (int setSize = 1; setSize < 128; setSize++) {
			WeightedSet<Integer> set = new WeightedSet<Integer>();
			
			int totalWeight = 0;
			
			for (int i = 1; i < setSize; i++) {
				set.addEntry(i, i);
				totalWeight += i;
			}
			
			int item = 1;
			int cumulativeWeight = 0;
			for (int weight = 0; weight < totalWeight; weight++) {
				assertEquals(Integer.valueOf(item), set.findEntryWithWeight(weight));
				
				if (cumulativeWeight >= item) {
					cumulativeWeight -= item;
					item++;
				}
				
				cumulativeWeight++;
			}
		}
	}

	@Test
	public void zipTest() {
		// Checks that zipping a set works correctly when all elements are present
		WeightedSet<Integer> set = new WeightedSet<Integer>();
		
		Map<String, Integer> expectedMap = new HashMap<String, Integer>();
		Map<Integer, String> mapping = new HashMap<Integer, String>();
		for (int i = 0; i < 100; i++) {
			int weight = 300 - i;
			
			set.addEntry(i, weight);
			
			String mapString = ("test data " + (char)i);
			
			expectedMap.put(mapString, weight);
			mapping.put(i, mapString);
		}
		
		WeightedSet<String> zippedSet = set.zip(mapping);
		
		assertTrue(expectedMap.equals(zippedSet.getEntryMap()));
	}

	@Test
	public void entryMapTest() {
		// Checks that getting an entry map works
		WeightedSet<String> set = new WeightedSet<String>();
		
		Map<String, Integer> expectedMap = new HashMap<String, Integer>();
		for (int i = 0; i < 100; i++) {
			int weight = 300 - i;
			String mapString = ("test data " + (char)i);
			
			set.addEntry(mapString, weight);
			
			expectedMap.put(mapString, weight);
		}
		
		assertTrue(expectedMap.equals(set.getEntryMap()));
	}
}
