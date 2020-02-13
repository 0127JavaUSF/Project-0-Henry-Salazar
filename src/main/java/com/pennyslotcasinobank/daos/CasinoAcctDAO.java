package com.pennyslotcasinobank.daos;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.pennyslotcasinobank.exceptions.BalanceNot0Exception;
import com.pennyslotcasinobank.exceptions.ExceedsBalanceException;
import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.ConnectionUtil;
import com.pennyslotscasinobank.Transaction;

public class CasinoAcctDAO implements AcctDAO {
	
	//creates an account
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

			//create account
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
					
					//create account holder
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
	
	//closes the account
	@Override
	public void closeAcct(int acctNumber) throws NetworkException, SQLException, BalanceNot0Exception, InvalidAcctException {
				
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
			
			//get balance
			String sql = "Select balance from accts WHERE id = ?;";
			
			PreparedStatement prepared = connection.prepareStatement(sql);
			prepared.setInt(1, acctNumber);
			
			//if balance > one penny
			ResultSet result = prepared.executeQuery();
			if(result.next()) {
				BigDecimal balance = result.getBigDecimal("balance");
				if(balance.doubleValue() > .01) {
					
					if(connection != null) {			
						connection.rollback();
					}
					//can not close account if balance
					throw new BalanceNot0Exception();
				}
			}

			//first delete account holders
			sql = "DELETE FROM acct_holders where acct_id = ?;";

			prepared = connection.prepareStatement(sql);
			prepared.setInt(1, acctNumber);
			
			prepared.execute();
			
			//delete all transactions with acct number
			sql = "DELETE FROM transactions where acct_id = ?;";

			prepared = connection.prepareStatement(sql);
			prepared.setInt(1, acctNumber);
			
			prepared.execute();
			
			//delete all transfers with acct number
			//in real life the acct would not be deleted because acct history would need to be preserved
			sql = "DELETE FROM transfers where from_acct_id = ? OR to_acct_id = ?;";

			prepared = connection.prepareStatement(sql);
			prepared.setInt(1, acctNumber);
			prepared.setInt(2, acctNumber);
			
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
	
	//get the account from the account number
	@Override
	public Account getFromAcctNum(int acctNumber) throws NetworkException, SQLException, InvalidAcctException {
		
		//connect to JDBC
		try(Connection connection = ConnectionUtil.getConnection()) {
			
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}

			//get account from account number
			String sql = "SELECT * from accts WHERE id = ?;";

			PreparedStatement prepared = connection.prepareStatement(sql);

			prepared.setInt(1, acctNumber);
			ResultSet result = prepared.executeQuery();

			if(result.next()) {
				
				Account acct = new Account();
				
				//convert from SQL type to Java account type
				Account.Type type = Account.fromSQLType( result.getInt("type") );
				acct.Init(
						type, 
						result.getBigDecimal("balance"), 
						result.getInt("id"));
				
				return acct; //return new account
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
	public BigDecimal deposit(BigDecimal deposit, String username, int acctNumber) throws NetworkException, SQLException, InvalidAcctException, InvalidUserException {

		if(deposit.doubleValue() < 0) {
			deposit = deposit.multiply(new BigDecimal(-1.0));
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
			
			BigDecimal newBalance = depositA(connection, deposit, acctNumber);
			
			//insert into transactions table
			
			//get user id first
			String sql = "SELECT id FROM users WHERE username = ?;";
			
			PreparedStatement prepared = connection.prepareStatement(sql);
			prepared.setString(1, username);
			
			ResultSet result = prepared.executeQuery();
			if(result.next()) {
				
				UUID userID = java.util.UUID.fromString( result.getString("id") );
			
				//create transaction record
				sql = "INSERT INTO transactions (user_id, acct_id, type, amount) VALUES (?, ?, 1, ?);";
				
				prepared = connection.prepareStatement(sql);
				prepared.setObject(1, userID);
				prepared.setInt(2, acctNumber);
				prepared.setObject(3, deposit);
				
				prepared.execute();
				
				connection.commit();
				
				return newBalance;
			}
			else {
				if(connection != null) {
					connection.rollback();
				}
				throw new InvalidUserException();
			}
			
		} catch (SQLException e) {
			
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
	
	private BigDecimal depositA(Connection connection, BigDecimal deposit, int acctNumber) throws NetworkException, SQLException, InvalidAcctException {
	
		//update balance
		String sql = "UPDATE accts SET balance = balance + ? WHERE id = ? RETURNING balance;";

		PreparedStatement prepared = connection.prepareStatement(sql);

		prepared.setObject(1, deposit);
		prepared.setInt(2, acctNumber);
		ResultSet result = prepared.executeQuery();

		if(result.next()) {
			
			BigDecimal newBalance = result.getBigDecimal("balance");
			return newBalance;
		}
		else {
			throw new InvalidAcctException();
		}
	}
	
	//given the username, returns a list of accounts
	public List<Account> getAccts(String username) throws NetworkException, SQLException, InvalidUserException {
		
		//connect to JDBC
		try(Connection connection = ConnectionUtil.getConnection()) {
			
			//if connection issue
			if(connection == null) {
				throw new NetworkException();
			}

			//nested query
			//get accounts with matching account numbers (from account holders table). username may have multiple accounts
			String sql = "SELECT * from accts WHERE id IN (SELECT acct_id FROM acct_holders WHERE user_id = (SELECT id from users WHERE username = ?));";

			PreparedStatement prepared = connection.prepareStatement(sql);

			prepared.setString(1, username);
			ResultSet result = prepared.executeQuery();

			List<Account> accts = new LinkedList<Account>();
			boolean hasResult = false;
			while(result.next()) { //loop over results
				
				hasResult = true;
				
				//create account and add to List<>
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
	
	//returns a list of recent transactions
	public List<Transaction> getRecentActivity(int acctNumber) throws NetworkException, SQLException {
		
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

			//get transaction history sorted by date
			String sql = "SELECT date, type, amount FROM transactions WHERE acct_id = ? ORDER BY date DESC LIMIT 32";
			
			PreparedStatement prepared = connection.prepareStatement(sql);
			prepared.setInt(1, acctNumber);

			List<Transaction> transactions = new LinkedList<Transaction>();
			ResultSet result = prepared.executeQuery();
			while(result.next()) {
				
				Transaction trans = new Transaction();
				trans.Init(
						new BigDecimal( result.getDouble("amount") ),
						result.getTimestamp("date"),
						0,
						0,
						result.getInt("type"));
				
				transactions.add(trans);
			}
			
			//get transfer history sorted by date
			//transfers use a different table because the # of columns is different
			sql = "SELECT date, to_acct_id, from_acct_id, amount FROM transfers WHERE to_acct_id = ? OR from_acct_id = ? ORDER BY date DESC LIMIT 32";
			
			prepared = connection.prepareStatement(sql);
			prepared.setInt(1, acctNumber);
			prepared.setInt(2, acctNumber);

			result = prepared.executeQuery();
			while(result.next()) {
				
				Transaction trans = new Transaction();
				trans.Init(
						new BigDecimal( result.getDouble("amount") ),
						result.getTimestamp("date"),
						result.getInt("from_acct_id"),
						result.getInt("to_acct_id"),
						Transaction.TYPE_TRANSFER);

				transactions.add(trans);
			}
			
			connection.commit();
			
			//sort transactions and transfers date
			Comparator<Transaction> comparator = Collections.reverseOrder(new Comparator<Transaction>() {
				  public int compare(Transaction t1, Transaction t2) {
					    return t1.getTimestamp().compareTo(t2.getTimestamp());
					  }
				  });
			
			Collections.sort(transactions, comparator);

			//return recent activity
			return transactions;

		} catch (SQLException e) {
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
	
	//returns the new balance
	@Override
	public BigDecimal withdrawal(BigDecimal withdrawal, String username, int acctNumber) throws NetworkException, SQLException, ExceedsBalanceException, InvalidAcctException, InvalidUserException {

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
						
			//get user id first
			String sql = "SELECT id FROM users WHERE username = ?;";
			
			PreparedStatement prepared = connection.prepareStatement(sql);
			prepared.setString(1, username);
			
			ResultSet result = prepared.executeQuery();
			if(result.next()) {
				
				UUID userID = java.util.UUID.fromString( result.getString("id") );
				
				//insert into transactions table
				sql = "INSERT INTO transactions (user_id, acct_id, type, amount) VALUES (?, ?, 2, ?);";
				
				prepared = connection.prepareStatement(sql);
				prepared.setObject(1, userID);
				prepared.setInt(2, acctNumber);
				prepared.setObject(3, withdrawal);
				
				prepared.execute();
				
				connection.commit();
				
				return newBalance;
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
	
	private BigDecimal withdrawalA(Connection connection, BigDecimal withdrawal, int acctNumber) throws NetworkException, SQLException, ExceedsBalanceException, InvalidAcctException {
		
		//get balance
		String sql = "SELECT balance FROM accts WHERE id = ?;";

		PreparedStatement prepared = connection.prepareStatement(sql);
		prepared.setInt(1, acctNumber);
		
		ResultSet result = prepared.executeQuery();
		if(result.next()) {
			
			BigDecimal balance = result.getBigDecimal("balance");
			
			//if withdrawal is > balance
			if(balance.doubleValue() - withdrawal.doubleValue() < 0) {
				if(connection != null) {
					connection.rollback();
				}
				throw new ExceedsBalanceException(); //do not allow overdrafts
			}
			
			//update acct balance
			sql = "UPDATE accts SET balance = balance - ? WHERE id = ? RETURNING balance;";

			prepared = connection.prepareStatement(sql);

			prepared.setObject(1, withdrawal);
			prepared.setInt(2, acctNumber);
			result = prepared.executeQuery();

			if(result.next()) {
				
				BigDecimal newBalance = result.getBigDecimal("balance");
								
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
	
	//account transfer
	@Override
	public void transfer(BigDecimal amount, String username, int fromAcctNumber, int toAcctNumber) throws NetworkException, SQLException, ExceedsBalanceException, InvalidAcctException, InvalidUserException {
		
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
			
			//get user id first
			String sql = "SELECT id FROM users WHERE username = ?;";
			
			PreparedStatement prepared = connection.prepareStatement(sql);
			prepared.setString(1, username);
			
			ResultSet result = prepared.executeQuery();
			if(result.next()) {
				
				UUID userID = java.util.UUID.fromString( result.getString("id") );
				
				//insert into transfers table
				sql = "INSERT INTO transfers (user_id, to_acct_id, from_acct_id, amount) VALUES (?, ?, ?, ?);";
				
				prepared = connection.prepareStatement(sql);
				prepared.setObject(1, userID);
				prepared.setInt(2, toAcctNumber);
				prepared.setInt(3, fromAcctNumber);
				prepared.setObject(4, amount);
				
				prepared.execute();
				
				connection.commit();
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
	
	//assumes user added to account is a secondary account holder because primary is added by createAcct
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
				
				//create new account holder
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
	
	//remove the user from the account
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

			//get user ID
			String sql = "SELECT id FROM users WHERE username = ?;";

			PreparedStatement prepared = connection.prepareStatement(sql);
			prepared.setString(1, username);
			
			ResultSet result = prepared.executeQuery();
			if(result.next()) {
				
				UUID uuid = java.util.UUID.fromString( result.getString("id") );
				
				//delete account holder
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
