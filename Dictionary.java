import java.util.HashSet;

public class Dictionary {
	
	
	/* Hashset is used because of constant time to add and check if a string is contained within.
	Also it does not allow for duplicates.
	*/
	private HashSet<String> dict;
	public Dictionary (){
		dict = new HashSet<String>();
	}
	
	//Dictionary is case insensitive
	public void addWord(String word) {
		dict.add(word.toLowerCase());
	}
	
	//Must lowercase the word first since the dictionary is not case sentitive.
	public boolean containsWord(String word) {
		return dict.contains(word.toLowerCase());
	}
	
	public void printDictionary(){
		System.out.println(dict);
	}

}
