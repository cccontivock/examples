package com.github.cccontivock.examples.atcte;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.GenericServlet;

class ChunkedByteReadingTask implements Runnable {
	private final GenericServlet genericServlet;
	private final URL url;
	private final AtomicBoolean chunkDecodingFailed;
	private final AtomicBoolean shouldStop;

	ChunkedByteReadingTask(GenericServlet genericServlet, URL url,
			AtomicBoolean chunkDecodingFailed, AtomicBoolean shouldStop) {
		this.genericServlet = genericServlet;
		this.url = url;
		this.chunkDecodingFailed = chunkDecodingFailed;
		this.shouldStop = shouldStop;
	}

	@Override
	public void run() {
		int bytesReadCtTotal = 0;
		try {
			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
			huc.setDoInput(true);
			huc.setRequestProperty("Transfer-Encoding", "chunked");
			huc.connect();
			InputStream errorStream = huc.getErrorStream();
			if (null != errorStream) {
				readAndLogError(errorStream);
				shouldStop.set(true);
				return;
			}
			InputStream inputStream = huc.getInputStream();
			int responseCode = huc.getResponseCode();
			if (responseCode > 300) {
				shouldStop.set(true);
				return;
			}
			genericServlet.log("HTTP response code " + responseCode);
			String transferEncoding = huc.getHeaderField("Transfer-Encoding");
			if (!"chunked".equalsIgnoreCase(transferEncoding)) {
				genericServlet.log("Unexpected Transfer-Encoding: "
						+ transferEncoding);
				shouldStop.set(true);
				return;
			}
			byte[] bytes = new byte[4096];
			while (bytesReadCtTotal < ByteServlet.bytesToServeCountTotal) {
				bytesReadCtTotal += inputStream.read(bytes);
			}
			inputStream.close();
			huc.disconnect();
		} catch (IOException ex) {
			shouldStop.set(true);
			if (ex.getMessage().contains("missing CR")) {
				ex.printStackTrace();
				chunkDecodingFailed.set(true);
			}
			genericServlet.log("Failure during byte reading.", ex);
			return;
		} finally {
			if (bytesReadCtTotal != ByteServlet.bytesToServeCountTotal) {
				genericServlet.log("Expected "
						+ ByteServlet.bytesToServeCountTotal
						+ " bytes but read " + bytesReadCtTotal + " bytes.");
			}
		}
		genericServlet.log("Read bytes without error.");
	}

	private void readAndLogError(InputStream errorStream) throws IOException {
		BufferedReader errorBr = new BufferedReader(new InputStreamReader(
				errorStream));
		String errorLine = null;
		StringBuilder errorSb = new StringBuilder();
		while (null == (errorLine = errorBr.readLine())) {
			errorSb.append(errorLine).append('\n');
		}
		String errorString = errorSb.toString();
		if (null != errorString && !errorString.isEmpty()) {
			genericServlet.log("Error response: " + errorString);
		}
	}
}
