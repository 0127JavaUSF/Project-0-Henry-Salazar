package com.pennyslotscasinobank.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import com.pennyslotcasinobank.daos.CasinoAcctDAO;
import com.pennyslotscasinobank.Account;

@RunWith(MockitoJUnitRunner.class)
public class CasinoAcctDAOMockTest {

	static Account acct1 = new Account(Account.Type.CHECKING, new BigDecimal(199.99), 123456);
	static Account acct2 = new Account(Account.Type.SAVINGS, new BigDecimal(49.49), 987654);
	
	static List<Account> accts = new LinkedList<Account>();
	
	//mock object should probably be a supporting class, not the main class tested
	//this is simply to show use of Mockito
	
	@Mock
	CasinoAcctDAO acctDAO;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		accts.add(acct1);
		accts.add(acct2);
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
	public void createAcctTest() {
		
		try {
			when(acctDAO.createAcct(Account.Type.CHECKING, "sman")).thenReturn(acct1);
				
			assertEquals("createAcct should return acct1", acct1, acctDAO.createAcct(Account.Type.CHECKING, "sman"));
		}
		catch(Exception e) {
			
		}
	}

	@Test
	public void getFromAcctNumTest() {
		
		try {
			when(acctDAO.getFromAcctNum(acct2.getAcctNumber())).thenReturn(acct2);

			assertEquals("getFromAcctNum should return acct2", acct2, acctDAO.getFromAcctNum(acct2.getAcctNumber()));
		}
		catch(Exception e) {
			
		}
	}
	
	@Test
	public void depositTest() {
		
		try {
			when(acctDAO.deposit(new BigDecimal(100.0), acct1.getAcctNumber())).thenReturn(new BigDecimal(200.0)); //returns new balance

			assertEquals("deposit should return 200.0", new BigDecimal(200.0), acctDAO.deposit(new BigDecimal(100.0), acct1.getAcctNumber()));
		}
		catch(Exception e) {
			
		}
	}
	
	@Test
	public void getAcctsTest() {
		
		try {
			when(acctDAO.getAccts("mrpac")).thenReturn(accts);

			assertEquals("getAccts should return accts", accts, acctDAO.getAccts("mrpac"));
		}
		catch(Exception e) {
			
		}
	}
	
	@Test
	public void withdrawalTest() {
		
		try {
			when(acctDAO.withdrawal(new BigDecimal(200.0), acct2.getAcctNumber())).thenReturn(new BigDecimal(50.0));
				
			assertEquals("withdrawal should return 50.0", new BigDecimal(50.0), acctDAO.withdrawal(new BigDecimal(200.0), acct2.getAcctNumber()));
		}
		catch(Exception e) {
			
		}
	}
}
