package me.wangxx.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

/**
 * @author wangxx
 * 
 */
public class NioHttpServer implements Runnable {

	private static Logger logger = Logger.getLogger(NioHttpServer.class);

	public static void main(String[] args) throws IOException {

		int port = 9090;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}

		// 1.init
		NioHttpServer httpServer = new NioHttpServer(port);

		//
		new Thread(new Reader(), "reader").start();
		new Thread(new Writer(), "writer").start();
		new Thread(new Handler(),"handler").start();
		new Thread(httpServer, "selector").start();
	}

	private static Selector selector;
	private ServerSocketChannel serverChannel;
	private static BlockingQueue registerWriteQueue = new LinkedBlockingQueue();

	public NioHttpServer(int port) throws IOException {
		selector = Selector.open();
		serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(new InetSocketAddress(port));
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
	}

	@Override
	public void run() {
		System.out.println("start listening");

		SelectionKey key = null;
		while (true) {

			try {
				//
				int num = selector.select();

				if (num > 0) {
					Set selectedKeys = selector.selectedKeys();
					Iterator it = selectedKeys.iterator();

					while (it.hasNext()) {
						//
						key = (SelectionKey) it.next();
						it.remove();

						if (!key.isValid()) {
							continue;
						}
						if (key.isAcceptable()) {
							accept(key);
						} else if (key.isReadable()) {
							read(key);
							key.cancel();
						} else if (key.isWritable()) {
							write(key);
							key.cancel();
						}
					}
				} else {
					//register "write" event
					dealRegisterQueue();
				}

			} catch (Exception e) {
				if(key != null){
					key.cancel();
					Util.closeQuietly(key.channel());
				}
				logger.error("closed error",e);
			}
		}
	}
	
	private void dealRegisterQueue() throws InterruptedException{
		
		while(!registerWriteQueue.isEmpty()){
			SocketChannel channel = (SocketChannel) registerWriteQueue.take();
			try {
				channel.register(selector,  SelectionKey.OP_WRITE);
			} catch (ClosedChannelException e) {
				logger.error("dealRegisterQueue error",e);
			}
		}
	}
	
	public static void addRegisterQueue(SocketChannel channel){
		registerWriteQueue.offer(channel);
		selector.wakeup();
	}	

	private void accept(SelectionKey key) throws IOException {
		SocketChannel socketChannel = serverChannel.accept();
		socketChannel.configureBlocking(false);
		
		HttpRequest request = new HttpRequest(socketChannel);
		socketChannel.register(selector, SelectionKey.OP_READ,request);
	}

	private void read(SelectionKey key) throws IOException {
		//触发读线程
		Reader.addReaderQueue(key);
	}

	private void write(SelectionKey key) throws IOException {
		//触发写线程
		Writer.addWriterQueue(key);
	}

}
