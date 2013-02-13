package appl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import utils.Utils;

import DataModels.Tweet;

import edu.usc.pgroup.floe.api.communication.*;
import edu.usc.pgroup.floe.impl.communication.*;

public class TweetGenerator {

	public static byte[] tweetToByte(Tweet inpInfo) {
		byte[] retBytes = null;
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream outStream = new ObjectOutputStream(byteStream);
			outStream.writeObject(inpInfo);
			outStream.flush();
			outStream.close();
			byteStream.close();
			retBytes = byteStream.toByteArray();
		} catch (IOException ex) {
			// TODO: Handle the exception
		}
		return retBytes;
	}

	public static void main(String[] args) {
		String location = "localhost";
		int port = Integer.parseInt("57158");

		ConnectionInfo cInfo = new ConnectionInfo();
		cInfo.setDestAddress(location);
		cInfo.setOutPort(port);

		TCPSinkPushChannel channel = new TCPSinkPushChannel(cInfo);
		channel.openConnection();

		// for
		int i = 0;
		FileInputStream fstream;
		try {
			fstream = new FileInputStream("data/tentweets.txt");

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			int cnt = 10000;
			int maxThreads = 24;
			Thread[] ts = new Thread[24];
			int tcnt = 0;

			while ((strLine = br.readLine()) != null) {

				Tweet t = new Tweet();
				t.setTweet(strLine);
				t.setGenTime(System.currentTimeMillis());
				
				Message<byte[]> msg = new MessageImpl<byte[]>();

				msg.putPayload(Utils.Serialize(t));

				channel.putMessage(msg);
				try {
					Thread.currentThread().sleep(1000/5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
				//	e.printStackTrace();
				}
				i++;
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
