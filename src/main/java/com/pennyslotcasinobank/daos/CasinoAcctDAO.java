package com.pennyslotcasinobank.daos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.pennyslotcasinobank.exceptions.ExceedsBalanceException;
import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.ConnectionUtil;

public class CasinoAcctDAO implements AcctDAO {
	
	//returns the account number
	@Override
	public Account createAcct(Account.Type type, String username) throws NetworkException, SQLException, InvalidUserException {
				
		//connect to JDBC
		Connection connection = null;
		try {
			connection = ConnectionUtil.getConnection();
					
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}
			
			//transaction
			//both the account and account holder need to be created at the same time
			connection.setAutoCommit(false);

			String sql = "INSERT INTO accts (type) VALUES (?) RETURNING id;";

			PreparedStatement prepared = connection.prepareStatement(sql);
			prepared.setInt(1, Account.toSQLType(type));
			
			ResultSet result = prepared.executeQuery();

			if(result.next()) { //if success

				String acctNumString = result.getString("id");
				int acctNum = Integer.parseInt(acctNumString);
				
				//get user id
				sql = "SELECT id FROM users WHERE username = ?";
				prepared = connection.prepareStatement(sql);
				prepared.setString(1, username);
				
				result = prepared.executeQuery();
				if(result.next()) {
					UUID userID = java.util.UUID.fromString( result.getString("id") );
					
					sql = "INSERT INTO acct_holders (user_id, acct_id, is_primary) VALUES (?, ?, ?);";
					
					prepared = connection.prepareStatement(sql);
					prepared.setObject(1, userID); //UUID requires setObject
					prepared.setInt(2, acctNum);
					prepared.setBoolean(3, true);
					
					prepared.execute();
					
					//end transaction
					connection.commit();
					
					//update client
					Account acct = new Account(type, new BigDecimal(0), acctNum);
					
					return acct;
				}
				else {
					if(connection != null) {
						connection.rollback();
					}
					throw new InvalidUserException();
				}
			}
			else {
				if(connection != null) {
					connection.rollback();
				}
				throw new SQLException();
			}

		}
		catch (SQLException e) {
			
			if(connection != null) {
				connection.rollback();
			}
			throw new SQLException();
		}
		finally {
			
			//close connection
			if(connection != null) {
				connection.close();
			}
		}
	}
	
	@Override
	public void closeAcct(int acctNumber) throws NetworkException, SQLException, InvalidAcctException {
				
		//connect to JDBC
		Connection connection = null;
		try {
			connection = ConnectionUtil.getConnection();
					
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}
			
			//transaction
			connection.setAutoCommit(false);

			String sql = "DELETE FROM acct_holders where acct_id = ?;";

			PreparedStatement prepared = connection.prepareStatement(sql);
			prepared.setInt(1, acctNumber);
			
			prepared.execute();

			//delete acct
			sql = "DELETE FROM accts where id = ?;";
			
			prepared = connection.prepareStatement(sql);
			prepared.setInt(1, acctNumber);
			
			prepared.execute();
			
			connection.commit();
		}
		catch (SQLException e) {
			
			if(connection != null) {
				connection.rollback();
			}
			throw new SQLException();
		}
		finally {
			
			//close connection
			if(connection != null) {
				connection.close();
			}
		}
	}
	
	@Override
	public Account getFromAcctNum(int acctNumber) throws NetworkException, SQLException, InvalidAcctException {
		
		//connect to JDBC
		try(Connection connection = ConnectionUtil.getConnection()) {
			
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}

			String sql = "SELECT * from accts WHERE id = ?;";

			PreparedStatement prepared = connection.prepareStatement(sql);

			prepared.setInt(1, acctNumber);
			ResultSet result = prepared.executeQuery();

			if(result.next()) {
				
				Account acct = new Account();
				
				Account.Type type = Account.fromSQLType( result.getInt("type") );
				acct.Init(
						type, 
						result.getBigDecimal("balance"), 
						result.getInt("id"));
				
				return acct;
			}
			else {
				throw new InvalidAcctException();
			}

		} catch (SQLException e) {
			
			throw new SQLException();
		}
	}
	
	//returns the new balance
	@Override
	public BigDecimal deposit(BigDecimal deposit, int acctNumber) throws NetworkException, SQLException, InvalidAcctException {

		if(deposit.doubleValue() < 0) {
			deposit = deposit.multiply(new BigDecimal(-1.0));
		}
		
		//connect to JDBC
		try(Connection connection = ConnectionUtil.getConnection()) {
			
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}

			BigDecimal newBalance = depositA(connection, deposit, acctNumber);
			
			return newBalance;

		} catch (SQLException e) {
			
			throw new SQLException();
		}
	}
	
	private BigDecimal depositA(Connection connection, BigDecimal deposit, int acctNumber) throws NetworkException, SQLException, InvalidAcctException {
	
		String sql = "UPDATE accts SET balance = balance + ? WHERE id = ? RETURNING balance;";

		PreparedStatement prepared = connection.prepareStatement(sql);

		prepared.setObject(1, deposit);
		prepared.setInt(2, acctNumber);
		ResultSet result = prepared.executeQuery();

		if(result.next()) {
			
			BigDecimal newBalance = new BigDecimal( result.getDouble("balance") );
			return newBalance;
		}
		else {
			throw new InvalidAcctException();
		}
	}
	
	public List<Account> getAccts(String username) throws NetworkException, SQLException, InvalidUserException {
		
		//connect to JDBC
		try(Connection connection = ConnectionUtil.getConnection()) {
			
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}

			String sql = "SELECT * from accts WHERE id IN (SELECT acct_id FROM acct_holders WHERE user_id = (SELECT id from users WHERE username = ?));";

			PreparedStatement prepared = connection.prepareStatement(sql);

			prepared.setString(1, username);
			ResultSet result = prepared.executeQuery();

			List<Account> accts = new LinkedList<Account>();
			boolean hasResult = false;
			while(result.next()) {
				
				hasResult = true;
				
				Account.Type type =  Account.fromSQLType( result.getInt("type") );
				Account acct = new Account(
						type,
						result.getBigDecimal("balance"),
						result.getInt("id")
						);
				
				accts.add(acct);
			}
			
			if(!hasResult){
				throw new InvalidUserException();
			}
			
			return accts;

		} catch (SQLException e) {
			
			throw new SQLException();
		}
	}
	
	//returns the new balance
	@Override
	public BigDecimal withdrawal(BigDecimal withdrawal, int acctNumber) throws NetworkException, SQLException, ExceedsBalanceException, InvalidAcctException {

		if(withdrawal.doubleValue() < 0) {
			withdrawal = withdrawal.multiply(new BigDecimal(-1.0));
		}
		
		//connect to JDBC
		Connection connection = null;
		try {
			connection = ConnectionUtil.getConnection();
					
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}
			
			//transaction
			connection.setAutoCommit(false);

			BigDecimal newBalance = withdrawalA(connection, withdrawal, acctNumber);
			
			connection.commit();
			
			return newBalance;
		}
		catch (SQLException e) {
			
			if(connection != null) {
				connection.rollback();
			}
			throw new SQLException();
		}
		finally {
			
			//close connection
			if(connection != null) {
				connection.close();
			}
		}
	}
	
	private BigDecimal withdrawalA(Connection connection, BigDecimal withdrawal, int acctNumber) throws NetworkException, SQLException, ExceedsBalanceException, InvalidAcctException {
		
		String sql = "SELECT balance FROM accts WHERE id = ?;";

		PreparedStatement prepared = connection.prepareStatement(sql);
		prepared.setInt(1, acctNumber);
		
		ResultSet result = prepared.executeQuery();
		if(result.next()) {
			
			BigDecimal balance = new BigDecimal(result.getDouble("balance"));
			
			if(balance.doubleValue() - withdrawal.doubleValue() < 0) {
				if(connection != null) {
					connection.rollback();
				}
				throw new ExceedsBalanceException();
			}
			
			sql = "UPDATE accts SET balance = balance - ? WHERE id = ? RETURNING balance;";

			prepared = connection.prepareStatement(sql);

			prepared.setObject(1, withdrawal);
			prepared.setInt(2, acctNumber);
			result = prepared.executeQuery();

			if(result.next()) {
				
				BigDecimal newBalance = new BigDecimal( result.getDouble("balance") );
								
				return newBalance;
			}
			else {
				if(connection != null) {
					connection.rollback();
				}
				throw new InvalidAcctException();
			}
		}
		else {
			if(connection != null) {
				connection.rollback();
			}
			throw new InvalidAcctException();
		}
	}
	
	@Override
	public void transfer(BigDecimal amount, int fromAcctNumber, int toAcctNumber) throws NetworkException, SQLException, ExceedsBalanceException, InvalidAcctException {
		
		if(fromAcctNumber == toAcctNumber) {
			throw new InvalidAcctException();
		}
		
		//connect to JDBC
		Connection connection = null;
		try {
			connection = ConnectionUtil.getConnection();
					
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}
			
			//transaction
			connection.setAutoCommit(false);

			withdrawalA(connection, amount, fromAcctNumber);

			depositA(connection, amount, toAcctNumber);
			
			connection.commit();
		}
		catch (SQLException e) {
			
			if(connection != null) {
				connection.rollback();
			}
			throw new SQLException();
		}
		finally {
			
			//close connection
			if(connection != null) {
				connection.close();
			}
		}
	}
	
	//assumes user added to account is a secondary account holder because primary should already be added
	@Override
	public void addUser(String username, int acctNumber) throws NetworkException, SQLException, InvalidAcctException, InvalidUserException {

		//connect to JDBC
		Connection connection = null;
		try {
			connection = ConnectionUtil.getConnection();
					
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}
			
			//transaction
			connection.setAutoCommit(false);

			String sql = "SELECT id FROM users WHERE username = ?;";

			PreparedStatement prepared = connection.prepareStatement(sql);
			prepared.setString(1, username);
			
			ResultSet result = prepared.executeQuery();
			if(result.next()) {
				
				UUID uuid = java.util.UUID.fromString( result.getString("id") );
				
				//check if already an account holder
				sql = "SELECT 1 FROM acct_holders WHERE acct_id = ? AND user_id = ?";

				prepared = connection.prepareStatement(sql);
				prepared.setInt(1, acctNumber);
				prepared.setObject(2, uuid);
				
				//if already an account holder
				result = prepared.executeQuery();
				if(result.next()) {
				
					if(connection != null) {
						connection.rollback();
					}
					//throw invalid user exception
					throw new InvalidUserException();
				}
				
				sql = "INSERT INTO acct_holders (acct_id, user_id, is_primary) VALUES (?, ?, false) RETURNING id;";
				
				prepared = connection.prepareStatement(sql);
				prepared.setInt(1, acctNumber);
				prepared.setObject(2, uuid);
				
				result = prepared.executeQuery();
				if(result.next()) {
					
					connection.commit();
				}
				else {
					if(connection != null) {
						connection.rollback();
					}
					throw new InvalidAcctException();
				}
			}
			else {
				if(connection != null) {
					connection.rollback();
				}
				throw new InvalidUserException();
			}
		}
		catch (SQLException e) {
			
			if(connection != null) {
				connection.rollback();
			}
			throw new SQLException();
		}
		finally {
			
			//close connection
			if(connection != null) {
				connection.close();
			}
		}
	}
	
	@Override
	public void removeUser(String username, int acctNumber) throws NetworkException, SQLException, InvalidAcctException, InvalidUserException {
		
		//connect to JDBC
		Connection connection = null;
		try {
			connection = ConnectionUtil.getConnection();
					
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}
			
			//transaction
			connection.setAutoCommit(false);

			String sql = "SELECT id FROM users WHERE username = ?;";

			PreparedStatement prepared = connection.prepareStatement(sql);
			prepared.setString(1, username);
			
			ResultSet result = prepared.executeQuery();
			if(result.next()) {
				
				UUID uuid = java.util.UUID.fromString( result.getString("id") );
				
				sql = "DELETE FROM acct_holders WHERE acct_id = ? AND user_id = ? RETURNING 1;";
				
				prepared = connection.prepareStatement(sql);
				prepared.setInt(1, acctNumber);
				prepared.setObject(2, uuid);
				
				result = prepared.executeQuery();
				if(result.next()) {
					
					connection.commit();
				}
				else {
					if(connection != null) {
						connection.rollback();
					}
					throw new InvalidAcctException();
				}
			}
			else {
				if(connection != null) {
					connection.rollback();
				}
				throw new InvalidUserException();
			}
		}
		catch (SQLException e) {
			
			if(connection != null) {
				connection.rollback();
			}
			throw new SQLException();
		}
		finally {
			
			//close connection
			if(connection != null) {
				connection.close();
			}
		}
	}
}
