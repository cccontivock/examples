package com.github.cccontivock.examples.atcte;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = ByteServlet.servletMapping, loadOnStartup = ByteServlet.loadOnStartup)
public class ByteServlet extends HttpServlet {
	static final String servletMapping = "/byteservlet";
	static final int loadOnStartup = 1;
	private static final long serialVersionUID = -5405432244347991445L;
	private static final int bytesToServeCountTotal = 1024 * 1024;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log(getClass().getName() + " processing an HTTP GET request.");
		response.addHeader("Content-Length", "-1");
		OutputStream outputStream = response.getOutputStream();
		for (int bytesServedCt = 0; bytesServedCt < bytesToServeCountTotal; bytesServedCt++) {
			outputStream.write('*');
		}
		outputStream.flush();
		outputStream.close();
	}
}
