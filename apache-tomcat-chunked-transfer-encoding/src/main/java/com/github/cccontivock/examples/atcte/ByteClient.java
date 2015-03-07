package com.github.cccontivock.examples.atcte;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = ByteClient.servletMapping, loadOnStartup = ByteServlet.loadOnStartup + 1)
public class ByteClient extends HttpServlet {
	static final String servletMapping = "/byteclient";
	private static final long serialVersionUID = 3106512104504697600L;

	private Thread managementThread;

	@Override
	public void destroy() {
		if (null == managementThread) {
			return;
		}
		managementThread.interrupt();
		try {
			managementThread.join();
		} catch (InterruptedException ex) {
			log("Failed to join management thread.", ex);
		} finally {
			managementThread = null;
		}
	}

	private URL createUrl(HttpServletRequest request) {
		try {
			return new URL(request
					.getRequestURL()
					.toString()
					.replaceAll(ByteClient.servletMapping,
							ByteServlet.servletMapping));
		} catch (MalformedURLException ex) {
			log("Failed to instantiate URL.", ex);
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected synchronized void doGet(HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		if (null != managementThread) {
			return;
		}
		managementThread = new Thread(new TaskManagementRunnable(this,
				createUrl(req)));
		managementThread.start();
	}
}
