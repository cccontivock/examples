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
	private final AtomicBoolean shouldStop = new AtomicBoolean(false);
	private int taskCount = 1;
	private ExecutorService executorService;

	TaskManagementRunnable(GenericServlet genericServlet, URL url) {
		this.genericServlet = genericServlet;
		this.url = url;
	}

	@Override
	public void run() {
		boolean erred = false;
		while (taskCount < 512 && !erred && !chunkDecodingFailed.get()
				&& !shouldStop.get()) {
			increaseChunkedByteReadersCount();
			startChunkedByteReaders();
			erred = waitForChunkedByteReaders();
		}
		genericServlet.log("Did" + (chunkDecodingFailed.get() ? "" : " not")
				+ " encounter chunk-decoding failure.");
		genericServlet.log(getClass().getName() + ".run() exiting.");
	}

	private int increaseChunkedByteReadersCount() {
		return taskCount *= 2;
	}

	private void startChunkedByteReaders() {
		executorService = Executors.newFixedThreadPool(taskCount);
		for (int taskIdx = 0; taskIdx < taskCount; taskIdx++) {
			executorService.execute(new ChunkedByteReadingTask(genericServlet,
					url, chunkDecodingFailed, shouldStop));
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
