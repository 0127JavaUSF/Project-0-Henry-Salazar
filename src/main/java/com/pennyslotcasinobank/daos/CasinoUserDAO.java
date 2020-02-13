package com.pennyslotcasinobank.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotcasinobank.exceptions.UsernameAlreadyExistsException;
import com.pennyslotcasinobank.exceptions.WrongLogInException;
import com.pennyslotscasinobank.ConnectionUtil;
import com.pennyslotscasinobank.User;

public class CasinoUserDAO implements UserDAO {
	
	//returns a list of users from the account number
	@Override
	public List<User> getUsersFromAcctNumber(int acctNumber) throws NetworkException, SQLException, InvalidAcctException {

		//connect to JDBC
		Connection connection = null;
		try {
			
			connection = ConnectionUtil.getConnection();
			
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}
			
			//start transaction
			connection.setAutoCommit(false);

			//get primary account holder
			String sql = "SELECT first_name, last_name, username FROM users WHERE id = (SELECT user_id FROM acct_holders WHERE acct_id = ? AND is_primary = true);";

			PreparedStatement prepared = connection.prepareStatement(sql);

			prepared.setInt(1, acctNumber);
			ResultSet result = prepared.executeQuery();

			List<User> users = new LinkedList<User>();
			if(result.next()) {
				
				User primary = new User();
				userFromResultSet(result, primary);
				
				//primary is always first in list
				users.add(primary);

				//get all secondary account holders
				sql = "SELECT first_name, last_name, username FROM users WHERE id IN (SELECT user_id FROM acct_holders WHERE acct_id = ? AND is_primary = false);";

				prepared = connection.prepareStatement(sql);

				prepared.setInt(1, acctNumber);
				result = prepared.executeQuery();
				
				while(result.next()) {
					
					//get user
					User user = new User();
					userFromResultSet(result, user);				
					users.add(user);
				}
				
				connection.commit();
				
				return users;
			}
			else {
				if(connection != null) {
					connection.rollback();
				}
				throw new InvalidAcctException();
			}			
		}
		catch (SQLException e) {
			if(connection != null) {
				connection.rollback();
			}
			throw new SQLException();
		}
		finally {
			
			if(connection != null) {
				connection.close();
			}
		}
	}
	
	//returns a User object given a username
	@Override
	public User getUserFromUsername(String username) throws NetworkException, SQLException, InvalidUserException {
		
		//connect to JDBC
		try(Connection connection = ConnectionUtil.getConnection()) {
			
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}

			//get user
			String sql = "SELECT first_name, last_name FROM users WHERE username = ?;";

			PreparedStatement prepared = connection.prepareStatement(sql);

			prepared.setString(1, username);
			ResultSet result = prepared.executeQuery();

			//if username in database
			if(result.next()) {

				User user = new User();
				user.Init(
						result.getString("first_name"),
						result.getString("last_name"),
						username);
				
				return user;
			}
			else {
				throw new InvalidUserException();
			}

		} catch (SQLException e) {
			
			throw new SQLException();
		}
	}
	
	//verifies username and password in database
	@Override
	public void logIn(User user, String username, String password) throws NetworkException, SQLException, WrongLogInException {
		
		//connect to JDBC
		try(Connection connection = ConnectionUtil.getConnection()) {
			
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}

			String sql = "SELECT first_name, last_name, password FROM users WHERE username = ?;";

			PreparedStatement prepared = connection.prepareStatement(sql);

			prepared.setString(1, username);
			ResultSet result = prepared.executeQuery();

			//if username in database
			if(result.next()) {
				String passwordInDB = result.getString("password");
				
				//if password does NOT match
				if(!passwordInDB.equals(password)) {
					throw new WrongLogInException();
				}
				
				//set first and last name
				user.Init(
						result.getString("first_name"),
						result.getString("last_name"),
						username);
			}
			else {
				throw new WrongLogInException();
			}

		} catch (SQLException e) {
			
			throw new SQLException();
		}
	}
	
	//register new user
	@Override
	public void register(String firstName, String lastName, String username, String password) throws NetworkException, SQLException, UsernameAlreadyExistsException {
		
		//connect to JDBC
		try(Connection connection = ConnectionUtil.getConnection()) {
			
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}

			//check if username is already in database
			String sql = "SELECT 1 FROM users WHERE username = ?;";

			PreparedStatement prepared = connection.prepareStatement(sql);

			prepared.setString(1, username);
			ResultSet result = prepared.executeQuery();
			if(result.next()) {
				
				//throw username exists exception
				throw new UsernameAlreadyExistsException();
			}
			
			//insert user into database
			sql = "INSERT INTO users (first_name, last_name, username, password) VALUES (?, ?, ?, ?);";

			prepared = connection.prepareStatement(sql);
			
			prepared.setString(1, firstName);
			prepared.setString(2, lastName);
			prepared.setString(3, username);
			prepared.setString(4, password);
			
			prepared.execute();

		} catch (SQLException e) {
			
			throw new SQLException();
		}
	}
	
	//helper function to initialize user
	private void userFromResultSet(ResultSet result, User user) throws SQLException {
		
		user.Init(
				result.getString("first_name"),
				result.getString("last_name"),
				result.getString("username"));
	}
}
