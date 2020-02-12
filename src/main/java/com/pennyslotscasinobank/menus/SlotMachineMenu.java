package com.pennyslotscasinobank.menus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Random;

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotcasinobank.exceptions.ExceedsBalanceException;
import com.pennyslotcasinobank.exceptions.InvalidAcctException;
import com.pennyslotcasinobank.exceptions.InvalidUserException;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Account;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.User;
import com.pennyslotscasinobank.View;

public class SlotMachineMenu implements View {

	@Override
	public void showMenu() {

		CasinoAcctDAO acctDAO = new CasinoAcctDAO();
		Account acct = Data.getData().getSelectedAcct();
		BigDecimal balance = acct.getBalance();
		BigDecimal bet = new BigDecimal(.01);
		User user = Data.getData().getUser();
		
		System.out.println("\nSlot Machine");

		do {
			System.out.format("Acct balance: %.2f\n", balance);
			System.out.format("Bet amount: %.2f\n", bet);
			
			System.out.println("1) Spin wheel");
			System.out.println("2) Change bet");
			System.out.println("0) Return to previous menu");
			
			int option = InputUtil.readInt(0, 2);
			if(option == 0) {
				
				//if balance changed (user either spent money or won the slot machine)
				if(balance != acct.getBalance()) {
					BigDecimal diff = balance.subtract(acct.getBalance());
					
					if(diff.doubleValue() < 0) {
						try {
							BigDecimal newBalance = acctDAO.withdrawal(diff, user.getUsername(), acct.getAcctNumber());
							
							acct.setBalance(newBalance);
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
						catch (NetworkException e) {
							MenuUtil.printNetworkError();
						}
						catch(SQLException e) {
							MenuUtil.printSQLError();
						}
					}
					else {
						try {
							BigDecimal newBalance = acctDAO.deposit(diff, user.getUsername(), acct.getAcctNumber());
							
							acct.setBalance(newBalance);
						}
						catch (InvalidAcctException e) {
							MenuUtil.printInvalidAcctError();
						}
						catch (InvalidUserException e) {
							MenuUtil.printInvalidUserError();
						}
						catch (NetworkException e) {
							MenuUtil.printNetworkError();
						}						
						catch(SQLException e) {
							MenuUtil.printSQLError();
						}
					}
				}
				return;
			}
			//change bet
			else if(option == 2) {
				System.out.println("Enter new bet");
				
				BigDecimal newBet = new BigDecimal(InputUtil.readPositiveDouble());
				if(newBet.doubleValue() == 0) {
					continue;
				}
				if(newBet.doubleValue() < .01) {
					System.out.println("Min bet is .01");
					continue;
				}
				
				bet = newBet;
				continue;
			}
			
			if(balance.doubleValue() - bet.doubleValue() < 0) {

				System.out.println("Out of money");
				continue;
				
			}
			
			//get slot values
			Random rand = new Random();
			int[] slotValue = { 
					rand.nextInt(6) + 1, //1-6
					rand.nextInt(6) + 1,
					rand.nextInt(6) + 1
			};
			
			//display slot values
			for(int i = 0; i < 3; i++) {
				System.out.print(slotValue[i] + " ");
				
				//wait 1 second
				try {
					Thread.sleep(375);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//if slots match
			boolean won = false;
			if(slotValue[0] == slotValue[1] && slotValue[0] == slotValue[2]) {
				
				won = true;
				
				//jackpot
				BigDecimal winnings = bet.multiply(new BigDecimal(100.0));
				System.out.println("\nJACK POT!");
				System.out.format("Winnings: bet * 100 = $%.2f\n", winnings);
				
				//make deposit
				//do this later to avoid multiple server calls
				//acctDAO.deposit(acct, winnings);
				balance = balance.add(winnings);
			}
			//if 2 of a kind
			else if(slotValue[0] == slotValue[1] || 
					slotValue[0] == slotValue[2] ||
					slotValue[1] == slotValue[2]) {
				
				won = true;
				
				BigDecimal winnings = bet.multiply(new BigDecimal(10.0));
				System.out.println("\n2 OF A KIND!");
				System.out.format("Winnings: bet * 10 = $%.2f\n", winnings);

				//make deposit
				//acctDAO.deposit(acct, winnings);
				balance = balance.add(winnings);
			}
			else {
				balance = balance.subtract(bet); //subtract bet amount only if lost
			}
			
			//if won, show ascii art
			if(won) {
				System.out.println(" _ __ ___   ___  _ __   ___ _   _ ");
				System.out.println("| '_ ` _ \\ / _ \\| '_ \\ / _ \\ | | |");
				System.out.println("| | | | | | (_) | | | |  __/ |_| |");
				System.out.println("|_| |_| |_|\\___/|_| |_|\\___|\\__, |");
				System.out.println("                             __/ /");
				System.out.println("                            |___/"); 
			}
			
			System.out.println();
		}
		while(true);
	}

	@Override
	public View selectOption() {

		return new AcctMenu(true);
	}

}
