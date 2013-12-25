package me.wangxx.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class Writer implements Runnable{
	
private static Logger logger = Logger.getLogger(Handler.class);
	
	private static BlockingQueue queue = new LinkedBlockingQueue();

	@Override
	public void run() {
		
		while(true){
			try {
				SelectionKey key = (SelectionKey)queue.take();
				
				write(key);
				
			} catch (Exception e) {
				logger.error("write error",e);
			}
		}
	}
	
	private static void write(SelectionKey key) throws IOException{
		
		SocketChannel channel = (SocketChannel)key.channel();
		
		//1.write
		HttpRequest request = (HttpRequest)key.attachment();
		byte[] data = request.getBody();
		ByteBuffer buffer = ByteBuffer.allocate(data.length);
        buffer.put(data, 0, data.length);
        buffer.flip();
        
        channel.write(buffer);
        
        //2.close
        close(channel);
	}
	
	private static void close(SocketChannel channel){
		try {
			channel.finishConnect();
			channel.socket().close();
			channel.close();
		} catch (IOException e) {
			logger.error("error close",e);
		}
	}

	public static void addWriterQueue(SelectionKey key){
		queue.offer(key);
	}
}
