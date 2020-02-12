package com.pennyslotscasinobank.menus;

import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.View;

public class AcctMenu implements View {

	private boolean showAcctSummary = false;
	
	public AcctMenu(boolean showAcctSummary) {
		
		this.showAcctSummary = showAcctSummary;
	}
	
	@Override
	public void showMenu() {
		
		//show account summary
		if(showAcctSummary) {
			MenuUtil.showAcctSummary(Data.getData().getSelectedAcct());
		}

		System.out.println("\nAccount Menu");
		System.out.println("1) Make a deposit");
		System.out.println("2) Make a withdrawal");
		System.out.println("3) Transfer money");
		System.out.println("4) Recent Activity");
		System.out.println("5) Edit account");
		System.out.println("6) Close account");
		System.out.println("7) Play slot machine");
		System.out.println("0) Return to Main Menu");
	}

	@Override
	public View selectOption() {

		//process menu option
		int option = InputUtil.readInt(0, 7);
		switch(option) {
		case 1:
			return new DepositMenu();
		case 2:
			return new WithdrawalMenu();
		case 3:
			return new TransferMenu();
		case 4:
			return new RecentActivityMenu();
		case 5:
			return new EditAcctMenu();
		case 6:
			return new CloseAcctMenu();
		case 7:
			return new SlotMachineMenu();
		case 0:
			return new MainMenu();
		}
		
		return null;
	}

}
