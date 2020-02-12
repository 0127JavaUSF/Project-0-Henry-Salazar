package com.pennyslotscasinobank;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {

	static public final int TYPE_DEPOSIT = 1, TYPE_WITHDRAWAL = 2, TYPE_TRANSFER = 3;
	
	private BigDecimal amount;
	private Timestamp timestamp;
	private int transferFromAcctNumber = 0;
	private int transferToAcctNumber = 0;
	private int type;
	
	public void Init(BigDecimal amount, Timestamp timestamp, int transferFromAcctNumber, int transferToAcctNumber, int type) {
		
		this.amount = amount;
		this.timestamp = timestamp;
		this.transferFromAcctNumber = transferFromAcctNumber;
		this.transferToAcctNumber = transferToAcctNumber;
		this.type = type;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public Timestamp getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}
	
	public int getTransferFromAcctNumber() {
		return transferFromAcctNumber;
	}

	public void setTransferFromAcctNumber(int transferFromAcctNumber) {
		this.transferFromAcctNumber = transferFromAcctNumber;
	}

	public int getTransferToAcctNumber() {
		return transferToAcctNumber;
	}
	public void setTransferToAcctNumber(int transferToAcctNumber) {
		this.transferToAcctNumber = transferToAcctNumber;
	}
	public int getType() {
		return type;
	}
	static public String getTypeString(int type) {
		switch(type) {
		case TYPE_DEPOSIT: return "Deposit"; 
		case TYPE_WITHDRAWAL: return "Withdrawal"; 
		case TYPE_TRANSFER: return "Transfer"; 
		}
		
		return "Unknown";
	}
	public void setType(int type) {
		this.type = type;
	}
}
