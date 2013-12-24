package me.wangxx.http;

import java.io.Closeable;
import java.io.IOException;

public class Util {
	
	public static void closeQuietly(Closeable is) {
		if (is != null) {
			try {
				is.close();
			} catch (IOException e) {
			}
		}
	}

}
