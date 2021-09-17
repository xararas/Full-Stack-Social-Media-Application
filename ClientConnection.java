package com.bham.fsd.assignments.jabberserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientConnection implements Runnable {
	private JabberDatabase jd;
	private Socket clientSocket;
	private String username = null;

	public ClientConnection(Socket clientSocket, JabberDatabase jd) {
		this.clientSocket = clientSocket;
		this.jd = jd;
		new Thread(this).start();

	}

	@Override
	public void run() {

		System.out.println("  Thread is running...");



			try {
				// Message for client
				ObjectOutputStream oos = new
						ObjectOutputStream(clientSocket.getOutputStream());

				// Message from client
				ObjectInputStream ois = new
						ObjectInputStream(clientSocket.getInputStream());

				JabberMessage request = (JabberMessage) ois.readObject();

				String input = request.getMessage();

				String[] userArray = input.split(" ");
				int user = 0;
				if(userArray.length >0 && (userArray[0].equals("signin") || userArray[0].equals("register")))
					user = jd.getUserID(userArray[1]);


				// P1a. Signing in (successful)
				if (user != -1) {
					this.username = userArray[1];
					JabberMessage jm = new JabberMessage("signedin");
					oos.writeObject(jm);
					oos.flush();
				}

				// P2. Registering
				if (user == -1 && userArray[0].equals("register")) {
					jd.addUser(userArray[1], "@gmail.com");
					JabberMessage jm = new JabberMessage("signedin");
					oos.writeObject(jm);
					oos.flush();
				}

				// P1b. Signing in (unsuccessful)
				if (user == -1 ) {
					JabberMessage jm1 = new JabberMessage("unknown-user");
					oos.writeObject(jm1);
					oos.flush();
				}
				while (true) {
					request = (JabberMessage) ois.readObject();

					input = request.getMessage();
					// P4. Getting the timeline
					if (input.equals("timeline")) {
						ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
						data = jd.getTimelineOfUserEx(user);
						JabberMessage jm = new JabberMessage("timeline", data);
						oos.writeObject(jm);
						oos.flush();
					}

					// P5. Getting the users (who to follow)
					if (input.equals("users")) {
						ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
						data = jd.getUsersNotFollowed(user);
						JabberMessage jm = new JabberMessage("users", data);
						oos.writeObject(jm);
						oos.flush();
					}

					//P6. Posting a jab
					if (userArray[0].equals("post")) {

						jd.addJab(username, userArray[1]);
						JabberMessage jm = new JabberMessage("posted");
						oos.writeObject(jm);
						oos.flush();
					}

					// P7. Liking a jab
					if (userArray[0].equals("like")) {

						int jabid = Integer.parseInt(userArray[1]);
						int userid = jd.getUserID(username);
						jd.addLike(userid, jabid);
						JabberMessage jm = new JabberMessage("posted");
						oos.writeObject(jm);
						oos.flush();

						// P8. Following a user.
						if (userArray[0].equals("follow")) {

							int userida = jd.getUserID(username);
							int useridb = jd.getUserID(userArray[1]);
							jd.addFollower(userida, useridb);
							oos.writeObject(jm);
							oos.flush();
						}

					}
				}
			} catch (Exception e) {				
				e.printStackTrace();
			}

	}
}