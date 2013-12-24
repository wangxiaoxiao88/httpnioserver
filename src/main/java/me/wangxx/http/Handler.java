package me.wangxx.http;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

/**
 * @author wangxx
 *
 * 业务处理类
 */
public class Handler implements Runnable{
	
	private static Logger logger = Logger.getLogger(Handler.class);
	
	private static BlockingQueue blockingQueue = new LinkedBlockingQueue();

	@Override
	public void run() {
		while(true){
			
			try {
				HttpRequest request = (HttpRequest)blockingQueue.take();
				
				//
				process(request);
				
			} catch (InterruptedException e) {
				logger.error("Handler error",e);
			}
			
		}
	}
	
	private static void process(HttpRequest request){
		
		//1.hanler
		System.out.println("receive data is " + request.getBody());
		
		//2.register "write"
		NioHttpServer.addRegisterQueue(request.getSocketChannel());
	}
	
	public static void addHandlerQueue(HttpRequest httpRequest){
		blockingQueue.offer(httpRequest);
	}

}
