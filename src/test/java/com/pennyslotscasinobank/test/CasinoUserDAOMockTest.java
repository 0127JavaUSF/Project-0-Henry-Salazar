package com.pennyslotscasinobank.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.pennyslotcasinobank.daos.CasinoUserDAO;
import com.pennyslotscasinobank.User;

@RunWith(MockitoJUnitRunner.class)
public class CasinoUserDAOMockTest {

	static User user1 = new User("Super", "Man", "sman");
	static User user2 = new User("Ms", "Pacman", "mspac");
	static User user3 = new User("Mr", "Pacman", "mrpac");
	static List<User> users = new LinkedList();
			
	@Mock
	static CasinoUserDAO registrar;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
				
		users.add(user1);
		users.add(user2);
		users.add(user3);
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
	public void getUsersFromAcctNumberTest() {
		
		try {	
			when(registrar.getUsersFromAcctNumber(123456)).thenReturn(users);
			
			assertEquals("getUsersFromAcctNumber should return users", users, registrar.getUsersFromAcctNumber(123456));
		}
		catch(Exception e) {
			
		}
	}

	@Test
	public void getUserFromUsernameTest() {
		
		try {	
			when(registrar.getUserFromUsername("sman")).thenReturn(user1);
			
			assertEquals("getUserFromUsername should return user1", user1, registrar.getUserFromUsername("sman"));
		}
		catch(Exception e) {
			
		}
	}
}
