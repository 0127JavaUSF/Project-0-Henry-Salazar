package com.pennyslotscasinobank.menus;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.User;
import com.pennyslotscasinobank.View;

public class DepositMenu implements View {

	@Override
	public void showMenu() {

		System.out.println("\nDeposit Menu");
		
		BigDecimal deposit = MenuUtil.promptForAmountInPennies();
		
		if(deposit == null) {
		
			System.out.println("Enter deposit amount");
			
			deposit = new BigDecimal(InputUtil.readPositiveDouble());
		}
		if(deposit.doubleValue() == 0) { //if 0, go back to previous menu
			return;
		}
			
		//use service to make deposit
		CasinoAcctDAO acctDAO = new CasinoAcctDAO();
		try {
			Account acct = Data.getData().getSelectedAcct();
			User user = Data.getData().getUser();
			
			BigDecimal newBalance = acctDAO.deposit(deposit, user.getUsername(), acct.getAcctNumber());
			
			acct.setBalance(newBalance);
					
			System.out.println("Success");
		}
		catch (InvalidAcctException e) {
			MenuUtil.printInvalidAcctError();
		}
		catch (InvalidUserException e) {
			MenuUtil.printInvalidUserError();
		}
		catch(NetworkException e) {
			MenuUtil.printNetworkError();
		}
		catch(SQLException e) {
			MenuUtil.printSQLError();
		}
	}

	@Override
	public View selectOption() {
		
		return new AcctMenu(true);
	}

}
