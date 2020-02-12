package com.pennyslotcasinobank.services;

public class CasinoUserService implements UserService {

	//client validation.
	public boolean validateUsername(String username) {
		
		//if < 4 characters or starts with a digit
		if(username.length() < 4 || Character.isDigit(username.charAt(0))) {
			return false;
		}
		
		return true;
	}
	
	//client validation.
	public boolean validatePassword(String password) {
		
		//if < 4 characters or starts with a digit
		if(password.length() < 4 || Character.isDigit(password.charAt(0))) {
			return false;
		}
		
		return true;
	}
}
