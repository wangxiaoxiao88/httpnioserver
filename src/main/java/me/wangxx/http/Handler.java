package me.wangxx.http;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
				
			} catch (Exception e) {
				logger.error("Handler error",e);
			}
			
		}
	}
	
	private static void process(HttpRequest request) throws IOException{
		
		//1.parse http
		byte[] buf = request.getBody();
		RequestHeaderDecoder decoder = new RequestHeaderDecoder();
		decoder.parseData(buf);
		
		//2.build return result
		byte[] body = buildResponseBody(decoder);
		request.setBody(body);
		
		//3.register "write"
		NioHttpServer.addRegisterQueue(request.getKey());
	}
	
	public static final String OK_200 = "HTTP/1.1 200 OK";
	public static final String NOT_FOUND_404 = "HTTP/1.1 404 Not Find";
	public static final String SERVER_ERROR_500 = "HTTP/1.1 500 Internal Server Error";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String CONNECTION = "Connection";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String KEEP_ALIVE = "keep-alive";
	public static final String CONTENT_ENCODING = "Content-Encoding";
	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String LAST_MODIFIED = "Last-Modified";
	public static final String GZIP = "gzip";
	
	private static byte[] buildResponseBody(RequestHeaderDecoder header) throws IOException{
		File currentFile = new File("/Users/xiaoxiao/test" + header.getResource());
		String mime = Util.getContentType(currentFile);
		String acceptEncoding = header.getHeader(ACCEPT_ENCODING);
		
		ResponseHeaderEncoder encoder = new ResponseHeaderEncoder();

		encoder.addHeader(CONNECTION, KEEP_ALIVE);
		encoder.addHeader(CONTENT_TYPE, mime);

		boolean zip = false;
		byte[] body = Util.file2ByteArray(currentFile, zip);
		encoder.addHeader(CONTENT_LENGTH, body.length+"");
		if (zip) {
			encoder.addHeader(CONTENT_ENCODING, GZIP);
		}

		DateFormat formater = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		Date lastModified = new Date(currentFile.lastModified());
		encoder.addHeader(LAST_MODIFIED,
				formater.format(lastModified));

		// response header byte
		byte[] head = encoder.getHeader();
		
		byte[] result = new byte[head.length + body.length];
		System.arraycopy(head, 0, result, 0, head.length);
		System.arraycopy(body, 0, result, head.length, body.length);
		System.out.println("---------");
		System.out.println(new String(head));
		System.out.println("---------");
		System.out.println(new String(body));
		System.out.println("---------");
		System.out.println(new String(result));
		return result;
	}
	
	public static void addHandlerQueue(HttpRequest httpRequest){
		blockingQueue.offer(httpRequest);
	}

}
