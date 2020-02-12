package com.pennyslotscasinobank.menus;

import java.math.BigDecimal;
import java.sql.SQLException;

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotcasinobank.exceptions.ExceedsBalanceException;
import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.User;
import com.pennyslotscasinobank.View;

public class WithdrawalMenu implements View {

	@Override
	public void showMenu() {

		System.out.println("\nWithdrawal Menu");
		
		BigDecimal withdrawal = MenuUtil.promptForAmountInPennies();
				
		if(withdrawal == null) {
			
			System.out.println("Enter withdrawal amount");
			
			withdrawal = new BigDecimal(InputUtil.readPositiveDouble());
		}
		if(withdrawal.doubleValue() == 0) { //if 0, go back to previous menu
			return;
		}
					
		//use service to make withdrawal
		CasinoAcctDAO acctDAO = new CasinoAcctDAO();
		try {
			Account acct = Data.getData().getSelectedAcct();
			User user = Data.getData().getUser();
			
			BigDecimal newBalance = acctDAO.withdrawal(withdrawal, user.getUsername(), acct.getAcctNumber());
			
			acct.setBalance(newBalance);
			
			System.out.println("Success");
		}
		catch (ExceedsBalanceException e) {
			MenuUtil.printExceedsBalanceError();
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
