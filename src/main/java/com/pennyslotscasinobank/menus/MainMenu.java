package com.pennyslotscasinobank.menus;

import com.pennyslotcasinobank.daos.CasinoUserDAO;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.User;
import com.pennyslotscasinobank.View;

public class MainMenu implements View {

	@Override
	public void showMenu() {
		
		User user = Data.getData().getUser();
		
		System.out.println("\nMain Menu");
		System.out.format("User is %s %s\n", user.getFirstName(), user.getLastName());
		System.out.println("1) Select existing account");
		System.out.println("2) Create a new account");
		System.out.println("0) Log out and return to Start Menu");
	}

	@Override
	public View selectOption() {

		//process selected option
		int option = InputUtil.readInt(0, 2);
		switch (option) {
		case 1:
			return new SelectAcctMenu();
		case 2:
			return new CreateAcctMenu();
		case 0:
			CasinoUserDAO userDAO = new CasinoUserDAO();

			//clear client data
			Data.getData().clear();

			return new StartMenu();
		}
		return null;
	}

}
