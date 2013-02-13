package utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Arrays;


import org.apache.poi.util.ArrayUtil;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import ru.fuzzysearch.BKTreeIndex;
import ru.fuzzysearch.BKTreeSearcher;
import ru.fuzzysearch.LevensteinMetric;
import ru.fuzzysearch.Metric;
import ru.fuzzysearch.SearchResult;

public class Utils {
	private static File fis;
	private static BKTreeIndex RI;
	
	public static int NumHashFunctions = 10;
	
	static{
		fis = new File("../../external/StringMatching/out.obj");
		try {
			RI = Utils.<BKTreeIndex>Deserialize(getBytesFromFile(fis));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static ArrayList<Long> getFeatureVector (String str) {
		String[] tokens = str.split(" ");

		int i=0;
		String ret_string = "";
		ArrayList<Long> results = new ArrayList<Long>();
		while(i<tokens.length){
				try {
						Metric mymetric = new LevensteinMetric();
						
						String curr_tok = tokens[i];
						int dist = 4;
						if(curr_tok.length()<5){
							dist = 2;
						}
						BKTreeSearcher RSearcher = new BKTreeSearcher(RI, mymetric, dist);
						////system.out.println("Searched: " + curr_tok.toLowerCase() + "\n");
						Set<SearchResult> RK = RSearcher.search(curr_tok.toLowerCase(),true);
						////system.out.println(RK.size());
						int curr_min = 1000;
						String min_str = "";
						Long minIdx = -1L;
						for(SearchResult RR: RK){
							if(curr_min > RR.distance){
								curr_min = RR.distance;
								min_str = RI.getDictionary()[RR.index.intValue()];
								minIdx = RR.index;
							}
							////system.out.println(RI.getDictionary()[RR.index] + ":" + RR.distance);
						}
						if(minIdx != -1)
						{
							results.add(minIdx);
						}
						
						////system.out.println(min_str + " " + curr_min);
						if(ret_string==""){
							ret_string = ret_string + min_str;
						}
						else{
							ret_string = ret_string + " "+ min_str;
						}
					
					//p.print();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i=i+1;
			}
			
		return results;
	    //return ret_string;
	}
	
	public static  byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
	
	public static <T> T Deserialize (byte[] bytes)
	{
		T I = null;
		
		try 
		{
			
			
			ByteArrayInputStream byteInput = new ByteArrayInputStream (bytes);
			ObjectInputStream ois = new ObjectInputStream (byteInput);
			I = (T)ois.readObject();
		}
		catch (Exception ex) 
		{
	  
		}
		return I;
		
	}
	
	
	public static <T> byte[] Serialize(T I) 
	{
		byte[] retBytes = null;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try 
		{
		    ObjectOutputStream outStream = new ObjectOutputStream(byteStream); 
		    outStream.writeObject(I);
		    outStream.flush(); 
		    outStream.close(); 
		    byteStream.close();
		    retBytes = byteStream.toByteArray ();
		}
		catch (IOException ex) 
		{
		    //TODO: Handle the exception
		}
		return retBytes;
	}

	
	
	public static String cleanTweet(String strLine){
		StringTokenizer token_str = new StringTokenizer(strLine);
		  //System.out.println("currLine: "+strLine+" "+token_str.countTokens());
		  int counter=1;
		  int total_tokens = token_str.countTokens();
		  String newStrLine = "";
		  while(token_str.hasMoreTokens()){
			  String curr_token = token_str.nextToken();
			  if(counter == 1 || counter == 2 || counter == total_tokens || counter == total_tokens-1 || Utils.isStopWord(curr_token)){
				  counter = counter + 1;
				  continue;
			  }
			  else{
				  if(counter == 2){
					  newStrLine = curr_token+"\t";
				  }
				  else{
					  newStrLine = newStrLine + " "+curr_token;
				  }
				  
				  counter = counter + 1;
			  }
		  }
		  return newStrLine;
	}

	
	public static boolean isStopWord(String w)
	{
		String[] stopWords = { "a", "a's", "able", "about", "above", "abroad", "according", "accordingly", "across", "actually", "adj", "after", "afterwards", "again", "against", "ago", "ahead", "ain't", "aint", "all", "allow", "allows", "almost", "alone", "along", "alongside", "already", "also", "although", "always", "am", "amid", "amidst", "among", "amongst", "amoungst", "amount", "an", "and", "another", "any", "anybody", "anyhow", "anyone", "anything", "anyway", "anyways", "anywhere", "apart", "appear", "appreciate", "appropriate", "are", "aren't", "arent", "around", "as", "aside", "ask", "asking", "associated", "at", "available", "away", "awfully", "b", "back", "backward", "backwards", "be", "became", "because", "become", "becomes", "becoming", "been", "before", "beforehand", "begin", "behind", "being", "believe", "below", "beside", "besides", "best", "better", "between", "beyond", "bill", "both", "bottom", "brief", "but", "by", "c", "c'mon", "c's", "call", "came", "can", "can't", "cannot", "cant", "caption", "cause", "causes", "certain", "certainly", "changes", "clearly", "cmon", "co", "co.", "com", "come", "comes", "computer", "con", "concerning", "consequently", "consider", "considering", "contain", "containing", "contains", "corresponding", "could", "couldn't", "couldnt", "course", "cry", "currently", "d", "dare", "daren't", "darent", "de", "definitely", "describe", "described", "despite", "detail", "did", "didn't", "didnt", "different", "directly", "do", "does", "doesn't", "doesnt", "doing", "don't", "done", "dont", "down", "downwards", "due", "during", "e", "each", "edu", "eg", "eight", "eighty", "either", "eleven", "else", "elsewhere", "empty", "end", "ending", "enough", "entirely", "especially", "et", "etc", "even", "ever", "evermore", "every", "everybody", "everyone", "everything", "everywhere", "ex", "exactly", "example", "except", "f", "fairly", "far", "farther", "few", "fewer", "fifteen", "fifth", "fify", "fill", "find", "fire", "first", "five", "followed", "following", "follows", "for", "forever", "former", "formerly", "forth", "forty", "forward", "found", "four", "from", "front", "full", "further", "furthermore", "g", "get", "gets", "getting", "give", "given", "gives", "go", "goes", "going", "gone", "got", "gotten", "greetings", "h", "had", "hadn't", "hadnt", "half", "happens", "hardly", "has", "hasn't", "hasnt", "have", "haven't", "havent", "having", "he", "he'd", "he'll", "he's", "hello", "help", "hence", "her", "here", "here's", "hereafter", "hereby", "herein", "heres", "hereupon", "hers", "herse", "herself", "hi", "him", "himse", "himself", "his", "hither", "hopefully", "how", "how's", "howbeit", "however", "hows", "hundred", "i", "i'd", "i'll", "i'm", "i've", "ie", "if", "ignored", "immediate", "in", "inasmuch", "inc", "inc.", "indeed", "indicate", "indicated", "indicates", "inner", "inside", "insofar", "instead", "interest", "into", "inward", "is", "isn't", "isnt", "it", "it'd", "it'll", "it's", "itd", "itll", "its", "itse", "itself", "j", "just", "k", "keep", "keeps", "kept", "know", "known", "knows", "l", "last", "lately", "later", "latter", "latterly", "least", "less", "lest", "let", "let's", "lets", "like", "liked", "likely", "likewise", "little", "look", "looking", "looks", "low", "lower", "ltd", "m", "made", "mainly", "make", "makes", "many", "may", "maybe", "mayn't", "maynt", "me", "mean", "meantime", "meanwhile", "merely", "might", "mightn't", "mightnt", "mill", "mine", "minus", "miss", "more", "moreover", "most", "mostly", "move", "mr", "mrs", "much", "must", "mustn't", "mustnt", "my", "myse", "myself", "n", "name", "namely", "nd", "near", "nearly", "necessary", "need", "needn't", "neednt", "needs", "neither", "never", "neverf", "neverless", "nevertheless", "new", "next", "nine", "ninety", "no", "no-one", "nobody", "non", "none", "nonetheless", "noone", "nor", "normally", "not", "nothing", "notwithstanding", "novel", "now", "nowhere", "o", "obviously", "of", "off", "often", "oh", "ok", "okay", "old", "on", "once", "one", "one's", "ones", "only", "onto", "opposite", "or", "other", "others", "otherwise", "ought", "oughtn't", "oughtnt", "our", "ours", "ourselves", "out", "outside", "over", "overall", "own", "p", "part", "particular", "particularly", "past", "per", "perhaps", "placed", "please", "plus", "possible", "presumably", "probably", "provided", "provides", "put", "q", "que", "quite", "qv", "r", "rather", "rd", "re", "really", "reasonably", "recent", "recently", "regarding", "regardless", "regards", "relatively", "respectively", "right", "round", "s", "said", "same", "saw", "say", "saying", "says", "second", "secondly", "see", "seeing", "seem", "seemed", "seeming", "seems", "seen", "self", "selves", "sensible", "sent", "serious", "seriously", "seven", "several", "shall", "shan't", "shant", "she", "she'd", "she'll", "she's", "shes", "should", "shouldn't", "shouldnt", "show", "side", "since", "sincere", "six", "sixty", "so", "some", "somebody", "someday", "somehow", "someone", "something", "sometime", "sometimes", "somewhat", "somewhere", "soon", "sorry", "specified", "specify", "specifying", "still", "sub", "such", "sup", "sure", "system", "t", "t's", "take", "taken", "taking", "tell", "ten", "tends", "th", "than", "thank", "thanks", "thanx", "that", "that'll", "that's", "that've", "thatll", "thats", "thatve", "the", "their", "theirs", "them", "themselves", "then", "thence", "there", "there'd", "there'll", "there're", "there's", "there've", "thereafter", "thereby", "thered", "therefore", "therein", "therell", "therere", "theres", "thereupon", "thereve", "these", "they", "they'd", "they'll", "they're", "they've", "theyd", "theyll", "theyre", "theyve", "thick", "thin", "thing", "things", "think", "third", "thirty", "this", "thorough", "thoroughly", "those", "though", "three", "through", "throughout", "thru", "thus", "till", "to", "together", "too", "took", "top", "toward", "towards", "tried", "tries", "truly", "try", "trying", "twelve", "twenty", "twice", "two", "u", "un", "under", "underneath", "undoing", "unfortunately", "unless", "unlike", "unlikely", "until", "unto", "up", "upon", "upwards", "us", "use", "used", "useful", "uses", "using", "usually", "v", "value", "various", "versus", "very", "via", "viz", "vs", "w", "want", "wants", "was", "wasn't", "wasnt", "way", "we", "we'd", "we'll", "we're", "we've", "welcome", "well", "went", "were", "weren't", "werent", "weve", "what", "what'll", "what's", "what've", "whatever", "whatll", "whats", "whatve", "when", "when's", "whence", "whenever", "whens", "where", "where's", "whereafter", "whereas", "whereby", "wherein", "wheres", "whereupon", "wherever", "whether", "which", "whichever", "while", "whilst", "whither", "who", "who'd", "who'll", "who's", "whod", "whoever", "whole", "wholl", "whom", "whomever", "whos", "whose", "why", "why's", "whys", "will", "willing", "wish", "with", "within", "without", "won't", "wonder", "wont", "would", "wouldn't", "wouldnt", "x", "y", "yes", "yet", "you", "you'd", "you'll", "you're", "you've", "youd", "youll", "your", "youre", "yours", "yourself", "yourselves", "youve", "z", "zero" };
		return Arrays.binarySearch(stopWords, w) >= 0 ? true : false;
	}


}
