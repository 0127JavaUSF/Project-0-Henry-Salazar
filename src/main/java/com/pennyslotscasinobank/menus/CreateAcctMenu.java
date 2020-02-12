package com.pennyslotscasinobank.menus;

import java.sql.SQLException;

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.User;
import com.pennyslotscasinobank.View;

public class CreateAcctMenu implements View {

	@Override
	public void showMenu() {

		Account acct = null;
		Data data = Data.getData();

		System.out.println("\nCreate Account");

		//get account type
		System.out.println("Select type");
		System.out.println("1) Checking");
		System.out.println("2) Savings");

		Account.Type type = Account.Type.CHECKING;
		int option = InputUtil.readInt(0, 2);
		switch (option) {
		case 1:
			type = Account.Type.CHECKING;
			break;
		case 2:
			type = Account.Type.SAVINGS;
			break;
		case 0:
			return;
		}
		
		//create account
		CasinoAcctDAO acctDAO = new CasinoAcctDAO();
		try {
			acct = acctDAO.createAcct(type, data.getUser().getUsername());
			
			data.getAccts().add(acct);
			
			data.getAcctHolders().add(data.getUser());

			System.out.println("Account creation successful");
		}
		catch(InvalidUserException e) {
			MenuUtil.printInvalidUserError();
			return;
		}
		catch(NetworkException e) {	
			MenuUtil.printNetworkError();
			return;
		}
		catch(SQLException e) {
			MenuUtil.printSQLError();
			return;
		}

		//show primary account holder
		User primaryUser = data.getUser();
		System.out.format("The primary account holder is %s %s\n", primaryUser.getFirstName(), primaryUser.getLastName());

		//get any secondary account holders
		do {
			System.out.println("Add a secondary account holder?");

			System.out.println("1) Yes");
			System.out.println("0) No");

			int addUser = InputUtil.readInt(0, 1);
			//if "No", break
			if (addUser == 0) {
				break;
			}
			
			//add the secondary user to the account
			MenuUtil.addSecondaryUser(acct);
			
			//loop so we can add multiple users
		} while (true);
		
		//show account summary
		if(acct != null)
			MenuUtil.showAcctSummary(acct);
	}

	@Override
	public View selectOption() {
		
		return new MainMenu();
	}

}
