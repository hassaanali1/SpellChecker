/* This class needs to hold the text, the order, whether or not its a newline and whether
 * or not the spelling of the word is correct.
 */
public class Word {
	String text;
	int order;
	boolean newline;
	boolean correctspelling;
	
	public Word(String w, int ord, boolean nl, boolean cs) {
		text = w;
		order = ord;
		newline = nl;
		correctspelling = cs;
	}

	public Word() {
		text = null;
		order = 0;
		newline = false;
		correctspelling = false;
	}
	
}
