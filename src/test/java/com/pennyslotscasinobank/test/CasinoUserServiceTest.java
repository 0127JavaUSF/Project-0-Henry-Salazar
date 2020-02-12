package com.pennyslotscasinobank.test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.pennyslotcasinobank.services.CasinoUserService;

public class CasinoUserServiceTest {
	
	CasinoUserService userService = new CasinoUserService();;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
				
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	@Before
	public void setUp() throws Exception {
	
	}

	@After
	public void tearDown() throws Exception {
	
	}
	
	@Test
	public void validatePasswordTest() {
		
		assertEquals("awesomePassword741 should return true", true, userService.validatePassword("awesomePassword741"));

		assertEquals("empty string should return false", false, userService.validatePassword(""));

		assertEquals("short password should return false", false, userService.validatePassword("Sm"));

		assertEquals("1password should return false", false, userService.validatePassword("1password"));
	}
	
	@Test
	public void validateUsernameTest() {
		
		assertEquals("user741 should return true", true, userService.validateUsername("user741"));

		assertEquals("empty string should return false", false, userService.validateUsername(""));

		assertEquals("short username should return false", false, userService.validateUsername("Sm"));

		assertEquals("1user should return false", false, userService.validateUsername("1user"));
	}
}
