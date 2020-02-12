package com.pennyslotscasinobank.menus;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotcasinobank.exceptions.ExceedsBalanceException;
import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.View;

public class TransferMenu implements View {
	
	private boolean showAcctSummary = false;

	@Override
	public void showMenu() {

		Data data = Data.getData();
		
		System.out.println("\nTransfer Menu");
		
		BigDecimal transferAmount = null;
		do {
			transferAmount = MenuUtil.promptForAmountInPennies();
			
			if(transferAmount == null) {
				//get transfer amount
				System.out.println("Enter amount");
				transferAmount = new BigDecimal(InputUtil.readPositiveDouble());				
			}
			
			if(transferAmount.doubleValue() == 0) {
				showAcctSummary = true;
				return;
			}
	
			if(transferAmount.doubleValue() > data.getSelectedAcct().getBalance().doubleValue()) {
				System.out.format("Amount exceeds account balance which is %.2f\n", data.getSelectedAcct().getBalance().doubleValue());
				continue;
			}
			
			break;
			
		} while(true);

		do {
			//get account number
			System.out.println("Enter account number");
			int acctNum = (int)InputUtil.readPositiveLong();
	
			if(acctNum == 0) {
				showAcctSummary = true;
				return;
			}
					
			//use service to get transfer account
			Account transferTo = null;
			CasinoAcctDAO acctDAO = new CasinoAcctDAO();
			try {
				transferTo = acctDAO.getFromAcctNum(acctNum);
				
				if(transferTo == null) {
					MenuUtil.printInvalidAcctError();
					continue;
				}
			}
			catch(NetworkException e) {
				MenuUtil.printNetworkError();
				continue;
			}
			catch(SQLException e) {
				MenuUtil.printSQLError();
				continue;
			}
			catch(InvalidAcctException e) {
				MenuUtil.printInvalidAcctError();
				continue;
			}
			
			//use service to make transfer
			try {
				Account selected = data.getSelectedAcct();
				acctDAO.transfer(transferAmount, selected.getAcctNumber(), transferTo.getAcctNumber());

				selected.setBalance(selected.getBalance().subtract(transferAmount));
				transferTo.setBalance(transferTo.getBalance().add(transferAmount));
				
				System.out.println("Success");
				
				//show account summary of both accounts
				MenuUtil.showAcctSummary(data.getSelectedAcct());
				MenuUtil.showAcctSummary(transferTo);
				return;
			}
			catch (ExceedsBalanceException e) {
				MenuUtil.printExceedsBalanceError();
			}
			catch (InvalidAcctException e) {
				MenuUtil.printInvalidAcctError();
			}
			catch(NetworkException e) {
				MenuUtil.printNetworkError();
			}
			catch(SQLException e) {
				MenuUtil.printSQLError();
			}
			return;
		}
		while(true);
	}

	@Override
	public View selectOption() {
		
		return new AcctMenu(showAcctSummary);
	}

}
