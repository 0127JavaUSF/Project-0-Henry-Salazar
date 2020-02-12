package com.pennyslotcasinobank.daos;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.pennyslotcasinobank.exceptions.BalanceNot0Exception;
import com.pennyslotcasinobank.exceptions.ExceedsBalanceException;
import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.Transaction;

public interface AcctDAO {

	public Account createAcct(Account.Type type, String username) throws NetworkException, SQLException, InvalidUserException;
	public void closeAcct(int acctNumber) throws NetworkException, SQLException, BalanceNot0Exception, InvalidAcctException;
	
	public Account getFromAcctNum(int acctNumber) throws NetworkException, SQLException, InvalidAcctException;
	
	public BigDecimal deposit(BigDecimal deposit, String username, int acctNumber) throws NetworkException, SQLException, InvalidAcctException, InvalidUserException;
	
	public List<Account> getAccts(String username) throws NetworkException, SQLException, InvalidUserException;
	
	public List<Transaction> getRecentActivity(int acctNumber) throws NetworkException, SQLException;

	public BigDecimal withdrawal(BigDecimal withdrawal, String username, int acctNumber) throws NetworkException, SQLException, ExceedsBalanceException, InvalidAcctException, InvalidUserException;
	
	public void transfer(BigDecimal amount, String username, int fromAcctNumber, int toAcctNumber) throws NetworkException, SQLException, ExceedsBalanceException, InvalidAcctException, InvalidUserException;
	
	public void addUser(String username, int acctNumber) throws NetworkException, SQLException, InvalidAcctException, InvalidUserException;
	public void removeUser(String username, int acctNumber) throws NetworkException, SQLException, InvalidAcctException, InvalidUserException;
}
