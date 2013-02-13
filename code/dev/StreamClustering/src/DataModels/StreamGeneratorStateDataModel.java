package DataModels;

import java.util.HashMap;
import java.util.Map;

public class StreamGeneratorStateDataModel {

	//Map from tweet to coutner+minium so far
	Map<String, TweetCounter> tweetToCounter = new HashMap<String, TweetCounter>();
	
	public DistanceResult getClosestOrUpdateCounter(Tweet t) {
		TweetCounter c = tweetToCounter.get(t.getTweet());
		
		if(c == null)
		{
			c = new TweetCounter();
			tweetToCounter.put(t.getTweet(), c);
		}
		
		DistanceResult r = c.getClosestOrUpdateCounter(t);
		if(r!=null)
		{
			tweetToCounter.remove(t.getTweet());
		}
		
		return r;
	}

}
