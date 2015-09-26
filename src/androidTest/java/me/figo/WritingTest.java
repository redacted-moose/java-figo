package me.figo;

import com.android.volley.Response;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;

import me.figo.internal.TaskStatusResponse;
import me.figo.internal.TokenResponse;
import me.figo.models.Account;
import me.figo.models.LoginSettings;
import me.figo.models.Service;
import me.figo.models.Transaction;
import me.figo.internal.TaskTokenResponse;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class WritingTest {

	private final String CLIENT_ID = "CaESKmC8MAhNpDe5rvmWnSkRE_7pkkVIIgMwclgzGcQY";
	private final String CLIENT_SECRET = "STdzfv0GXtEj_bwYn7AgCVszN1kKq5BdgEIKOM_fzybQ";
	private final String USER = "testuser@test.de";
	private final String PASSWORD = "some_words";
	
	// Bank account infos needed
	private final String ACCOUNT = "figo";
	private final String BANKCODE = "90090042";
	private final String PIN = "figo";
	
	private static String rand = null;
	
	
	private final FigoConnection fc = new FigoConnection(CLIENT_ID, CLIENT_SECRET, "https://127.0.0.1/");
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		SecureRandom random = new SecureRandom();
		rand = new BigInteger(130, random).toString(32); 
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}
	
	public void test_01_addUser(Response.Listener<T> listener, Response.ErrorListener errorListener) throws IOException, FigoError {
		String response = this.fc.addUser("Test", rand+USER, PASSWORD, "de", listener, errorListener);
		assertTrue(response.length() == 19);
	}

	public void test_02_credentialLogin(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException {
		TokenResponse accessToken = this.fc.credentialLogin(USER, PASSWORD, listener, errorListener);
		assertTrue(accessToken.access_token instanceof String);				
	}	
	
	public void test_03_getSupportedPaymentServices(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException	{
		TokenResponse accessToken = this.fc.credentialLogin(USER, PASSWORD, listener, errorListener);
		FigoSession fs = new FigoSession(accessToken.access_token);
		List<Service> response = fs.getSupportedServices("de", listener, errorListener);
		assertTrue(response.size() == 22);
	}
	
	public void test_04_getLoginSettings(Response.Listener<T> listener, Response.ErrorListener errorListener) throws IOException, FigoError {
		TokenResponse accessToken = this.fc.credentialLogin(USER, PASSWORD, listener, errorListener);
		FigoSession fs = new FigoSession(accessToken.access_token);
		LoginSettings response = fs.getLoginSettings("de", "47251550", listener, errorListener);
		assertTrue(response instanceof LoginSettings);		
	}
	
	public void test_05_addBankAccount(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException	{
		TokenResponse accessToken = this.fc.credentialLogin(USER, PASSWORD, listener, errorListener);
		FigoSession fs = new FigoSession(accessToken.access_token);
		TaskTokenResponse response= fs.setupNewAccount(BANKCODE, "de", ACCOUNT, PIN, Arrays.asList("standingOrders"), listener, errorListener);
		TaskStatusResponse taskStatus = fs.getTaskState(response, listener, errorListener);
		assertTrue(taskStatus instanceof TaskStatusResponse);
		try {
			Thread.sleep(25000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertTrue(fs.getAccounts(listener, errorListener).size() == 1);
	}
	
	public void test_06_modifyTransaction(Response.Listener<T> listener, Response.ErrorListener errorListener) throws IOException, FigoError {
		TokenResponse accessToken = this.fc.credentialLogin(USER, PASSWORD, listener, errorListener);
		FigoSession fs = new FigoSession(accessToken.access_token);
		Account testAccount = fs.getAccounts(listener, errorListener).get(0);
		Transaction testTransaction = fs.getTransactions(testAccount, listener, errorListener).get(0);
		String transactionId = testTransaction.getTransactionId();
		fs.modifyTransaction(testTransaction, FigoSession.FieldVisited.NOT_VISITED, listener, errorListener);
		testTransaction = fs.getTransaction(testAccount.getAccountId(), transactionId, listener, errorListener);
		assertFalse(testTransaction.isVisited());
		fs.modifyTransaction(testTransaction, FigoSession.FieldVisited.VISITED, listener, errorListener);
		testTransaction = fs.getTransaction(testAccount.getAccountId(), transactionId, listener, errorListener);
		assertTrue(testTransaction.isVisited());		
	}
	
	public void test_07_modifyAccountTransactions(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException	{
		TokenResponse accessToken = this.fc.credentialLogin(USER, PASSWORD, listener, errorListener);
		FigoSession fs = new FigoSession(accessToken.access_token);
		Account testAccount = fs.getAccounts(listener, errorListener).get(0);
		fs.modifyTransactions(testAccount, FigoSession.FieldVisited.NOT_VISITED, listener, errorListener);
		Transaction testTransaction = fs.getTransactions(testAccount, listener, errorListener).get(4);
		assertFalse(testTransaction.isVisited());
		fs.modifyTransactions(testAccount, FigoSession.FieldVisited.VISITED, listener, errorListener);
		testTransaction = fs.getTransactions(listener, errorListener).get(4);
		assertTrue(testTransaction.isVisited());
	}
	
	public void test_08_modifyUserTransaction(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException	{
		TokenResponse accessToken = this.fc.credentialLogin(USER, PASSWORD, listener, errorListener);
		FigoSession fs = new FigoSession(accessToken.access_token);
		fs.modifyTransactions(FigoSession.FieldVisited.NOT_VISITED, listener, errorListener);
		Transaction testTransaction = fs.getTransactions(listener, errorListener).get(3);
		assertFalse(testTransaction.isVisited());
		fs.modifyTransactions(FigoSession.FieldVisited.VISITED, listener, errorListener);
		testTransaction = fs.getTransactions(listener, errorListener).get(3);
		assertTrue(testTransaction.isVisited());
	}
	
	public void test_09_deleteTransaction(Response.Listener<T> listener, Response.ErrorListener errorListener) throws IOException, FigoError {
		TokenResponse accessToken = this.fc.credentialLogin(USER, PASSWORD, listener, errorListener);
		FigoSession fs = new FigoSession(accessToken.access_token);
		List<Transaction> transactions = fs.getTransactions(listener, errorListener);
		fs.removeTransaction(transactions.get(0), listener, errorListener);
		assertTrue(transactions.size() > fs.getTransactions(listener, errorListener).size());
		fs.removeUser(listener, errorListener);
	}
}
