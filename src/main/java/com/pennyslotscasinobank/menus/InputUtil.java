package com.pennyslotscasinobank.menus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputUtil {

	//read from the console and return a string with no validation
	public static String readLine() {
		if (System.console() != null) {
			return System.console().readLine();
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try {
			return reader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	//read from the console and return an integer in the specified range
	public static int readInt(int startRange, int endRange) {
		
		//error correction
		if(startRange > endRange) {
			int temp = startRange;
			startRange = endRange;
			endRange = temp;
		}
		
		do {
			try {
				//try to parse integer from string input
				int input = Integer.parseInt(readLine());
				
				//if in range, return
				if(input >= startRange && input <= endRange) {
					return input;
				}
			}
			catch(Exception e) {
			}

			//user did not enter an integer
			System.out.format("Enter a number between %d and %d\n", startRange, endRange);
		}
		//prompt for input again
		while(true);
	}
	
	//read from the console and return a positive double
	public static double readPositiveDouble() {
		
		do {
			try {
				//try to parse double from string input
				double input = Double.parseDouble(readLine());
				
				//if in range, return
				if(input >= 0) {
					return input;
				}
			}
			catch(Exception e) {
			}

			//user did not enter a positive double
			System.out.println("Enter a positive decimal value");
		}
		//prompt for input again
		while(true);
	}

	//read from the console and return a positive long value
	public static long readPositiveLong() {
				
		do {
			try {
				//try to parse integer from string input
				long input = Long.parseLong(readLine());
				
				//if in range, return
				if(input >= 0) {
					return input;
				}
			}
			catch(Exception e) {
			}

			//user did not enter a positive integer
			System.out.println("Enter a positive whole number");
		}
		//prompt for input again
		while(true);
	}
		
	//read from the console and return a string
	public static String readString() {
		
		do {
			String input = readLine();
			
			//if empty string
			if(input.isEmpty()) {
				System.out.println("Enter text");
				continue;
			}
			
			//if first character is a digit
			if(Character.isDigit(input.charAt(0))) {
				System.out.println("Can not start with a number");
				continue;
			}
			
			return input;
			
		} while(true);
	}
}
