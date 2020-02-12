package com.pennyslotcasinobank.daos;

import java.sql.SQLException;
import java.util.List;

import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotcasinobank.exceptions.UsernameAlreadyExistsException;
import com.pennyslotcasinobank.exceptions.WrongLogInException;
import com.pennyslotscasinobank.User;

public interface UserDAO {

	public List<User> getUsersFromAcctNumber(int acctNumber) throws NetworkException, SQLException, InvalidAcctException;
	
	public User getUserFromUsername(String username) throws NetworkException, SQLException, InvalidUserException;
	
	public void logIn(User user, String username, String password) throws NetworkException, SQLException, WrongLogInException;
	
	public void register(String firstName, String lastName, String username, String password) throws NetworkException, SQLException, UsernameAlreadyExistsException;
}
