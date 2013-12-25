package me.wangxx.http;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class HttpRequest {
	
	private byte[] body;
	
//	private SocketChannel channel;
//
//	public HttpRequest(SocketChannel channel){
//		this.channel = channel;
//	}
//	
//	public SocketChannel getSocketChannel(){
//		return channel;
//	}
	
	private SelectionKey key;

	public SelectionKey getKey() {
		return key;
	}

	public void setKey(SelectionKey key) {
		this.key = key;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
	
	
}
