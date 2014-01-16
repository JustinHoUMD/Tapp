package com.example.tap_latest;

import java.util.Scanner;

public class TestScan {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String name = "Anish";
		String phoneNumber = "1234567";
		String email = "example@abc.com";
		String facebookId = "anish25";
		String 	finalData = "Name:" + name + "," + "Phone:" + phoneNumber + ","
				+ "Email:" + email + "," + "FacebookId:" + facebookId;
		Scanner s = new Scanner(finalData).useDelimiter("Name:(\\w+),Phone:(\\w+),Email:(\\w+),FacebookId:,(\\w+)");
		System.out.println(s.next());
	    System.out.println(s.next());
	    System.out.println(s.next());
	    System.out.println(s.next());

	}

}
