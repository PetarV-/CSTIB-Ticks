package uk.ac.cam.pv273.fjava.tick5;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import uk.ac.cam.cl.fjava.messages.RelayMessage;

public class Database 
{
	private Connection connection;
	
	public Database(String databasePath) throws SQLException
	{
		try
		{
			Class.forName("org.hsqldb.jdbcDriver");
		} 
		catch (ClassNotFoundException e) 
		{
			e.printStackTrace();
		}		
		connection = DriverManager.getConnection("jdbc:hsqldb:file:"
				+databasePath,"SA","");
		
		Statement delayStmt = connection.createStatement();
		try {delayStmt.execute("SET WRITE_DELAY FALSE");}  //Always update data on disk
		finally {delayStmt.close();}
		
		connection.setAutoCommit(false);
		
		Statement sqlStmt = connection.createStatement();
		try 
		{
			sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+
		                 	"message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
		}
		catch (SQLException e) 
		{
			System.out.println("Warning: Database table \"messages\" already exists.");
		} 
		finally 
		{
			sqlStmt.close();
		}
		
		Statement sqlStmt2 = connection.createStatement();
		try 
		{
			sqlStmt2.execute("CREATE TABLE statistics(key VARCHAR(255), value INT)");
			String stmt1 = "INSERT INTO statistics(key,value) VALUES ('Total messages',0)";
			String stmt2 = "INSERT INTO statistics(key,value) VALUES ('Total logins',0)";
			Statement sqlStmt3 = connection.createStatement();
			sqlStmt3.addBatch(stmt1);
			sqlStmt3.addBatch(stmt2);
			try 
			{
				sqlStmt3.executeBatch();
			} 
			finally 
			{ 
				sqlStmt3.close();
			}			
		}
		catch (SQLException e) 
		{
			System.out.println("Warning: Database table \"statistics\" already exists.");
		} 	
		finally 
		{
			sqlStmt2.close();
		}
		
		connection.commit();
	}
	
	public void close() throws SQLException
	{
		connection.close();
	}
	
	public void incrementLogins() throws SQLException
	{
		String stmt = "UPDATE statistics SET value = value+1 WHERE key='Total logins'";
		PreparedStatement updateMessage = connection.prepareStatement(stmt);
		try 
		{
			updateMessage.executeUpdate();
		} 
		finally 
		{ 
			updateMessage.close();
		}	
		connection.commit();
	}
	
	public synchronized void addMessage(RelayMessage m) throws SQLException
	{
		String stmt1 = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
		String stmt2 = "UPDATE statistics SET value = value+1 WHERE key='Total messages'";
		PreparedStatement insertMessage = connection.prepareStatement(stmt1);
		PreparedStatement updateMessage = connection.prepareStatement(stmt2);
		try 
		{
			insertMessage.setString(1, m.getFrom());
			insertMessage.setString(2, m.getMessage());
			insertMessage.setLong(3, m.getCreationTime().getTime());
			insertMessage.executeUpdate();
			updateMessage.executeUpdate();
		} 
		finally 
		{ 
			insertMessage.close();
			updateMessage.close();
		}
		connection.commit();
	}
	
	public List<RelayMessage> getRecent() throws SQLException
	{
		List<RelayMessage> ret = new ArrayList<RelayMessage>();
		String stmt = "SELECT nick,message,timeposted FROM messages "+
					"ORDER BY timeposted DESC LIMIT 10";
		PreparedStatement recentMessages = connection.prepareStatement(stmt);
		try 
		{
			ResultSet rs = recentMessages.executeQuery();
			try 
			{
				while (rs.next())
				{
					ret.add(new RelayMessage(rs.getString(1), rs.getString(2), new Date(rs.getLong(3))));
				}
			} 
			finally 
			{
				rs.close();
			}
		} 
		finally 
		{
			recentMessages.close();
		}
		Collections.reverse(ret);
		return ret;
	}
	
	public static void main(String[] args) 
	{
		String db_path;
		Connection connection;
		try
		{
			// Connect to the database
			db_path = args[0];
			Class.forName("org.hsqldb.jdbcDriver");
			
			connection = DriverManager.getConnection("jdbc:hsqldb:file:"
					+db_path,"SA","");
			
			Statement delayStmt = connection.createStatement();
			try {delayStmt.execute("SET WRITE_DELAY FALSE");}  //Always update data on disk
			finally {delayStmt.close();}
		}
		catch (ArrayIndexOutOfBoundsException | ClassNotFoundException | SQLException e)
		{
			System.err.println("Usage: java uk.ac.cam.pv273.fjava.tick5.Database <database name>");
			return;
		}
		try 
		{
			
			// Manually manage commits
			connection.setAutoCommit(false);
			
			// Create a new table
			Statement sqlStmt = connection.createStatement();
			try 
			{
				sqlStmt.execute("CREATE TABLE messages(nick VARCHAR(255) NOT NULL,"+
			                 	"message VARCHAR(4096) NOT NULL,timeposted BIGINT NOT NULL)");
			}
			catch (SQLException e) 
			{
				System.out.println("Warning: Database table \"messages\" already exists.");
			} 
			finally 
			{
				sqlStmt.close();
			}
			
			// Insert data into the table
			String stmt = "INSERT INTO MESSAGES(nick,message,timeposted) VALUES (?,?,?)";
			PreparedStatement insertMessage = connection.prepareStatement(stmt);
			try 
			{
				insertMessage.setString(1, "Alastair"); //set value of first "?" to "Alastair"
				insertMessage.setString(2, "Hello, Andy");
				insertMessage.setLong(3, System.currentTimeMillis());
				insertMessage.executeUpdate();
			} 
			finally 
			{ 
			   insertMessage.close();
			}
			
			// Commit the changes
			connection.commit();
			
			// Query data from the table
			stmt = "SELECT nick,message,timeposted FROM messages "+
					"ORDER BY timeposted DESC LIMIT 10";
			PreparedStatement recentMessages = connection.prepareStatement(stmt);
			try 
			{
				ResultSet rs = recentMessages.executeQuery();
				try 
				{
					while (rs.next())
						System.out.println(rs.getString(1)+": "+rs.getString(2)+
							" ["+rs.getLong(3)+"]");
				} 
				finally 
				{
					rs.close();
				}
			} 
			finally 
			{
				recentMessages.close();
			}
			
			// Close the connection
			connection.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
