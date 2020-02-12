package com.pennyslotscasinobank.menus;

import java.sql.SQLException;

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.User;
import com.pennyslotscasinobank.View;

public class EditAcctMenu implements View {

	@Override
	public void showMenu() {

		Data data = Data.getData();
		
		Account acct = data.getSelectedAcct();
		CasinoAcctDAO acctDAO = new CasinoAcctDAO();

		System.out.println("\nEdit Account");
		
		//can only add / remove secondary account holders
		//otherwise close account and create a new one
		
		do {
			//if secondary user exists
			boolean hasSecondary = data.getAcctHolders().size() > 1 ? true : false;

			System.out.println("1) Add secondary user");
			//if secondary user, show option
			if(hasSecondary) {
				System.out.println("2) Remove secondary user");
			}
			System.out.println("0) Return to Account Menu");
	
			int option = InputUtil.readInt(0, hasSecondary ? 2 : 1);
			switch(option) {
			case 1:
				//add secondary user
				MenuUtil.addSecondaryUser(acct);
				break;
			case 2:
				//note: can not remove primary user
				User selectedUser = MenuUtil.selectUser(data.getAcctHolders(), false);

				//if user selected, remove
				if(selectedUser != null) {
					
					try {
						acctDAO.removeUser(selectedUser.getUsername(), acct.getAcctNumber());
						
						data.getAcctHolders().remove(selectedUser);

						System.out.println("User removed");
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
					catch (InvalidUserException e) {
						MenuUtil.printInvalidUserError();
					}
				}		
				break;
			case 0:
				return;
			}
		}
		while(true);
	}

	@Override
	public View selectOption() {
		
		return new AcctMenu(true);
	}

}
