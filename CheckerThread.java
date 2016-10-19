/*
 * These threads are used to spell check words in parallel
 */
public class CheckerThread implements Runnable {
	
	Word wp = new Word();
	
	public CheckerThread(String word, int ord, boolean newline){
		wp.text = word;
		wp.order = ord;
		wp.newline = newline;
	}
	
	//@Override
	public void run() {
		if (!SpellCheck.dictionary.containsWord(wp.text)){
			// Mark the word as spelled incorrectly
			wp.correctspelling = false;
			SpellCheck.misspelledwords = true;
		} else {
			wp.correctspelling = true;
		}
		SpellCheck.WordList.add(wp);	
	}

}
