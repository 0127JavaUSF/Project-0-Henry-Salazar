package com.pennyslotscasinobank.menus;

import com.pennyslotscasinobank.View;

public class StartMenu implements View {

	@Override
	public void showMenu() {

		System.out.println("Welcome to $Penny Slots Casino Bank$\n");
		
		System.out.println("|#######====================#######|");
		System.out.println("|#(1)*UNITED STATES OF AMERICA*(1)#|");
		System.out.println("|#****        =====     ****  ****#|");
		System.out.println("|#* {G}      | (\") |             *#|");
		System.out.println("|#*  ****    | /v\\ |    O N E    *#|");
		System.out.println("|#(1)         \\===/            (1)#|");
		System.out.println("|##=========ONE DOLLAR===========##|");
		System.out.println("------------------------------------");
		
		System.out.println("\nMain Menu");
		System.out.println("1) Log in");
		System.out.println("2) Register");
		System.out.println("0) Quit");
	}

	@Override
	public View selectOption() {
		int option = InputUtil.readInt(0, 2);
		switch (option) {
		case 1:
			//log in menu
			return new LoginMenu();
		case 2:
			//register menu
			return new RegisterMenu();
		case 0:
			//exit app
			return null;
		}
		return null;
	}
}
