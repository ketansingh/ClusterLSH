package edu.usc.pgroup.floe.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.net.URLEncoder;

public class EucalyptusAccountInfo 
{
	String endPoint;
	String awsAccessKey;
	String secretKey;
	String signatureMethod;	
	String signatureVersion;
	String version;
	String extension;
	
	public EucalyptusAccountInfo(InputStream eucaProperties)
	{
		Properties props = new Properties();
		try
		{
			props.load(eucaProperties);
			this.endPoint = props.getProperty("EndPoint");
			this.awsAccessKey =  props.getProperty("AWSAccessKeyId");
			this.secretKey = props.getProperty("SecretKey");
			this.extension = props.getProperty("Extension");
			this.signatureMethod = "HmacSHA256";
			this.signatureVersion = "2";
			this.version = "2009-04-04";
			eucaProperties.close();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}							
	}
	public String generateSignature(String value) 
	{
		String prefixStringReq = "GET\n"+this.endPoint+"\n" + this.extension + "\n" + value;
        try 
        {
            byte[] keyBytes = this.secretKey.getBytes();           
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, this.signatureMethod);
            Mac mac = Mac.getInstance(this.signatureMethod);
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(prefixStringReq.getBytes());
            return new String(Base64.encodeBase64(rawHmac));
        } 
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
    }
	public static String generateGMTTimeStamp()	
	{			
		SimpleDateFormat gmtDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		gmtDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");	
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		timeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date date = new Date();
		try {
			return dateFormat.format(date)+ "T" + URLEncoder.encode(timeFormat.format(date),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public String generateQueryString(TreeMap<String,String> inpParams)
	{		
		TreeMap<String,String> queryParams = inpParams;
		queryParams.put("AWSAccessKeyId", this.awsAccessKey);						
		queryParams.put("SignatureMethod",this.signatureMethod);
		queryParams.put("SignatureVersion",this.signatureVersion);
		queryParams.put("Version",this.version);		
		Set<Entry<String,String>> entrySet = queryParams.entrySet();		
		Iterator<Entry<String,String>> entryIter = entrySet.iterator();
		String retQueryStr = "";		
		while(entryIter.hasNext())
		{
			Entry<String,String> tempEntry = entryIter.next();
			retQueryStr+= tempEntry.getKey() + "=" + tempEntry.getValue()+"&";
		}
		retQueryStr = retQueryStr.substring(0,retQueryStr.length()-1);
		String queryString = "";
		try {
			queryString = "http://" + this.endPoint + this.extension + "?" + retQueryStr + "&Signature=" + URLEncoder.encode(generateSignature(retQueryStr),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return queryString;
	}
}
