package com.pennyslotscasinobank.menus;

import java.sql.SQLException;

import com.pennyslotcasinobank.daos.CasinoUserDAO;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotcasinobank.exceptions.WrongLogInException;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.User;
import com.pennyslotscasinobank.View;

public class LoginMenu implements View {

	private View nextMenu = null;
	
	@Override
	public void showMenu() {
		
		System.out.println("\nLog In Menu");

		do {
			//get username
			System.out.println("Enter username: ");
			String username = InputUtil.readString();

			//get password
			System.out.println("Enter password: ");
			String password = InputUtil.readString();

			User user = new User();

			//use service to log in
			CasinoUserDAO userDAO = new CasinoUserDAO();
			try {
				userDAO.logIn(user, username, password);
				
				Data.getData().clear();
				Data.getData().setUser(user);

				//if success, show main menu
				nextMenu = new MainMenu();
				return;
			}
			catch(NetworkException e) {
				
				MenuUtil.printNetworkError();
				
				nextMenu = new StartMenu();
				return;
			}
			catch(SQLException e) {				
				MenuUtil.printSQLError();
				
				nextMenu = new StartMenu();
				return;
			}
			catch(WrongLogInException e) {

				//incorrect username and or password. allow user to try again
				System.out.println("Incorrect username and/or password");
				System.out.println("1) Try again");
				System.out.println("0) Return to Start Menu");

				int option = InputUtil.readInt(0, 1);				
				switch (option) {
				case 1:
					break;
				case 0:
					nextMenu = new StartMenu();
					return;
				}
			}

		} while (true);

	}

	@Override
	public View selectOption() {
		
		return nextMenu;
	}

}
