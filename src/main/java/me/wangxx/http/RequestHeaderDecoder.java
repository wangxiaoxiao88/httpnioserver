package me.wangxx.http;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class RequestHeaderDecoder {
	
	public static enum Action {
		GET,HEAD,POST;
	}
	
	private static Logger logger = Logger.getLogger(RequestHeaderDecoder.class);
	
	private static final byte[] END = new byte[] { 13, 10, 13, 10 };
	private static final byte[] GET = new byte[] { 71, 69, 84, 32 };
	
	private static Map<String,String> header = new HashMap<String,String>();
	
	private String resource;
	
	public String getHeader(String key){
		return header.get(key);
	}
	
	public boolean parseData(byte[] buf){
		
		//1.find
		int beginIndex = Util.findSubArray(buf, GET, 0);
		
		if(beginIndex == -1){
			System.out.println("----------------------------");
			System.out.println(new String(buf));
			logger.error("not found get index");
			return false;
		}
		
		int endIndex = Util.findSubArray(buf, END, beginIndex);
		
		ByteBuffer b = ByteBuffer.wrap(buf, beginIndex, endIndex);
		
		//2.extrace
		extractValue(buf);			
		return true;
	}
	
	private void extractValue(byte[] buf){
		
		String body = new String(buf);
		
		String[] lines = body.split("\r\n");
		String[] split = lines[0].split(" ");
		
		resource = split[1];
		
		for(int i = 1;i<lines.length;i++){
			String[] temp = lines[i].split(":");
			if(temp.length == 2){
				header.put(temp[0].trim(), temp[1].trim());
			}
		}
	}
	
	
	public String getResource(){
		return resource;
	}

}
