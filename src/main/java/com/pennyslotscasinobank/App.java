package com.pennyslotscasinobank;

import com.pennyslotscasinobank.menus.StartMenu;

public class App {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
			
		View view = new StartMenu();
		
		while(view != null) {
			view.showMenu();
			view = view.selectOption();
		}
		
		System.out.println("Good Bye!");
	}
}
