package com.pennyslotscasinobank.menus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotcasinobank.daos.CasinoUserDAO;
import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.User;

public class MenuUtil {

	//add user to account
	public static User addSecondaryUser(Account acct) {
		
		//prompt for user
		User secondaryUser = searchForUser();
		if(secondaryUser != null) //if user found
		{			
			//add secondary user to account
			CasinoAcctDAO acctDAO = new CasinoAcctDAO();
			
			try {
				acctDAO.addUser(secondaryUser.getUsername(), acct.getAcctNumber());
				
				Data.getData().getAcctHolders().add(secondaryUser);
				
				System.out.println("Success");
				System.out.format("%s %s added to account\n", secondaryUser.getFirstName(), secondaryUser.getLastName());
				return secondaryUser;
			}
			catch (InvalidAcctException e) {
				printInvalidAcctError();
				return null;
			}
			catch(NetworkException e) {			
				printNetworkError();
				return null;
			}
			catch(SQLException e) {
				printSQLError();
				return null;
			}
			catch (InvalidUserException e) {
				printInvalidUserError();
				return null;
			}
		}
		return null;
	}
	
	public static void printExceedsBalanceError() {
		System.out.println("Exceeds balance error");
	}
	
	public static void printInvalidAcctError() {
		System.out.println("Invalid account error");
	}
	
	public static void printInvalidUserError() {
		System.out.println("Invalid user error");
	}
	
	public static void printNetworkError() {
		System.out.println("Network error");
	}
	
	public static void printSQLError() {
		System.out.println("SQL error");
	}
	
	//will return null if user does not want to enter an amount in pennies
	public static BigDecimal promptForAmountInPennies() {
		
		System.out.println("Enter amount in pennies?");
		System.out.println("1) Yes");
		System.out.println("0) No");
		
		int option = InputUtil.readInt(0, 1);
		
		//if no, return null
		if(option == 0) {
			return null;
		}
		
		System.out.println("Enter amount:");
		
		//get pennies
		long pennies = InputUtil.readPositiveLong();
		
		//convert to dollars
		BigDecimal bigDec = new BigDecimal(pennies);
		bigDec = bigDec.divide(new BigDecimal(100.0));
		
		return bigDec;
	}
	
	//search for existing user
	public static User searchForUser() {
		
		CasinoUserDAO userDAO = new CasinoUserDAO();
		User secondaryUser = null;
		boolean isValid = false;
		do {
			System.out.println("1) search by username");
			System.out.println("2) search by account number");
			System.out.println("0) cancel");
			
			int option = InputUtil.readInt(0, 2);
			switch(option) {

			case 1: //search for user by username
				System.out.println("Enter username");
				
				String username = InputUtil.readString();
				try {
					secondaryUser = userDAO.getUserFromUsername(username);
				}
				catch(NetworkException e) {	
					printNetworkError();
					continue;
				}
				catch(SQLException e) {				
					printSQLError();
					continue;
				}
				catch(InvalidUserException e) {				
					System.out.println("User does not exist");
					continue;
				}
				if (secondaryUser != null) {
					isValid = true;
					break;
				}
				break;
				
			case 2: //search for user by account number
				System.out.println("Enter account number");
				
				int acctNum = (int)InputUtil.readPositiveLong();

				List<User> possibleUsers = null;
				try {
					possibleUsers = userDAO.getUsersFromAcctNumber(acctNum);
				}
				catch(NetworkException e) {				
					printNetworkError();
					continue;
				}
				catch(SQLException e) {				
					printSQLError();
					continue;
				}
				catch (InvalidAcctException e) {
					System.out.println("Invalid account number");
					continue;
				}
				if (possibleUsers != null && possibleUsers.size() > 0) {

					secondaryUser = selectUser(possibleUsers, true);
					isValid = true;
				}
				break;
				
			case 0: //cancel
				return null;
			}
			
			//user was not found
			if(!isValid) {
				System.out.println("User is invalid");
			}
			
			//loop and let user do another search
		} while(!isValid);
		
		return secondaryUser;
	}
	
	//select user from a list
	//if canSelectPrimaryUser == false, the first user in the list is not shown in the menu
	public static User selectUser(List<User> users, boolean canSelectPrimaryUser) {
		
		System.out.println("Select user");
		
		//show all users in format: option) firstName lastName ID username
		System.out.format("%6s %16s %16s %16s\n", "Option", "Last Name", "First Name", "Username");				

		boolean isFirst = true;
		int i = 1;
		for(User u : users) {
						
			if((isFirst && canSelectPrimaryUser) || !isFirst) {
				System.out.format("%6d) %16s %16s %16s\n", i, u.getFirstName(), u.getLastName(), u.getUsername());				
				i++;
			}

			isFirst = false;
		}
		
		//add go back option
		System.out.format("%6d) Go back\n", 0);
		
		do {
			int opt = InputUtil.readInt(0, i);
			if(opt == 0) { //if go back option
				return null;
			}
			
			//if canSelectPrimaryUser, array index is different
			int index = canSelectPrimaryUser ? opt - 1 : opt;
			
			//return selected user
			return users.get(index);
			
		} while(true);
	}

	//show account summary
	public static void showAcctSummary(Account acct) {
		
		Data data = Data.getData();
		
		System.out.println("\nAccount Summary");
		
		//show account type and account number
		System.out.format("%s #%d\n", acct.getType().toString(), acct.getAcctNumber());

		//show balance
		System.out.format("balance: %.2f\n", acct.getBalance());

		//if selected acct
		if(acct == data.getSelectedAcct()) {
			
			//show users
			List<User> users = data.getAcctHolders();
			if(users.size() == 0) { //if not set
				
				//get users
				CasinoUserDAO acctDAO = new CasinoUserDAO();
				try {
					users = acctDAO.getUsersFromAcctNumber(acct.getAcctNumber());
					
					data.setAcctHolders(users);
				} 
				catch (NetworkException e) {
					MenuUtil.printNetworkError();
				} 
				catch (SQLException e) {
					MenuUtil.printSQLError();
				} 
				catch (InvalidAcctException e) {
					MenuUtil.printInvalidAcctError();
				}	
			}
			
			int i = 0;
			for(User u : users) {
				
				//if primary user
				if(i == 0) {
					System.out.format("Primary %s %s\n", u.getFirstName(), u.getLastName());
				}
				else {
					//secondary user
					System.out.format("Secondary %s %s\n", u.getFirstName(), u.getLastName());
				}
				
				i++;
			}
		}
	}
}
