package com.pennyslotscasinobank.menus;

import java.sql.SQLException;

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.View;

public class CloseAcctMenu implements View {
	
	View nextMenu = null;

	@Override
	public void showMenu() {

		System.out.println("\nConfirm close account?");
		System.out.println("1) Yes");
		System.out.println("0) No");
		
		int option = InputUtil.readInt(0, 1);
		switch(option) {
		case 1:
			//use service to close acct
			CasinoAcctDAO acctDAO = new CasinoAcctDAO();
			try {
				Data data = Data.getData();
				Account acct = data.getSelectedAcct();

				acctDAO.closeAcct(acct.getAcctNumber());
								
				//clear for client data
				if(data.getAccts().contains(acct)) {
					data.getAccts().remove(acct);
				}
				data.setSelectedAcct(null);
				data.setAcctHolders(null);
				
				System.out.println("Account closed");

			}
			catch (InvalidAcctException e) {
				MenuUtil.printInvalidAcctError();
			}
			catch (NetworkException e) {
				MenuUtil.printNetworkError();
			}
			catch(SQLException e) {
				MenuUtil.printSQLError();
			}
						
			//show main menu
			nextMenu = new MainMenu();
			return;
			
		case 0: //if "No", show main account menu
			nextMenu = new AcctMenu(true);
			return;
		}
	}

	@Override
	public View selectOption() {

		return nextMenu;
	}

}
