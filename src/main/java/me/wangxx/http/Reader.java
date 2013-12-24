package me.wangxx.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class Reader implements Runnable{
	
	private static Logger logger = Logger.getLogger(Reader.class);
	
	private static BlockingQueue queue = new LinkedBlockingQueue();
	
	private static int BUFFER_SIZE = 1024;
	
	public static void addReaderQueue(SelectionKey key){
		queue.offer(key);
	}

	@Override
	public void run() {
		
		while(true){
			try {
				SelectionKey key = (SelectionKey)queue.take();
				
				//
				process(key);
			} catch (Exception e) {
				logger.error("Reader error",e);
			}
		}
	}
	
	private static void process(SelectionKey key) throws IOException{
		
		SocketChannel channel = (SocketChannel) key.channel();
		
		//1.read
		byte[] body = read(channel);
		
		HttpRequest request = (HttpRequest)key.attachment();
		request.setBody(body);
		
		//2.handle
		Handler.addHandlerQueue(request);
	}
	
	private static byte[] read(SocketChannel channel) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		
		byte[] data = new byte[BUFFER_SIZE * 2];
		int r = 0;
		int offset = 0;
		
		while(true){
			buffer.clear();
			r = channel.read(buffer);
			if(r  == -1 || r == 0) break;
			if(r+offset > data.length){
				data = growDouble(data);
			}
			byte[] buf = buffer.array();
			System.arraycopy(buf, 0, data, offset, r);
			offset += r;
		}
		 byte[] req = new byte[offset];
	     System.arraycopy(data, 0, req, 0, offset);
	     
		return req;
	}
	
	private static byte[] growDouble(byte[] buf){
		byte[] tmp = new byte[buf.length * 2];
		System.arraycopy(buf, 0, tmp, 0, buf.length);
		return tmp;
	}
	
	

}
