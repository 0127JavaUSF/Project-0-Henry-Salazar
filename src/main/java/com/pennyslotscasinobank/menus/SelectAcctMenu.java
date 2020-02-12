package com.pennyslotscasinobank.menus;

import java.sql.SQLException;
import java.util.List;

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.User;
import com.pennyslotscasinobank.View;

public class SelectAcctMenu implements View {
	
	View nextMenu = null;

	@Override
	public void showMenu() {

		Data data = Data.getData();	
		User user = data.getUser();
		
		System.out.println("\nSelect Account");
		//show the logged in user
		System.out.format("User is %s %s\n", user.getFirstName(), user.getLastName());
		
		//table format
		System.out.format("%6s%12s%16s%12s\n", "Option", "Type", "Acct#", "Balance");
		
		CasinoAcctDAO acctDAO = new CasinoAcctDAO();
		List<Account> accts = null;
		boolean wasError = false;
		try {
			accts = acctDAO.getAccts(user.getUsername());
			
			data.setAccts(accts);
			data.setAcctHolders(null);
		} 
		catch (NetworkException e) {
			MenuUtil.printNetworkError();
			wasError = true;
		} 
		catch (SQLException e) {
			MenuUtil.printSQLError();
			wasError = true;
		} 
		catch (InvalidUserException e) {
			System.out.println("No accts");
			wasError = true;
		}
		
		if(wasError) {
			nextMenu = new MainMenu();
			return;
		}
				
		//show user in format: option) type acct# balance
		int i = 1;
		for (Account a : accts) {
			System.out.format("%6d)%12s%16d%12.2f\n", i, a.getType().toString(), a.getAcctNumber(), a.getBalance());
			i++;
		}

		System.out.format("%6d) Go back\n", 0);

		int option = InputUtil.readInt(0, accts.size());
		if(option == 0) {
			nextMenu = new MainMenu();
			return;
		}
		
		data.setSelectedAcct(accts.get(option - 1)); // - 1 because accts is 0 index based
		nextMenu = new AcctMenu(true);
	}

	@Override
	public View selectOption() {

		return nextMenu;
	}
}
