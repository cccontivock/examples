package com.github.cccontivock.examples.atcte;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(value = ByteServlet.servletMapping, loadOnStartup = ByteServlet.loadOnStartup)
public class ByteServlet extends HttpServlet {
	static final String servletMapping = "/byteservlet";
	static final int loadOnStartup = 1;
	static final int bytesToServeCountTotal = 4 * 1024 * 1024;
	private static final long serialVersionUID = -5405432244347991445L;
	private static final byte[] byteArray = createArray();

	private static byte[] createArray() {
		byte[] byteArray = new byte[4096];
		Arrays.fill(byteArray, (byte) '*');
		return byteArray;
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		log(getClass().getName() + " processing an HTTP GET request.");
		response.setContentLength(-1);
		OutputStream outputStream = response.getOutputStream();
		for (int bytesServedCt = 0; bytesServedCt < bytesToServeCountTotal;) {
			outputStream.write(byteArray);
			bytesServedCt += byteArray.length;
			if (bytesServedCt % 8192 == 0) {
				outputStream.flush();
				response.flushBuffer();
			}
		}
		outputStream.close();
	}
}
