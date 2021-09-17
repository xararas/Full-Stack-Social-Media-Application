package com.bham.fsd.assignments.jabberserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class JabberServer implements Runnable {

	private static final int PORT_NUMBER = 44444;
	private ServerSocket serverSocket;

	public JabberServer() throws IOException {	

		System.out.println("Server waiting for connection");

			serverSocket = new ServerSocket(PORT_NUMBER);
			new Thread(this).start();

			while (true) {

				serverSocket.setSoTimeout(10000000);

				Socket clientSocket = serverSocket.accept(); 
				System.out.println("  Client accepted...");
				ClientConnection client = new
						ClientConnection(clientSocket, new JabberDatabase());
				System.out.println("  ClientConnection started...");
			
				}
			}
	
	public static void main(String [] args) throws IOException  {

		JabberServer jb = new JabberServer();
	}

	@Override
	public void run() {

		while (true) {

			try {

				Thread.sleep(100);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}