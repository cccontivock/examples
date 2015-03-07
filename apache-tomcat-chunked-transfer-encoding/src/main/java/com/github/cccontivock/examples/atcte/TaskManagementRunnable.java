package com.github.cccontivock.examples.atcte;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.GenericServlet;

class TaskManagementRunnable implements Runnable {

	private final GenericServlet genericServlet;
	private final URL url;
	private final AtomicBoolean chunkDecodingFailed = new AtomicBoolean(false);
	private int taskCount = 1;
	private ExecutorService executorService;

	TaskManagementRunnable(GenericServlet genericServlet, URL url) {
		this.genericServlet = genericServlet;
		this.url = url;
	}

	@Override
	public void run() {
		boolean erred = false;
		while (!erred && !anyChunkDecodingFailed()) {
			increaseChunkedByteReadersCount();
			startChunkedByteReaders();
			erred = waitForChunkedByteReaders();
		}
	}

	private boolean anyChunkDecodingFailed() {
		return chunkDecodingFailed.get();
	}

	private int increaseChunkedByteReadersCount() {
		return taskCount *= 2;
	}

	private void startChunkedByteReaders() {
		executorService = Executors.newFixedThreadPool(taskCount);
		for (int taskIdx = 0; taskIdx < taskCount; taskIdx++) {
			executorService.execute(new ChunkedByteReadingTask(genericServlet,
					url, chunkDecodingFailed));
		}
	}

	private boolean waitForChunkedByteReaders() {
		executorService.shutdown();
		try {
			executorService.awaitTermination(2, TimeUnit.MINUTES);
			return false;
		} catch (InterruptedException ex) {
			genericServlet.log(
					"Failed to await termination of executor service.", ex);
			return true;
		} finally {
			executorService = null;
		}
	}
}
