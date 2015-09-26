//
// Copyright (c) 2013 figo GmbH
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
//

package me.figo;

import com.android.volley.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import me.figo.models.Account;
import me.figo.models.Notification;
import me.figo.models.Payment;
import me.figo.models.PaymentProposal;
import me.figo.models.PaymentType;
import me.figo.models.StandingOrder;
import me.figo.models.TanScheme;
import me.figo.models.Transaction;
import me.figo.models.User;

import org.junit.Before;
import org.junit.Test;

public class SessionTest {

    FigoSession sut = null;

    @Before
    public void setUp() throws Exception {
        sut = new FigoSession("ASHWLIkouP2O6_bgA2wWReRhletgWKHYjLqDaqb0LFfamim9RjexTo22ujRIP_cjLiRiSyQXyt2kM1eXU2XLFZQ0Hro15HikJQT_eNeT_9XQ");
    }

    @Test
    public void testGetAccount(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException {
        Account a = sut.getAccount("A1.2", listener, errorListener);
        assertEquals(a.getAccountId(), "A1.2");
    }

    @Test
    public void testGetAccountBalance(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException	{
    	Account a = sut.getAccount("A1.2", listener, errorListener);
        assertNotNull(a.getBalance().getBalance());
        assertNotNull(a.getBalance().getBalanceDate());
    }

    @Test
    public void testGetAccountTransactions(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException	{
    	Account a = sut.getAccount("A1.2", listener, errorListener);
        List<Transaction> ts = sut.getTransactions(a, listener, errorListener);
        assertTrue(ts.size() > 0);
    }

    @Test
    public void testGetAccountPayments(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException	{
    	Account a = sut.getAccount("A1.2", listener, errorListener);
        List<Payment> ps = sut.getPayments(a, listener, errorListener);
        assertTrue(ps.size() >= 0);
    }
    
    @Test
    public void testGetSupportedTanSchemes(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException	{
    	Account a = sut.getAccount("A1.1", listener, errorListener);
    	List<TanScheme> schemes = a.getSupportedTanSchemes();
    	assertTrue(schemes.size() == 3);
    }

    @Test
    public void testGetTransactions(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException {
        List<Transaction> transactions = sut.getTransactions(listener, errorListener);
        assertTrue(transactions.size() > 0);
    }

    @Test
    public void testGetNotifications(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException {
        List<Notification> notifications = sut.getNotifications(listener, errorListener);
        assertTrue(notifications.size() > 0);
    }

    @Test
    public void testGetPayments(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException {
        List<Payment> payments = sut.getPayments(listener, errorListener);
        assertTrue(payments.size() >= 0);
    }

    @Test
    public void testMissingHandling(Response.Listener<T> listener, Response.ErrorListener errorListener) throws IOException, FigoError {
        assertNull(sut.getAccount("A1.5", listener, errorListener));
    }

    @Test(expected=FigoError.class)
    public void testExceptionHandling(Response.Listener<T> listener, Response.ErrorListener errorListener) throws IOException, FigoError {
        sut.getSyncURL("", "http://localhost:3003/", listener, errorListener);
    }

    @Test
    public void testSyncUri(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException {
        assertNotNull(sut.getSyncURL("qwe", "http://figo.me/test", listener, errorListener));
    }

    @Test
    public void testUser(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException {
        User user = sut.getUser(listener, errorListener);
        assertEquals("demo@figo.me", user.getEmail());
    }

    @Test
    public void testCreateUpdateDeleteNotification(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException {
        Notification addedNotificaton = sut.addNotification(new Notification("/rest/transactions", "http://figo.me/test", "qwe"), listener, errorListener);
        assertNotNull(addedNotificaton.getNotificationId());
        assertEquals(addedNotificaton.getObserveKey(), "/rest/transactions");
        assertEquals(addedNotificaton.getNotifyURI(), "http://figo.me/test");
        assertEquals(addedNotificaton.getState(), "qwe");

        addedNotificaton.setState("asd");
        Notification updatedNotification = sut.updateNotification(addedNotificaton, listener, errorListener);
        assertEquals(updatedNotification.getNotificationId(), addedNotificaton.getNotificationId());
        assertEquals(updatedNotification.getObserveKey(), "/rest/transactions");
        assertEquals(updatedNotification.getNotifyURI(), "http://figo.me/test");
        assertEquals(updatedNotification.getState(), "asd");

        sut.removeNotification(updatedNotification, listener, errorListener);

        Notification reretrievedNotification = sut.getNotification(addedNotificaton.getNotificationId(), listener, errorListener);
        assertNull(reretrievedNotification);
    }

    @Test
    public void testCreateUpdateDeletePayment(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException {
        Payment addedPayment = sut.addPayment(new Payment("Transfer", "A1.1", "4711951501", "90090042", "figo", "Thanks for all the fish.", new BigDecimal(0.89)), listener, errorListener);
        assertNotNull(addedPayment.getPaymentId());
        assertEquals("A1.1", addedPayment.getAccountId());
        assertEquals("Demobank", addedPayment.getBankName());
        assertEquals(0.89f, addedPayment.getAmount().floatValue(), 0.0001);

        addedPayment.setAmount(new BigDecimal(2.39));
        Payment updatedPayment = sut.updatePayment(addedPayment, listener, errorListener);
        assertEquals(addedPayment.getPaymentId(), updatedPayment.getPaymentId());
        assertEquals("A1.1", updatedPayment.getAccountId());
        assertEquals("Demobank", updatedPayment.getBankName());
        assertEquals(2.39f, updatedPayment.getAmount().floatValue(), 0.0001);

        sut.removePayment(updatedPayment, listener, errorListener);

        Payment reretrievedPayment = sut.getPayment(addedPayment.getAccountId(), addedPayment.getPaymentId(), listener, errorListener);
        assertNull(reretrievedPayment);
    }

    public void testGetPaymentProposals(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException	{
    	List<PaymentProposal> proposals = sut.getPaymentProposals(listener, errorListener);
    	assertEquals(2, proposals.size());
    }
    
    @Test
    public void testGetSupportedPaymentTypes(Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException	{
    	HashMap<String, PaymentType> types = sut.getAccounts(listener, errorListener).get(0).getSupportedPaymentTypes();
    	assertEquals(2, types.size());
    }
    
    @Test
	public void testGetStandingOrders(Response.Listener<T> listener, Response.ErrorListener errorListener) throws IOException, FigoError {
        List<StandingOrder> so = sut.getStandingOrders(listener, errorListener);
        assertTrue(so.size() > 0);
	}

}
