package com.pennyslotscasinobank.menus;

import java.sql.SQLException;

import com.pennyslotcasinobank.daos.CasinoUserDAO;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotcasinobank.exceptions.UsernameAlreadyExistsException;
import com.pennyslotcasinobank.services.CasinoUserService;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.User;
import com.pennyslotscasinobank.View;

public class RegisterMenu implements View {
	
	private View nextMenu = null;

	@Override
	public void showMenu() {
		
		CasinoUserDAO userDAO = new CasinoUserDAO();
		CasinoUserService userService = new CasinoUserService();
		
		System.out.println("\nRegister Menu");
		
		//set first name
		System.out.println("Enter first name");
		String firstName = InputUtil.readString();
		
		//set last name
		System.out.println("Enter last name");
		String lastName = InputUtil.readString();		

		//get password
		String password = null;
		do {
			System.out.println("Enter password: ");
			password = InputUtil.readString();

			//do client validation
			if(userService.validatePassword(password)) {
				break;
			}
			
			System.out.println("Error: password is too short or begins with a number");
			
		} while(true);
		
		//use a do while loop because if the username is taken, the app needs to prompt the user again
		do {

			//get username
			String username = null;
			do {
				System.out.println("Enter username: ");
				username = InputUtil.readString();

				//do client validation
				if(userService.validateUsername(username)) {
					break;
				}
				
				System.out.println("Error: username is too short or begins with a number");
			
			} while(true);

			//use registration service
			try {
				userDAO.register(firstName, lastName, username, password);
				
				//client data
				User user = new User(firstName, lastName, username);
				Data.getData().setUser(user);				
				
				System.out.println("User registered");
				
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
			catch(UsernameAlreadyExistsException e) {
				System.out.println("Username exists");
				
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
