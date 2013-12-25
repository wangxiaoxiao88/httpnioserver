package me.wangxx.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResponseHeaderEncoder {
	
	private static final String NEWLINE = "\r\n";
	private static final String OK_200 = "HTTP/1.1 200 OK";
	
	private String status;
	
	private Map<String,String> header = new HashMap<String,String>();
	
	public ResponseHeaderEncoder(){
		status = OK_200;
	}
	
	public void addHeader(String key,String value){
		header.put(key, value);
	}
	
	public byte[] getHeader() {
		return toString().getBytes();
	}
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder(120);
		sb.append(status).append(NEWLINE);
		Set<String> keySet = header.keySet();
		for (String key : keySet) {
			sb.append(key).append(": ").append(header.get(key)).append(NEWLINE);
		}
		sb.append(NEWLINE); // empty line;
		return sb.toString();
	}
	

}
