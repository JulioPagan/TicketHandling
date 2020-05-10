package javaapplication1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;

public class Dao {
	// instance fields
	static Connection connect = null;
	static Statement statement = null;
	static String userLoggedIn;
	static Boolean chkIfUserIsAdmin;

	int id = 0;

	// constructor
	public Dao() {

	}

	public Connection getConnection() {
		// Setup the connection with the DB
		try {
			connect = DriverManager
					.getConnection("jdbc:mysql://www.papademas.net:3307/tickets?autoReconnect=true&useSSL=false"
							+ "&user=fp411&password=411");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connect;
	}

	// CRUD implementation

	public void createTables() {
		// variables for SQL Query table creations
		final String createTicketsTable = "CREATE TABLE jpaga_tickets(ticket_id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "issuer_username VARCHAR(30), ticket_issuer VARCHAR(30), ticket_description VARCHAR(200), start_date DATE, close_date DATE)";
		final String createUsersTable = "CREATE TABLE jpaga_users(uid INT AUTO_INCREMENT PRIMARY KEY, uname VARCHAR(30), upass VARCHAR(30), admin int)";
		final String createTicketHistoryTable = "CREATE TABLE jpaga_ticket_history(ticket_history_id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "ticket_id INT, action_taken VARCHAR(30), issuer_username VARCHAR(30), ticket_issuer VARCHAR(30), "
				+ "ticket_description VARCHAR(200), start_date DATE, close_date DATE)";

		try {

			// execute queries to create tables

			statement = getConnection().createStatement();

			statement.executeUpdate(createTicketHistoryTable);
			statement.executeUpdate(createTicketsTable);
			statement.executeUpdate(createUsersTable);
			System.out.println("Created tables in given database...");

			// end create table
			// close connection/statement object
			statement.close();
			connect.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		// add users to user table
		addUsers();
	}

	public void addUsers() {
		// add list of users from userlist.csv file to users table

		// variables for SQL Query inserts
		String sql;

		Statement statement;
		BufferedReader br;
		List<List<String>> array = new ArrayList<>(); // list to hold (rows & cols)

		// read data from file
		try {
			br = new BufferedReader(new FileReader(new File("./userlist.csv")));

			String line;
			while ((line = br.readLine()) != null) {
				array.add(Arrays.asList(line.split(",")));
			}
		} catch (Exception e) {
			System.out.println("There was a problem loading the file");
		}

		try {

			// Setup the connection with the DB

			statement = getConnection().createStatement();
			// DELETE previus entries of users in DB to enter new ones
			statement.executeUpdate("DELETE FROM jpaga_users");
			// create loop to grab each array index containing a list of values
			// and PASS (insert) that data into your User table
			for (List<String> rowData : array) {

				sql = "insert into jpaga_users(uname,upass,admin) " + "values('" + rowData.get(0) + "'," + " '"
						+ rowData.get(1) + "','" + rowData.get(2) + "');";
				statement.executeUpdate(sql);
			}
			System.out.println("Inserts completed in the given database...");

			// close statement object
			statement.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public int insertRecords(String ticketName, String ticketDesc, String sdate) {
		try {
			statement = getConnection().createStatement();
			statement.executeUpdate(
					"Insert into jpaga_tickets"
							+ "(issuer_username, ticket_issuer, ticket_description, start_date) values('" + userLoggedIn
							+ "','" + ticketName + "','" + ticketDesc + "','" + sdate + "')",
					Statement.RETURN_GENERATED_KEYS);

			// retrieve ticket id number newly auto generated upon record insertion
			ResultSet resultSet = null;
			resultSet = statement.getGeneratedKeys();
			if (resultSet.next()) {
				// retrieve first field in table
				id = resultSet.getInt(1);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Insert query into History Record Table
		String action_taken = "insertTicket";
		try {
			statement.executeUpdate(
					"Insert into jpaga_ticket_history(ticket_id, action_taken, issuer_username, ticket_issuer, ticket_description, start_date) values('"
							+ id + "','" + action_taken + "','" + userLoggedIn + "','" + ticketName + "','" + ticketDesc
							+ "','" + sdate + "')");
			System.out.println("Ticket history database updated successfully!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;
	}

	public static ResultSet readRecords() {

		ResultSet results = null;
		try {
			statement = connect.createStatement();
			if(chkIfUserIsAdmin){
				results = statement.executeQuery("SELECT * FROM jpaga_tickets");
			}else{
				results = statement.executeQuery("SELECT * FROM jpaga_tickets WHERE issuer_username='" + userLoggedIn + "'");
			}
			//connect.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		return results;
	}

	public ResultSet readHistoryRecords(){
		ResultSet results = null;
		try{
			statement = connect.createStatement();
			results =  statement.executeQuery("SELECT * FROM jpaga_ticket_history");
		}catch(SQLException e1){
			e1.printStackTrace();
		}
		return results;
	}



	// continue coding for updateRecords implementation
	public void updateRecords(int ticketID, String newDesc){
		// Execute update  query
		System.out.println("Creating update statement...");
		try {
			statement = getConnection().createStatement();
			String sql = "UPDATE jpaga_tickets " +
					"SET ticket_description='" + newDesc + "' WHERE ticket_id='" + ticketID + "'";
			statement.executeUpdate(sql);
		System.out.println("Updated ticket successfully!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Insert query into History Table
		String action_taken = "updateTicket";
		try {
			statement.executeUpdate(
					"Insert into jpaga_ticket_history(ticket_id, action_taken, issuer_username, ticket_description) values('"
							+ ticketID + "','" + action_taken + "','" + userLoggedIn + "','" + newDesc + "')");
			System.out.println("Changes documented in history database");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void closeRecord(int ticketID, String cdate){

		try {
			statement = getConnection().createStatement();
			String sql = "UPDATE jpaga_tickets " +
					"SET close_date='" + cdate + "' WHERE ticket_id='" + ticketID + "'";
			statement.executeUpdate(sql);

			System.out.println("Ticket " + ticketID + " closed successfully!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Insert query into History Table
		String action_taken = "closeTicket";
		try {
			statement.executeUpdate(
					"Insert into jpaga_ticket_history(ticket_id, action_taken, issuer_username, close_date) values('"
							+ ticketID + "','" + action_taken + "','" + userLoggedIn + "','" + cdate + "')");
			System.out.println("Ticket history database updated successfully!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// continue coding for deleteRecords implementation
	public void deleteRecords(int ticketID){
		// Execute delete  query
		System.out.println("Creating statement...");
		try{
		statement = getConnection().createStatement();
		String sql = "DELETE FROM jpaga_tickets  " +
					"WHERE ticket_id='" +  ticketID + "'";

		int response = JOptionPane.showConfirmDialog(null, "Delete ticket #"  + ticketID + "?", "Confirm",  JOptionPane.YES_NO_OPTION, 
					JOptionPane.QUESTION_MESSAGE);
		
		if (response == JOptionPane.NO_OPTION) {
		System.out.println("No record deleted");
		} else if (response == JOptionPane.YES_OPTION) {
		statement.executeUpdate(sql);
		System.out.println("Record deleted successfully!");
		//Insert query into History Table

		String action_taken = "deletedTicket";
		try {
			statement.executeUpdate(
					"Insert into jpaga_ticket_history(ticket_id, action_taken, issuer_username) values('"
							+ ticketID + "','" + action_taken + "','" + userLoggedIn + "')");
			System.out.println("Ticket history database updated successfully!");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		} else if (response == JOptionPane.CLOSED_OPTION) {
		System.out.println("Request cancelled");
		}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
