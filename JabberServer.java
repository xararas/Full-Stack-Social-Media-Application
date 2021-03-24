package com.bham.fsd.assignments.jabberserver;

// Charalampos Kalakos 2165173

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.PreparedStatement;

public class JabberServer {
	
	private static String dbcommand = "jdbc:postgresql://127.0.0.1:5432/postgres";
	private static String db = "postgres";
	private static String pw = "";

	private static Connection conn;
	
	public static Connection getConnection() {
		return conn;
	}

	public static void main(String[] args) {
				
		JabberServer jabber = new JabberServer();
		JabberServer.connectToDatabase();
		jabber.resetDatabase(); 	
		
		//print1(jabber.getFollowerUserIDs(2));
		//print1(jabber.getFollowingUserIDs(0));
		//print2(jabber.getLikesOfUser(0));
		//print2(jabber.getTimelineOfUser(7));
		//jabber.addJab("ellie", "the last of us part 2");
		//jabber.addUser("haris", "haris@hotmail.com");
		//jabber.addFollower(14, 10);
		//jabber.addLike(11, 8);
		//print1(jabber.getUsersWithMostFollowers());
		//print2(jabber.getMutualFollowUserIDs());
	}
	
	public ArrayList<String> getFollowerUserIDs(int userid) {

		ArrayList<String> ret = new ArrayList<String>();

		try {

			PreparedStatement stmt = conn.prepareStatement("SELECT userid FROM jabberuser INNER JOIN follows ON jabberuser.userid = follows.userida AND follows.useridb = ?");
			stmt.setInt(1, userid);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				ret.add(rs.getObject("userid").toString());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public ArrayList<String> getFollowingUserIDs(int userid) {

		ArrayList<String> ret = new ArrayList<String>();

		try {

			PreparedStatement stmt = conn.prepareStatement("SELECT userid FROM jabberuser INNER JOIN follows ON jabberuser.userid = follows.useridb AND follows.userida = ?");
			stmt.setInt(1, userid);

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				ret.add(rs.getObject("userid").toString());
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;
	}
	
	public ArrayList<ArrayList<String>> getMutualFollowUserIDs() {
		
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();

		try {

			PreparedStatement stmt = conn.prepareStatement("SELECT a.userida, b.useridb FROM follows AS a INNER JOIN follows as b ON(a.userida = b.useridb AND a.useridb = b.userida) group by a.userida, b.useridb");		
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				ArrayList<String> r = new ArrayList<String>();
				r.add(rs.getObject("userida").toString());
				r.add(rs.getObject("useridb").toString());
				ret.add(r);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;	
	}

	public ArrayList<ArrayList<String>> getLikesOfUser(int userid) {
		
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();

		try {

			PreparedStatement stmt = conn.prepareStatement("SELECT username, jabtext FROM jabberuser NATURAL JOIN jab INNER JOIN likes ON jab.jabid = likes.jabid AND likes.userid = ?");
		
			stmt.setInt(1, userid);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				ArrayList<String> r = new ArrayList<String>();
				r.add(rs.getObject("username").toString());
				r.add(rs.getObject("jabtext").toString());
				ret.add(r);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;	
	}
	
	public ArrayList<ArrayList<String>> getTimelineOfUser(int userid) {
		
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();

		try {

			PreparedStatement stmt = conn.prepareStatement("SELECT username, jabtext FROM jabberuser NATURAL JOIN jab INNER JOIN follows ON jab.userid = follows.useridb AND follows.userida = ?");
		
			stmt.setInt(1, userid);
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				ArrayList<String> r = new ArrayList<String>();
				r.add(rs.getObject("username").toString());
				r.add(rs.getObject("jabtext").toString());
				ret.add(r);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return ret;	
	}

	public void addJab(String username, String jabtext) {
		
		int newjabid = getNextJabID();
		
		try {
			
			PreparedStatement stmt = conn.prepareStatement("insert into jab values(?,(SELECT userid FROM jabberuser WHERE username = ?),?) ");
			
			stmt.setInt(1, newjabid);
			stmt.setString(2, username);
			stmt.setString(3, jabtext);
			
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
	}
	
	private int getNextJabID() {
		
		String query = "select max(jabid) from jab";
		
		int maxid = -1;
		
		try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				maxid = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (maxid < 0) {
			return maxid;
		}
		
		return maxid + 1;
	}
	
	
	public void addUser(String username, String emailadd) {
		
		int newuserid = getNextUserID();
		
		try {
			
			PreparedStatement stmt = conn.prepareStatement("insert into jabberuser values(?,?,?)");
			
			stmt.setInt(1, newuserid);
			stmt.setString(2, username);
			stmt.setString(3, emailadd);
			
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	
private int getNextUserID() {
		
		String query = "select max(userid) from jabberuser";
		
		int maxid = -1;
		
		try (Statement stmt = conn.createStatement()) {
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				maxid = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if (maxid < 0) {
			return maxid;
		}
		
		return maxid + 1;
}
	
	public void addFollower(int userida, int useridb) {
		
		try {
			
			PreparedStatement stmt = conn.prepareStatement("insert into follows values(?,?)");
			
			stmt.setInt(1,userida);
			stmt.setInt(2,useridb);
			
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addLike(int userid, int jabid) {
		
		try {
			
			PreparedStatement stmt = conn.prepareStatement("insert into likes values(?,?)");
			
			stmt.setInt(1,userid);
			stmt.setInt(2,jabid);
			
			stmt.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getUsersWithMostFollowers() {
		
ArrayList<String> ret = new ArrayList<String>();
		
		try {
			
			PreparedStatement stmt = conn.prepareStatement("select userid from jabberuser natural join follows group by userid having count(userid) >= all (select count(userida) from follows group by userida order by count(userida) desc)");
			
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				ret.add(rs.getObject("userid").toString());
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
		return ret;	
			
	}
	
	public JabberServer() {}
	
	public static void connectToDatabase() {

		try {
			conn = DriverManager.getConnection(dbcommand,db,pw);

		}catch(Exception e) {		
			e.printStackTrace();
		}
	}

	/*
	 * Utility method to print an ArrayList of ArrayList<String>s to the console.
	 */
	private static void print2(ArrayList<ArrayList<String>> list) {
		
		for (ArrayList<String> s: list) {
			print1(s);
			System.out.println();
		}
	}
		
	/*
	 * Utility method to print an ArrayList to the console.
	 */
	private static void print1(ArrayList<String> list) {
		
		for (String s: list) {
			System.out.print(s + " ");
		}
	}

	public void resetDatabase() {
		
		dropTables();
		
		ArrayList<String> defs = loadSQL("jabberdef");
	
		ArrayList<String> data =  loadSQL("jabberdata");
		
		executeSQLUpdates(defs);
		executeSQLUpdates(data);
	}
	
	private void executeSQLUpdates(ArrayList<String> commands) {
	
		for (String query: commands) {
			
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList<String> loadSQL(String sqlfile) {
		
		ArrayList<String> commands = new ArrayList<String>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(sqlfile + ".sql"));
			
			String command = "";
			
			String line = "";
			
			while ((line = reader.readLine())!= null) {
				
				if (line.contains(";")) {
					command += line;
					command = command.trim();
					commands.add(command);
					command = "";
				}
				
				else {
					line = line.trim();
					command += line + " ";
				}
			}
			
			reader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return commands;
		
	}

	private void dropTables() {
		
		String[] commands = {
				"drop table jabberuser cascade;",
				"drop table jab cascade;",
				"drop table follows cascade;",
				"drop table likes cascade;"};
		
		for (String query: commands) {
			
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}
