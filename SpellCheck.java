import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class SpellCheck {
	
	static Scanner inputReader;
	static Dictionary dictionary;
	// Used when checking words for spelling.
	static ArrayList<Word> WordList = new ArrayList<Word>();
	static boolean misspelledwords;
	// Print the menu
	public static void printMenu(){
		System.out.println("What would you like to do?");
		System.out.println("0) Quit");
		System.out.println("1) Add words to the dictionary through the console");
		System.out.println("2) Add words to the dictionary with a file");
		System.out.println("3) Enter text for the spellchecker through the console");
		System.out.println("4) Enter text for the spellchecker with a file");
		System.out.println("5) Reset the dictionary");		
	}
	
	// Add words to the dictionary via console. Keep reading words line by line until the user enters 0.
	public static void addWords() {
		String text;
		String[] words;
		System.out.println("Enter the words you would like to the dictionary seperated by spaces. Type 0 to finish entering words.");
		text = inputReader.nextLine();
		words = text.split(" ");
		
		while (!text.equals("0")) {
			for (String word : words){
				addWordToDict(word);
			}
			System.out.println("Word(s) added");
			text = inputReader.nextLine();
			words = text.split(" ");
		}
		printMenu();
	}
	
	// Helper function to add a word. Adds it to the dictionary and then writes it to the dictionary file.
	public static void addWordToDict(String word){
		if (!dictionary.containsWord(word)) {
			dictionary.addWord(word);
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter("dictionary.txt", true));
				bw.write(word);
				bw.newLine();
				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*Add to the dictionary via a file name. Assumes that the file is in the project folder.
	 *Also assumes words are separated by spaces.
	 */
	public static void addWithFile(){
		System.out.println("Please enter the name of the file");
		String fileName = inputReader.nextLine();
		BufferedReader reader = null;
		File file = new File(fileName);
		if (file.exists()) {
			try {
				reader = new BufferedReader(new FileReader(file));
			    String text = null;
			    while ((text = reader.readLine()) != null) {
					String[] words = text.split(" ");
					for (String word : words){
						/*Don't need to check if its already in the dict,
						 *since it is a HashSet and duplicates wont be added.
						 */
						addWordToDict(word);
					}
			    }
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("File doesnt exist!");
		}
		System.out.println("File contents added to dictionary");
		printMenu();
	}
	
	
	// Check spelling for text entered via console.
	public static void checkSpelling() {
		
		System.out.println("Enter the text you would like to have spellchecked.");
		String text = inputReader.nextLine();
		String[] words = text.split(" "); 
		
		int i=0;
		Thread[] threads = new Thread[words.length];
		// for all words start a thread thread that checks the dictionary then adds a word to WordList.
		for (String word : words) {
			Runnable r = new CheckerThread(word, i, false);
			threads[i] = new Thread(r);
			threads[i].start();			
			i++;
		}
		// Wait until all words are checked.
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// Sorting is needed because threads can finish at different times but we need to preserve order
		WordList.sort(new Comparator<Word>() {
		    @Override
		    public int compare(Word w1, Word w2) {
		        return w1.order - w2.order;
		    }
		});
		// The result used later if the user wants to save the line.
		String result = "";
		for (String word : words) {
			result = result + word + " ";
		}
		// If there are misspelled words allow the user to rewrite them one by one.
		if (misspelledwords) {
			System.out.println("There are some mispelled words. Would you like to go through and rewrite them? (y/n)");
			String revision = inputReader.nextLine();
			if (revision.equals("y")){
				result = "";
				for (Word wp: WordList) {
					// For all words with incorrect spelling ask the user to fix the spelling
					if (wp.correctspelling == false) {
						// Replace the word with the new user input
						System.out.println("Please enter the correct spelling of " + wp.text);
						String replacement = inputReader.nextLine();	
						wp.text = replacement;
					}
					// Put the revised result together
					result = result + wp.text + " ";
				}
				System.out.println("Revised text: " + result);
			} else {
				System.out.println("Text not revised");
			}
		} else {
			System.out.println("No misspelled words were found");
		}
		// Since work with the global ArrayList is done it can be reset for later use.
		misspelledwords = false;
		WordList.clear();
		saveToFile(result);
		printMenu();
	}
	
	/* Read a file in and check if there are misspelled words. Give the user the option to rewrite words one by one.
	 * There is an edge case of newlines that must be accounted for.
	 */
	public static void checkFileSpelling(){
		System.out.println("What is the name of the file you would like to read from?");
		// Get file name and create a scanner that will read from it
		String fileName = inputReader.nextLine();
		File file = new File(fileName);
		if (file.exists()) {
			Scanner scanner = null;
			String text = null;
			try {
				scanner = new Scanner(file);
				// Put all text into one string
				text = scanner.useDelimiter("\\A").next();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				scanner.close();
			}
			// Split by spaces
			String[] words = text.split(" ");
			ArrayList<Thread> threads = new ArrayList<Thread>();
			
			int i=0;
			for (String word : words) {
				// Special case since newlines are not split by space, they must be dealt with separately
				if (word.contains("\n")) {
					String[] linebreak = word.split("\n");
					// Strange little workaround for the case where the string is of the format: word\nword\n
					int l = linebreak.length;
					int q = 1;
					boolean lb = false;
					for (String w: linebreak) {
						if (q<l) {
							lb = true;
						} else {
							lb = false;
						}
						Runnable r = new CheckerThread(w, i, lb);
						threads.add(new Thread(r));
						threads.get(i).start();
						i++;
						q++;
					}
				} else {
					Runnable r = new CheckerThread(word, i, false);
					threads.add(new Thread(r));
					threads.get(i).start();
					i++;
				}
			}
			// Wait until all words have been checked.
			for (Thread t: threads) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			 // Sort because you arent sure when the threads will finish
			WordList.sort(new Comparator<Word>() {
			    @Override
			    public int compare(Word w1, Word w2) {
			        return w1.order - w2.order;
			    }
			});
			
			if (misspelledwords) {
				System.out.println("This file contains spelling errors. Would you like to go through and correct them? (y/n)");
			} else {
				System.out.println("The file entered had no spelling errors.");
				printMenu();
				return;
			}
			String confirmation = inputReader.nextLine();
			// For each word if it is misspelled, ask the user for correct spelling
			if (confirmation.equals("y")){
				for (int k=0; k<WordList.size(); k++) {
					if (WordList.get(k).correctspelling == false) {
						System.out.println("Please enter the correct spelling for " + WordList.get(k).text);
						String in = inputReader.nextLine();
						// Again deal with the special case of newlines when correcting text. This part preserves initial formatting.
						if (WordList.get(k).newline) {
							WordList.get(k).text = in + "\n";
						} else {
							WordList.get(k).text = in;
						}	
					}
				}
				String result = "";
				for (Word w : WordList) {
					result = result + " " + w.text;
				}
				System.out.println("Result = " + result);
				saveToFile(result);
			} else {
				System.out.println("Errors not corrected.");
			}
		} else {
			System.out.println("File doesnt exist!");
		}
		WordList.clear();
		misspelledwords = false;
		printMenu();
	}
	
	// Helper function to take input and prompt the user to enter a file name to save to.
	public static void saveToFile(String input) {
		System.out.println("Would you like to save the result to a file? (y/n)");
		String save = inputReader.nextLine();
		if (save.equals("y")) {
			try {
				System.out.println("Please enter the name of the file to be created");
				String name = inputReader.nextLine();
				OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(name));
				Writer writer = new BufferedWriter(os);
		        writer.write(input);
		        writer.flush();
		        writer.close();
		        System.out.println("Result saved into file named "+ name);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Result not saved.");
		}
	}
	
	// Resets the dictionary by deleting dictionary.txt and then creating a new one. Also creates a new dictionary object.
	public static void resetDictionary() {
		// Delete the file and create a new blank one.
		try {
			File dictionaryFile = new File("dictionary.txt");
			Files.deleteIfExists(dictionaryFile.toPath());
			dictionaryFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Dictionary Reset");
		printMenu();
		dictionary = new Dictionary();
	}
	
	//Fill out the dictionary at the start of execution based on what is in the file.
	public static void populateDictionary() {
		try {
			File dictionaryFile = new File("dictionary.txt");
			// Does nothing if there already is a file.
			dictionaryFile.createNewFile();
			FileInputStream fstream = new FileInputStream("dictionary.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		    String line;
		    // Add all words from dictionary.txt to the dictionary object. 
		    while ((line = br.readLine()) != null) {
		    	dictionary.addWord(line);
		    }
		    br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		dictionary = new Dictionary();
		inputReader = new Scanner(System.in);
		misspelledwords = false;
		
		populateDictionary();
		printMenu();
    
		String line = inputReader.nextLine();
		// Loop until the user is done.
		while (!line.equals("0")){
			switch (line) {
			// Add words to the Dictionary
			case "1":
				addWords();
				break;
			// Add words via a file.
			case "2":
				addWithFile();
				break;
			// Inputting text for the spell checker
			case "3":
				checkSpelling();
				break;
			// Input text via file
			case "4":
				checkFileSpelling();
				break;
			// Delete dictionary and create a new blank file
			case "5":
				resetDictionary();
				break;
			default:
				System.out.println("Please enter 0-5");
			}
			line = inputReader.nextLine();
		}
		inputReader.close();
		System.out.println("Quit");
	}
}
