package pellets;

import java.io.UnsupportedEncodingException;

import DataModels.Tweet;

import utils.Utils;

import edu.usc.pgroup.floe.api.exception.LandmarkException;
import edu.usc.pgroup.floe.api.framework.pelletmodels.StreamInStreamOutPellet;
import edu.usc.pgroup.floe.api.state.StateObject;
import edu.usc.pgroup.floe.api.stream.FEmitter;
import edu.usc.pgroup.floe.api.stream.FIterator;

public class TweetCleaner implements StreamInStreamOutPellet {

	@Override
	public void invoke(FIterator in, FEmitter out,
			StateObject stateObject) {
		
		while(true)
		{
			try {
				
				//byte[] t = in.next();
				Tweet tweet = (Tweet)in.next();
				//Tweet tweet = Utils.<Tweet>Deserialize(t);
				
				//System.out.println("TweetCleaner:"+tweet.getTweet());
				
				String newStrLine = Utils.cleanTweet(tweet.getTweet());
				
				String stemmed = TestMapRed.stemTerm(newStrLine);
				
				tweet.setStemmedCleanTweet(stemmed);
				
				byte[] nt = Utils.Serialize(tweet);
				out.emit(nt);
				
			} catch (LandmarkException e) {
			
				e.printStackTrace();
			}
			
		}		
	}

}
