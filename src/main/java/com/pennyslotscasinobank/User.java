package com.pennyslotscasinobank;

public class User {
	
	private String firstName = null;
	private String lastName = null;
	private String username = null;
	//password should not be saved on client
	//neither should UUID
	
	public User() {
		super();
	}
	
	public User(String firstName, String lastName, String username) {
		super();
		
		Init(firstName, lastName, username);
	}
	
	public void Init(String firstName, String lastName, String username) {
		
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
