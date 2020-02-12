package com.pennyslotscasinobank;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Account {

	public enum Type{ CHECKING, SAVINGS; }
	
	private Type type = Type.CHECKING;
	private BigDecimal balance = new BigDecimal(0);
	private int acctNumber = 0;
	
	public Account() {
		super();
	}
	
	public Account(Type type, BigDecimal balance, int acctNumber) {
		
		Init(type, balance, acctNumber);
	}
	
	public void Init(Type type, BigDecimal balance, int acctNumber) {
		
		this.type = type;
		this.balance = balance;
		this.acctNumber = acctNumber;
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
	public void addToBalance(BigDecimal amount) {
		this.balance = this.balance.add(amount);
	}
	
	public void subtractBalance(BigDecimal amount) {
		this.balance = this.balance.subtract(amount);
	}

	public BigDecimal getBalance() {

		//always round
		return balance.setScale(2, RoundingMode.HALF_EVEN);
	}
	public void setBalance(BigDecimal balance) {
		this.balance = balance.setScale(2, RoundingMode.HALF_EVEN);
	}
	public int getAcctNumber() {
		return acctNumber;
	}
	public void setAcctNumber(int acctNumber) {
		this.acctNumber = acctNumber;
	}
	
	//converts account type from SQL account type to Java Account.Type
	public static Type fromSQLType(int type) {
		
		switch(type) {
		case 1: return Type.CHECKING;
		case 2: return Type.SAVINGS;
		}
		
		return Type.CHECKING;
	}
	
	//converts account type to SQL account type
	public static int toSQLType(Type type) {
		
		switch(type) {
		case CHECKING: return 1;
		case SAVINGS: return 2;
		}
		
		return 1;
	}
}
