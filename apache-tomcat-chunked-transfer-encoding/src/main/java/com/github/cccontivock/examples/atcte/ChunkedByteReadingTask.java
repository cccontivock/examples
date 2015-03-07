package com.github.cccontivock.examples.atcte;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.GenericServlet;

class ChunkedByteReadingTask implements Runnable {
	private final GenericServlet genericServlet;
	private final URL url;
	private final AtomicBoolean chunkDecodingFailed;

	ChunkedByteReadingTask(GenericServlet genericServlet, URL url,
			AtomicBoolean chunkDecodingFailed) {
		this.genericServlet = genericServlet;
		this.url = url;
		this.chunkDecodingFailed = chunkDecodingFailed;
	}

	@Override
	public void run() {
		try {
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setChunkedStreamingMode(4096);
			huc.connect();
			InputStream inputStream = huc.getInputStream();
			byte[] bytes = new byte[1024];
			while (inputStream.available() > 0) {
				inputStream.read(bytes);
			}
			inputStream.close();
			huc.disconnect();
		} catch (IOException ex) {
			if (ex.getMessage().contains("missing CR")) {
				chunkDecodingFailed.set(true);
			}
			genericServlet.log("Failure during byte reading.", ex);
			return;
		}
		genericServlet.log("Read bytes without error.");
	}
}
