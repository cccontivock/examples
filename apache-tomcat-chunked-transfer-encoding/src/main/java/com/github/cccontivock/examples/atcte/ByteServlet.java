package com.github.cccontivock.examples.atcte;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/bytes")
public class ByteServlet extends HttpServlet {
	private static final long serialVersionUID = -5405432244347991445L;
	private static final int BYTES_TO_SERVE_TOTAL_COUNT = 1024 * 1024;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.addHeader("Content-Length", "-1");
		OutputStream outputStream = response.getOutputStream();
		for(int bytesServedCt = 0; bytesServedCt < BYTES_TO_SERVE_TOTAL_COUNT; bytesServedCt++)
		{
			outputStream.write('*');
		}
	}
}
