package com.pennyslotscasinobank.menus;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotcasinobank.exceptions.NetworkException;
import com.pennyslotscasinobank.Data;
import com.pennyslotscasinobank.Transaction;
import com.pennyslotscasinobank.View;

public class RecentActivityMenu implements View {

	@Override
	public void showMenu() {

		System.out.println("\nRecent Activity");
		
		//table format
		System.out.format("%24s%24s%24s%24s\n", "Date", "Type", "Amount", "Notes");
		
		CasinoAcctDAO acctDAO = new CasinoAcctDAO();
		try {
			List<Transaction> list = acctDAO.getRecentActivity(Data.getData().getSelectedAcct().getAcctNumber());
			
			for(Transaction t : list) {
				
				int acctNum = Data.getData().getSelectedAcct().getAcctNumber();
				
				//if withdrawal or transfer to different acct
				double amount = t.getAmount().doubleValue();
				if(t.getType() == Transaction.TYPE_WITHDRAWAL || 
					(t.getType() == Transaction.TYPE_TRANSFER && t.getTransferFromAcctNumber() == acctNum)) {
					amount *= -1.0; //make negative
				}
				
				//if transfer
				String notes = "";
				if(t.getType() == Transaction.TYPE_TRANSFER) {
					if(t.getTransferFromAcctNumber() == acctNum) {
						notes = "to acct #" + t.getTransferToAcctNumber(); //put transfer acct # in notes column
					}
					else {
						notes = "from acct #" + t.getTransferFromAcctNumber();						
					}
				}
	
				String timestampString = new SimpleDateFormat("MM/dd/yyyy HH:mm a").format(t.getTimestamp());
				
				System.out.format("%24s%24s%24.2f%24s\n", timestampString, Transaction.getTypeString(t.getType()), amount, notes);
			}
		}
		catch (NetworkException e) {
			MenuUtil.printNetworkError();
		} 
		catch (SQLException e) {
			MenuUtil.printSQLError();
		} 
		
		System.out.println("0) Go back");
		
		int option = InputUtil.readInt(0, 0);
	}

	@Override
	public View selectOption() {
		// TODO Auto-generated method stub
		return new AcctMenu(true);
	}

}
