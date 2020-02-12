package com.pennyslotscasinobank;

import java.util.LinkedList;
import java.util.List;

public class Data {

	static private Data data = new Data(); //singleton reference
	
	//the selected account and user
	private Account selectedAcct;
	private User user;
	
	//the list of accounts associated with the selected user
	private List<Account> accts = new LinkedList<Account>();
	
	//the list of users associated with the selected account
	private List<User> acctHolders = new LinkedList<User>();

	//private constructor because it is singleton
	private Data() {
		super();
	}
	
	public static Data getData() {
		return data;
	}
	
	public void clear() {
		
		selectedAcct = null;
		user = null;
		
		accts = new LinkedList<Account>();
		acctHolders = new LinkedList<User>();
	}

	public Account getSelectedAcct() {
		return selectedAcct;
	}

	public void setSelectedAcct(Account acct) {
		this.selectedAcct = acct;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public List<Account> getAccts() {
		return accts;
	}

	public void setAccts(List<Account> accts) {
		
		//don't allow null. avoid NullPointerException
		if(accts == null)
			this.accts = new LinkedList<Account>();
		else
			this.accts = accts;
	}

	public List<User> getAcctHolders() {
		return acctHolders;
	}

	public void setAcctHolders(List<User> acctHolders) {
		
		//don't allow null. avoid NullPointerException
		if(acctHolders == null)
			this.acctHolders = new LinkedList<User>();
		else
			this.acctHolders = acctHolders;
	}
}
