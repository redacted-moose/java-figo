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

import com.android.volley.Request;
import com.android.volley.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;

import me.figo.internal.AccountOrderRequest;
import me.figo.internal.SetupAccountRequest;
import me.figo.internal.SubmitPaymentRequest;
import me.figo.internal.SyncTokenRequest;
import me.figo.internal.TaskResponseType;
import me.figo.internal.TaskStatusRequest;
import me.figo.internal.TaskStatusResponse;
import me.figo.internal.TaskTokenResponse;
import me.figo.internal.VisitedRequest;
import me.figo.models.Account;
import me.figo.models.AccountBalance;
import me.figo.models.Bank;
import me.figo.models.BusinessProcess;
import me.figo.models.LoginSettings;
import me.figo.models.PaymentContainer;
import me.figo.models.PaymentProposal;
import me.figo.models.PaymentProposal.PaymentProposalResponse;
import me.figo.models.ProcessToken;
import me.figo.models.Security;
import me.figo.models.Service;
import me.figo.models.Notification;
import me.figo.models.Payment;
import me.figo.models.StandingOrder;
import me.figo.models.Transaction;
import me.figo.models.User;

import java.util.Collections;

/**
 * Main entry point to the data access-part of the figo connect java library.
 * Here you can retrieve all the data the user granted your app access to.
 *
 * @author Stefan Richter
 */
public class FigoSession extends FigoApi {

    public enum PendingTransactions {
        INCLUDED,
        EXCLUDED
    }

    public enum FieldVisited {
        VISITED,
        NOT_VISITED
    }

    /**
     * Creates a FigoSession instance
     *
     * @param accessToken
     *            the access token to bind this session to a user
     */
    public FigoSession(String accessToken) {
        this(accessToken, 10000);
    }

    /**
     * Creates a FigoSession instance
     *
     * @param accessToken
     *            the access token to bind this session to a user
     * @param timeout
     *            the timeout used for queries
     */
    public FigoSession(String accessToken, int timeout) {
        this(accessToken, timeout, "https://api.figo.me");
    }

    /**
     * Creates a FigoSession instance
     *
     * @param accessToken
     *            the access token to bind this session to a user
     * @param timeout
     *            the timeout used for queries
     * @param apiEndpoint
     *            which endpoint to use (customize for different figo deployment)
     */
    public FigoSession(String accessToken, int timeout, String apiEndpoint) {
        super(apiEndpoint, "Bearer " + accessToken, timeout);
    }

    /**
     * Get the current figo Account
     *
     * @return User for the current figo Account
     */
    public void getUser(Response.Listener<User> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/user", null, Request.Method.GET, User.class, listener, errorListener);
    }

    /**
     * Modify figo Account
     *
     * @param user
     *            modified user object to be saved
     * @return User object for the updated figo Account
     */
    public void updateUser(User user, Response.Listener<User> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/user", user, Request.Method.PUT, User.class, listener, errorListener);
    }

    /**
     * Delete figo Account
     */
    public void removeUser(Response.Listener<Void> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/user", null, Request.Method.DELETE, null, listener, errorListener);
    }

    /**
     * Returns a list of all supported credit cards and payment services for a country
     * @param countryCode
     * @return List of Services
     */
    public void getSupportedServices(String countryCode, final Response.Listener<List<Service>> listener, Response.ErrorListener errorListener) 	{
        Response.Listener<Service.ServiceResponse> wrapperListener = new Response.Listener<Service.ServiceResponse>() {
            @Override
            public void onResponse(Service.ServiceResponse response) {
                listener.onResponse(response == null ? null : response.getServices());
            }
        };

    	this.queryApi("/rest/catalog/services/" + countryCode, null, Request.Method.GET, Service.ServiceResponse.class, wrapperListener, errorListener);
    }

    /**
     * Returns the login settings for a specified banking or payment service
     * @param countryCode
     * @param bankCode
     * @return LoginSettings
     */
    public void getLoginSettings(String countryCode, String bankCode, Response.Listener<LoginSettings> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/catalog/banks/" + countryCode + "/" + bankCode, null, Request.Method.GET, LoginSettings.class, listener, errorListener);
    }

    @Deprecated
    /**
     * Returns a TaskToken for a new account creation task
     * @param bankCode
     * @param countryCode
     * @param loginName
     * @param pin
     * @return
     */
    public void setupNewAccount(String bankCode, String countryCode, String loginName, String pin, Response.Listener<TaskTokenResponse> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts", new SetupAccountRequest(bankCode, countryCode, loginName, pin), Request.Method.POST, TaskTokenResponse.class, listener, errorListener);
    }

    /**
     * Returns a TaskToken for a new account creation task
     * @param bankCode
     * @param countryCode
     * @param loginName
     * @param pin
     * @param
     * @return
     */
    public void setupNewAccount(String bankCode, String countryCode, String loginName, String pin, List<String> syncTasks, Response.Listener<TaskTokenResponse> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts", new SetupAccountRequest(bankCode, countryCode, loginName, pin, syncTasks), Request.Method.POST, TaskTokenResponse.class, listener, errorListener);
    }

    @Deprecated
    /**
     * Returns a TaskToken for a new account creation task
     * @param bankCode
     * @param countryCode
     * @param loginName
     * @param pin
     * @return
     */
    public void setupNewAccount(String bankCode, String countryCode, List<String> credentials, Response.Listener<TaskTokenResponse> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts", new SetupAccountRequest(bankCode, countryCode, credentials), Request.Method.POST, TaskTokenResponse.class, listener, errorListener);
    }

    /**
     * Returns a TaskToken for a new account creation task
     * @param bankCode
     * @param countryCode
     * @param loginName
     * @param pin
     * @return
     */
    public void setupNewAccount(String bankCode, String countryCode, List<String> credentials, List<String> syncTasks, Response.Listener<TaskTokenResponse> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts", new SetupAccountRequest(bankCode, countryCode, credentials, syncTasks), Request.Method.POST, TaskTokenResponse.class, listener, errorListener);
    }

    /**
     * All accounts the user has granted your app access to
     *
     * @return List of Accounts
     */
    public void getAccounts(final Response.Listener<List<Account>> listener, Response.ErrorListener errorListener)  {
        Response.Listener<Account.AccountsResponse> wrapperListener = new Response.Listener<Account.AccountsResponse>() {
            @Override
            public void onResponse(Account.AccountsResponse response) {
                listener.onResponse(response == null ? null : response.getAccounts());
            }
        };

        this.queryApi("/rest/accounts", null, Request.Method.GET, Account.AccountsResponse.class, wrapperListener, errorListener);
    }

    /**
     * Returns the account with the specified ID
     *
     * @param accountId
     *            figo ID of the account to be retrieved
     * @return Account or Null
     */
    public void getAccount(String accountId, Response.Listener<Account> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/accounts/" + accountId, null, Request.Method.GET, Account.class, listener, errorListener);
    }

    /**
     * Modify an account
     *
     * @param account
     *            the modified account to be saved
     * @return Account object for the updated account returned by server
     */
    public void updateAccount(Account account, Response.Listener<Account> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/accounts/" + account.getAccountId(), account, Request.Method.PUT, Account.class, listener, errorListener);
    }

    /**
     * Remove an account
     *
     * @param accountId
     *            ID of the account to be removed
     */
    public void removeAccount(String accountId, Response.Listener<Void> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/accounts/" + accountId, null, Request.Method.DELETE, null, listener, errorListener);
    }

    /**
     * Remove an account
     *
     * @param account
     *            Account to be removed
     */
    public void removeAccount(Account account, Response.Listener<Void> listener, Response.ErrorListener errorListener)  {
        removeAccount(account.getAccountId(), listener, errorListener);
    }

    /**
     * Returns the balance details of the account with he specified ID
     *
     * @param accountId
     *            figo ID of the account to be retrieved
     * @return AccountBalance or Null
     */
    public void getAccountBalance(String accountId, Response.Listener<AccountBalance> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/accounts/" + accountId + "/balance", null, Request.Method.GET, AccountBalance.class, listener, errorListener);
    }

    /**
     * Returns the balance details of the supplied account
     *
     * @param account
     *            account whose balance should be retrieved
     * @return AccountBalance or Null
     */
    public void getAccountBalance(Account account, Response.Listener<AccountBalance> listener, Response.ErrorListener errorListener)  {
        getAccountBalance(account.getAccountId(), listener, errorListener);
    }

    /**
     * Modify balance or account limits
     *
     * @param accountId
     *            ID of the account to be modified
     * @param accountBalance
     *            modified AccountBalance object to be saved
     * @return AccountBalance object for the updated account as returned by the server
     */
    public void updateAccountBalance(String accountId, AccountBalance accountBalance, Response.Listener<AccountBalance> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/accounts/" + accountId + "/balance", accountBalance, Request.Method.PUT, AccountBalance.class, listener, errorListener);
    }

    /**
     * Modify balance or account limits
     *
     * @param account
     *            account to be modified
     * @param accountBalance
     *            modified AccountBalance object to be saved
     * @return AccountBalance object for the updated account as returned by the server
     */
    public void updateAccountBalance(Account account, AccountBalance accountBalance, Response.Listener<AccountBalance> listener, Response.ErrorListener errorListener)  {
        updateAccountBalance(account.getAccountId(), accountBalance, listener, errorListener);
    }

    /**
     * Set new bank account sorting order
     * @param orderedList
     * 			List of accounts in the new order
     */
    public void setAccountOrder(List<Account> orderedList, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts", new AccountOrderRequest(orderedList), Request.Method.PUT, null, listener, errorListener);
    }

    /**
     * All transactions on all account of the user
     *
     * @return List of Transaction objects
     */
    public void getTransactions(Response.Listener<List<Transaction>> listener, Response.ErrorListener errorListener) throws UnsupportedEncodingException {
        getTransactions((String) null, listener, errorListener);
    }

    /**
     * Retrieve all transactions on a specific account of the user
     *
     * @param accountId
     *            the ID of the account for which to retrieve the transactions
     * @return List of Transactions
     */
    public void getTransactions(String accountId, Response.Listener<List<Transaction>> listener, Response.ErrorListener errorListener) throws UnsupportedEncodingException {
        this.getTransactions(accountId, null, null, null, null, listener, errorListener);
    }

    /**
     * Retrieve all transactions on a specific account of the user
     *
     * @param account
     *            the account for which to retrieve the transactions
     * @return List of Transactions
     */
    public void getTransactions(Account account, Response.Listener<List<Transaction>> listener, Response.ErrorListener errorListener) throws UnsupportedEncodingException {
        this.getTransactions(account, null, null, null, null, listener, errorListener);
    }

    /**
     * Get an array of Transaction objects, one for each transaction of the user matching the criteria. Provide null values to not use the option.
     *
     * @param account
     *            account for which to list the transactions
     * @param since
     *            this parameter can either be a transaction ID or a date
     * @param count
     *            limit the number of returned transactions
     * @param offset
     *            which offset into the result set should be used to determine the first transaction to return (useful in combination with count)
     * @param include_pending
     *            this flag indicates whether pending transactions should be included in the response; pending transactions are always included as a complete
     *            set, regardless of the `since` parameter
     * @return an array of Transaction objects
     */
    public void getTransactions(Account account, String since, Integer count, Integer offset, PendingTransactions include_pending, Response.Listener<List<Transaction>> listener, Response.ErrorListener errorListener) throws UnsupportedEncodingException {
        getTransactions(account == null ? null : account.getAccountId(), since, count, offset, include_pending, listener, errorListener);
    }

    /**
     * Get an array of Transaction objects, one for each transaction of the user matching the criteria. Provide null values to not use the option.
     *
     * @param accountId
     *            ID of the account for which to list the transactions
     * @param since
     *            this parameter can either be a transaction ID or a date
     * @param count
     *            limit the number of returned transactions
     * @param offset
     *            which offset into the result set should be used to determine the first transaction to return (useful in combination with count)
     * @param include_pending
     *            this flag indicates whether pending transactions should be included in the response; pending transactions are always included as a complete
     *            set, regardless of the `since` parameter
     * @return an array of Transaction objects
     */
    public void getTransactions(String accountId, String since, Integer count, Integer offset, PendingTransactions include_pending, final Response.Listener<List<Transaction>> listener, Response.ErrorListener errorListener) throws UnsupportedEncodingException {
        String path = "";
        if (accountId == null) {
            path += "/rest/transactions?";
        } else {
            path += "/rest/accounts/" + accountId + "/transactions?";
        }
        if (since != null) {
            path += "since=" + URLEncoder.encode(since, "ISO-8859-1") + "&";
        }
        if (count != null) {
            path += "count=" + count + "&";
        }
        if (offset != null) {
            path += "offset=" + offset + "&";
        }
        if (include_pending != null) {
            path += "include_pending=" + (include_pending == PendingTransactions.INCLUDED ? "1" : "0");
        }

        Response.Listener<Transaction.TransactionsResponse> wrapperListener = new Response.Listener<Transaction.TransactionsResponse>() {
            @Override
            public void onResponse(Transaction.TransactionsResponse response) {
                listener.onResponse(response == null ? Collections.<Transaction>emptyList() : response.getTransactions());
            }
        };

        this.queryApi(path, null, Request.Method.GET, Transaction.TransactionsResponse.class, wrapperListener, errorListener);
    }

    /**
     * Retrieve a specific transaction by ID
     *
     * @param accountId
     *            ID of the account on which the transaction occurred
     * @param transactionId
     *            the figo ID of the specific transaction
     * @return Transaction or null
     */
    public void getTransaction(String accountId, String transactionId, Response.Listener<Transaction> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/accounts/" + accountId + "/transactions/" + transactionId, null, Request.Method.GET, Transaction.class, listener, errorListener);
    }

    /**
     * Modifies the visited field of a specific transaction
     * @param transaction
     * 				transaction which will be modified
     * @param visited
     * 				new value for the visited field
     */
    public void modifyTransaction(Transaction transaction, FieldVisited visited, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts/" + transaction.getAccountId() + "/transactions/" + transaction.getTransactionId(), new VisitedRequest(visited == FieldVisited.VISITED), Request.Method.PUT, null, listener, errorListener);
    }


    /**
     * Modifies the visited field of all transactions of the current user
     * @param visited
     * 			new value for the visited field
     */
    public void modifyTransactions(FieldVisited visited, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/transactions", new VisitedRequest(visited == FieldVisited.VISITED), Request.Method.PUT, null, listener, errorListener);
    }

    /**
     * Modifies the visited field of all transactions of a specific account
     * @param account
     * 			account which owns the transactions
     * @param visited
     * 			new value for the visited field
     */
    public void modifyTransactions(Account account, FieldVisited visited, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts/" + account.getAccountId() + "/transactions", new VisitedRequest(visited == FieldVisited.VISITED), Request.Method.PUT, null, listener, errorListener);
    }

    /**
     * Modifies the visited field of all transactions of a specific account
     * @param accountId
     * 			Id of the account which owns the transactions
     * @param visited
     * 			new value for the visited field
     */
    public void modifyTransactions(String accountId, FieldVisited visited, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts/" + accountId + "/transactions", new VisitedRequest(visited == FieldVisited.VISITED), Request.Method.PUT, null, listener, errorListener);
    }

    /**
     * Removes a Transaction
     * @param transaction
     * 				transaction which will be removed
     */
    public void removeTransaction(Transaction transaction, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts/" + transaction.getAccountId() + "/transactions/" + transaction.getTransactionId(), null, Request.Method.DELETE, null, listener, errorListener);
    }

    /**
     * Get an array of standing orders objects, one for each standing order of the user matching the criteria. Provide null values to not use the option.
     *
     * @param accountId
     *            ID of the account for which to list the standing orders
     * @return an array of Standing Order objects
     */
    public void getStandingOrders(String accountId, final Response.Listener<List<StandingOrder>> listener, Response.ErrorListener errorListener)  {
        String path = "";
        if (accountId == null) {
            path += "/rest/standing_orders";
        } else {
            path += "/rest/accounts/" + accountId + "/standing_orders";
        }

        Response.Listener<StandingOrder.StandingOrdersResponse> wrapperListener = new Response.Listener<StandingOrder.StandingOrdersResponse>() {
            @Override
            public void onResponse(StandingOrder.StandingOrdersResponse response) {
                listener.onResponse(response == null ? Collections.<StandingOrder>emptyList() : response.getStandingOrders());
            }
        };

        this.queryApi(path, null, Request.Method.GET, StandingOrder.StandingOrdersResponse.class, wrapperListener, errorListener);
    }

    /**
     * All standing orders on all accounts of the user
     *
     * @return List of Standing Order objects
     */
    public void getStandingOrders(Response.Listener<List<StandingOrder>> listener, Response.ErrorListener errorListener)  {
        getStandingOrders((String) null, listener, errorListener);
    }

    /**
     * Retrieve a specific standing order by ID
     *
     * @param accountId
     *            ID of the account on which the transaction occurred
     * @param standingOrderId
     *            the figo ID of the specific standingOrder
     * @return Standing Order or null
     */
    public void getStandingOrder(String accountId, String standingOrderId, Response.Listener<StandingOrder> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/accounts/" + accountId + "/standing_orders/" + standingOrderId, null, Request.Method.GET, StandingOrder.class, listener, errorListener);
    }

    /**
     * Retrieves a specific security
     * @param accountId
     * 			id of the security owning account
     * @param securityId
     * 			id of the security which will be retrieved
     * @return	Security or null
     */
    public void getSecurity(String accountId, String securityId, Response.Listener<Security> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts/" + accountId + "/securities/" + securityId, null, Request.Method.GET, Security.class, listener, errorListener);
    }

    /**
     * Retrieves a specific security
     * @param account
     * 			owning account
     * @param securityId
     * 			id of the security which will be retrieved
     * @return	Security or null
     */
    public void getSecurity(Account account, String securityId, Response.Listener<Security> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts/" + account.getAccountId() + "/securities/" + securityId, null, Request.Method.GET, Security.class, listener, errorListener);
    }

    /**
     * Retrieves all securities of the current user
     * @return List of Securities or null
     */
    public void getSecurities(final Response.Listener<List<Security>> listener, Response.ErrorListener errorListener) 	{
        Response.Listener<Security.SecurityResponse> wrapperListener = new Response.Listener<Security.SecurityResponse>() {
            @Override
            public void onResponse(Security.SecurityResponse response) {
                listener.onResponse(response == null ? Collections.<Security>emptyList() : response.getSecurities());
            }
        };

    	this.queryApi("/rest/securities", null, Request.Method.GET, Security.SecurityResponse.class, wrapperListener, errorListener);
    }

    /**
     * Retrieves all securities of a specific account
     * @param account Security owning account
     * @return List of Securities or null
     */
    public void getSecurities(Account account, final Response.Listener<List<Security>> listener, Response.ErrorListener errorListener) 	{
        Response.Listener<Security.SecurityResponse> wrapperListener = new Response.Listener<Security.SecurityResponse>() {
            @Override
            public void onResponse(Security.SecurityResponse response) {
                listener.onResponse(response == null ? Collections.<Security>emptyList() : response.getSecurities());
            }
        };

    	this.queryApi("/rest/accounts/" + account.getAccountId() + "/securities", null, Request.Method.GET, Security.SecurityResponse.class, wrapperListener, errorListener);
    }

    /**
     * Retrieves all securities of a specific account
     * @param accountId Security owning account id
     * @return List of Securities or null
     */
    public void getSecurities(String accountId, final Response.Listener<List<Security>> listener, Response.ErrorListener errorListener) 	{
        Response.Listener<Security.SecurityResponse> wrapperListener = new Response.Listener<Security.SecurityResponse>() {
            @Override
            public void onResponse(Security.SecurityResponse response) {
                listener.onResponse(response == null ? Collections.<Security>emptyList() : response.getSecurities());
            }
        };

    	this.queryApi("/rest/accounts/" + accountId + "/securities", null, Request.Method.GET, Security.SecurityResponse.class, wrapperListener, errorListener);
    }

    /**
     * Modifies the visited field of a specific security
     * @param security
     * 			security which will be modified
     * @param visited
     * 			new value for the visited field
     */
    public void modifySecurity(Security security, FieldVisited visited, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts/" + security.getAccountId() + "/securities/" + security.getSecurityId(), new VisitedRequest(visited == FieldVisited.VISITED), Request.Method.PUT, null, listener, errorListener);
    }

    /**
     * Modifies the visited field of all securities of the current user
     * @param visited
     * 			new value for the visited field
     */
    public void modifySecurities(FieldVisited visited, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/securities", new VisitedRequest(visited == FieldVisited.VISITED), Request.Method.PUT, null, listener, errorListener);
    }

    /**
     * Modifies the visited field of all securities of a specific account
     * @param account
     * 			account which owns the securities
     * @param visited
     * 			new value for the visited field
     */
    public void modifySecurities(Account account, FieldVisited visited, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts/" + account.getAccountId() + "/securities", new VisitedRequest(visited == FieldVisited.VISITED), Request.Method.PUT, null, listener, errorListener);
    }

    /**
     * Modifies the visited field of all securities of a specific account
     * @param accountId
     * 			Id of the account which owns the securities
     * @param visited
     * 			new value for the visited field
     */
    public void modifySecurities(String accountId, FieldVisited visited, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/rest/accounts/" + accountId + "/securities", new VisitedRequest(visited == FieldVisited.VISITED), Request.Method.PUT, null, listener, errorListener);
    }

    /**
     * Get bank
     *
     * @param bankId
     *            ID of the bank to be retrieved
     * @return Bank or null
     */
    public void getBank(String bankId, Response.Listener<Bank> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/banks/" + bankId, null, Request.Method.GET, Bank.class, listener, errorListener);
    }

    /**
     * Get Bank for account
     *
     * @param account
     *            Account for which to return the Bank
     * @return Bank or Null
     */
    public void getBank(Account account, Response.Listener<Bank> listener, Response.ErrorListener errorListener)  {
        getBank(account.getBankId(), listener, errorListener);
    }

    /**
     * Modify a bank
     *
     * @param bank
     *            modified bank object to be saved
     * @return Bank object for the updated bank
     */
    public void updateBank(Bank bank, Response.Listener<Bank> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/banks/" + bank.getBankId(), bank, Request.Method.PUT, Bank.class, listener, errorListener);
    }

    /**
     * Remove the stored PIN for a bank (if there was one)
     *
     * @param bankId
     *            ID of the bank whose pin should be removed
     */
    public void removeBankPin(String bankId, Response.Listener<Void> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/banks/" + bankId + "/remove_pin", null, Request.Method.POST, null, listener, errorListener);
    }

    /**
     * Remove the stored PIN for a bank (if there was one)
     *
     * @param bank
     *            bank whose pin should be removed
     */
    public void removeBankPin(Bank bank, Response.Listener<Void> listener, Response.ErrorListener errorListener)  {
        removeBankPin(bank.getBankId(), listener, errorListener);
    }

    /**
     * All notifications registered by this client for the user
     *
     * @return List of Notification objects
     */
    public void getNotifications(final Response.Listener<List<Notification>> listener, Response.ErrorListener errorListener)  {
        Response.Listener<Notification.NotificationsResponse> wrapperListener = new Response.Listener<Notification.NotificationsResponse>() {
            @Override
            public void onResponse(Notification.NotificationsResponse response) {
                listener.onResponse(response == null ? Collections.<Notification>emptyList() : response.getNotifications());
            }
        };

        this.queryApi("/rest/notifications", null, Request.Method.GET, Notification.NotificationsResponse.class, wrapperListener, errorListener);
    }

    /**
     * Retrieve a specific notification by ID
     *
     * @param notificationId
     *            figo ID for the notification to be retrieved
     * @return Notification or Null
     */
    public void getNotification(String notificationId, Response.Listener<Notification> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/notifications/" + notificationId, null, Request.Method.GET, Notification.class, listener, errorListener);
    }

    /**
     * Register a new notification on the server for the user
     *
     * @param notification
     *            Notification which should be registered
     * @return the newly registered Notification
     */
    public void addNotification(Notification notification, Response.Listener<Notification> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/notifications", notification, Request.Method.POST, Notification.class, listener, errorListener);
    }

    /**
     * Update a stored notification
     *
     * @param notification
     *            Notification with updated values
     */
    public void updateNotification(Notification notification, Response.Listener<Notification> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/notifications/" + notification.getNotificationId(), notification, Request.Method.PUT, Notification.class, listener, errorListener);
    }

    /**
     * Remove a stored notification from the server
     *
     * @param notification
     *            Notification to be removed
     */
    public void removeNotification(Notification notification, Response.Listener<Void> listener, Response.ErrorListener errorListener)  {
        this.queryApi("/rest/notifications/" + notification.getNotificationId(), null, Request.Method.DELETE, null, listener, errorListener);
    }

    /**
     * Retrieve all payments
     *
     * @return List of Payments
     */
    public void getPayments(final Response.Listener<List<Payment>> listener, Response.ErrorListener errorListener)  {
        Response.Listener<Payment.PaymentsResponse> wrapperListener = new Response.Listener<Payment.PaymentsResponse>() {
            @Override
            public void onResponse(Payment.PaymentsResponse response) {
                listener.onResponse(response == null ? Collections.<Payment>emptyList() : response.getPayments());
            }
        };

        this.queryApi("/rest/payments", null, Request.Method.GET, Payment.PaymentsResponse.class, wrapperListener, errorListener);
    }

    /**
     * Retrieve all payments on a certain account
     *
     * @param accountId
     *            the ID of the account for which to retrieve the payments
     * @return List of Payments
     */
    public void getPayments(String accountId, final Response.Listener<List<Payment>> listener, Response.ErrorListener errorListener)  {
        Response.Listener<Payment.PaymentsResponse> wrapperListener = new Response.Listener<Payment.PaymentsResponse>() {
            @Override
            public void onResponse(Payment.PaymentsResponse response) {
                listener.onResponse(response == null ? Collections.<Payment>emptyList() : response.getPayments());
            }
        };

        this.queryApi("/rest/accounts/" + accountId + "/payments", null, Request.Method.GET, Payment.PaymentsResponse.class, wrapperListener, errorListener);
    }

    /**
     * all payments on a certain account
     *
     * @param account
     *            the account for which to retrieve the payments
     * @return List of Payments
     */
    public void getPayments(Account account, Response.Listener<List<Payment>> listener, Response.ErrorListener errorListener)  {
        this.getPayments(account.getAccountId(), listener, errorListener);
    }

    /**
     * Retrieve a specific payment by ID
     *
     * @param accountId
     *            ID of the account on which the payment can be found
     * @param paymentId
     *            ID of the payment to be retrieved
     * @return Payment or Null
     */
    public void getPayment(String accountId, String paymentId, Response.Listener<Payment> listener, Response.ErrorListener errorListener) {
        this.queryApi("/rest/accounts/" + accountId + "/payments/" + paymentId, null, Request.Method.GET, Payment.class, listener, errorListener);
    }

    /**
     * Retrieve a specific payment by ID
     *
     * @param account
     *            the account on which the payment can be found
     * @param paymentId
     *            ID of the payment to be retrieved
     * @return Payment or Null
     */
    public void getPayment(Account account, String paymentId, Response.Listener<Payment> listener, Response.ErrorListener errorListener) {
        this.getPayment(account.getAccountId(), paymentId, listener, errorListener);
    }

    /**
     * Create a new payment
     *
     * @param payment
     *            Payment which should be created
     * @return the newly created payment
     */
    public void addPayment(Payment payment, Response.Listener<Payment> listener, Response.ErrorListener errorListener) {
        this.queryApi("/rest/accounts/" + payment.getAccountId() + "/payments", payment, Request.Method.POST, Payment.class, listener, errorListener);
    }

    public void addContainerPayment(PaymentContainer container, Response.Listener<PaymentContainer> listener, Response.ErrorListener errorListener) {
    	this.queryApi("/rest/accounts/" + container.getAccountId() + "/payments", container, Request.Method.POST, PaymentContainer.class, listener, errorListener);
    }

    /**
     * Returns a list of PaymentProposals.
     */
    public void getPaymentProposals(final Response.Listener<List<PaymentProposal>> listener, Response.ErrorListener errorListener) {
        Response.Listener<PaymentProposalResponse> wrapperListener = new Response.Listener<PaymentProposalResponse>() {
            @Override
            public void onResponse(PaymentProposalResponse response) {
                listener.onResponse(response == null ? Collections.<PaymentProposal>emptyList() : response.getPaymentProposals());
            }
        };

    	this.queryApi("/rest/adress_book", null, Request.Method.GET, PaymentProposalResponse.class, wrapperListener, errorListener);
    }

    /**
     * Update a stored payment
     *
     * @param payment
     *            Payment with updated values
     * @return updated Payment as returned by the server
     */
    public void updatePayment(Payment payment, Response.Listener<Payment> listener, Response.ErrorListener errorListener) {
        this.queryApi("/rest/accounts/" + payment.getAccountId() + "/payments/" + payment.getPaymentId(), payment, Request.Method.PUT, Payment.class, listener, errorListener);
    }

    /**
     * Remove a stored payment from the server
     *
     * @param payment
     *            payment to be removed
     */
    public void removePayment(Payment payment, Response.Listener<Void> listener, Response.ErrorListener errorListener) {
        this.queryApi("/rest/accounts/" + payment.getAccountId() + "/payments/" + payment.getPaymentId(), null, Request.Method.DELETE, null, listener, errorListener);
    }

    /**
     * Submit payment to bank server
     *
     * @param payment
     *            payment to be submitted
     * @param tanSchemeId
     *            TAN scheme ID of user-selected TAN scheme
     * @param state
     *            Any kind of string that will be forwarded in the callback response message
     * @return the URL to be opened by the user for the TAN process
     */
    public void submitPayment(Payment payment, String tanSchemeId, String state, Response.Listener<String> listener, Response.ErrorListener errorListener)  {
        submitPayment(payment, tanSchemeId, state, null, listener, errorListener);
    }

    /**
     * Submit payment to bank server
     *
     * @param payment
     *            payment to be submitted
     * @param tanSchemeId
     *            TAN scheme ID of user-selected TAN scheme
     * @param state
     *            Any kind of string that will be forwarded in the callback response message
     * @param redirectUri
     *            At the end of the submission process a response will be sent to this callback URL
     * @return the URL to be opened by the user for the TAN process
     */
    public void submitPayment(Payment payment, String tanSchemeId, String state, String redirectUri, final Response.Listener<String> listener, Response.ErrorListener errorListener)  {
        Response.Listener<TaskTokenResponse> wrapperListener = new Response.Listener<TaskTokenResponse>() {
            @Override
            public void onResponse(TaskTokenResponse response) {
                listener.onResponse(getApiEndpoint() + "/task/start?id=" + response.task_token);
            }
        };

        this.queryApi("/rest/accounts/" + payment.getAccountId() + "/payments/" + payment.getPaymentId() + "/submit",
                new SubmitPaymentRequest(tanSchemeId, state, redirectUri), Request.Method.POST, TaskTokenResponse.class, wrapperListener, errorListener);
    }

    /**
     * URL to trigger a synchronization. The user should open this URL in a web browser to synchronize his/her accounts with the respective bank servers. When
     * the process is finished, the user is redirected to the provided URL.
     *
     * @param state
     *            String passed on through the complete synchronization process and to the redirect target at the end. It should be used to validated the
     *            authenticity of the call to the redirect URL
     * @param redirect_url
     *            URI the user is redirected to after the process completes
     * @return the URL to be opened by the user
     */
    public void getSyncURL(String state, String redirect_url, final Response.Listener<String> listener, Response.ErrorListener errorListener)  {
        Response.Listener<TaskTokenResponse> wrapperListener = new Response.Listener<TaskTokenResponse>() {
            @Override
            public void onResponse(TaskTokenResponse response) {
                listener.onResponse(getApiEndpoint() + "/task/start?id=" + response.task_token);
            }
        };

        this.queryApi("/rest/sync", new SyncTokenRequest(state, redirect_url), Request.Method.POST, TaskTokenResponse.class, wrapperListener, errorListener);
    }

    /**
     * URL to trigger a synchronization. The user should open this URL in a web browser to synchronize his/her accounts with the respective bank servers. When
     * the process is finished, the user is redirected to the provided URL.
     *
     * @param state
     *            String passed on through the complete synchronization process and to the redirect target at the end. It should be used to validated the
     *            authenticity of the call to the redirect URL
     * @param redirect_url
     *            URI the user is redirected to after the process completes
     * @param syncTasks
     * 			  Tasks to sync while talking to the bank. Transactions are activated by default
     * @return the URL to be opened by the user
     */
    public void getSyncURL(String state, String redirect_url, List<String> syncTasks, final Response.Listener<String> listener, Response.ErrorListener errorListener) {
        Response.Listener<TaskTokenResponse> wrapperListener = new Response.Listener<TaskTokenResponse>() {
            @Override
            public void onResponse(TaskTokenResponse response) {
                listener.onResponse(getApiEndpoint() + "/task/start?id=" + response.task_token);
            }
        };

        this.queryApi("/rest/sync", new SyncTokenRequest(state, redirect_url, syncTasks), Request.Method.POST, TaskTokenResponse.class, wrapperListener, errorListener);
    }

    /**
     * Get the current status of a Task by id
     * @param tokenId
     * 			ID of the TaskToken which will be checked
     * @return	A TaskStatusResponse Object with information about the task.
     */
    public void getTaskState(String tokenId, Response.Listener<TaskStatusResponse> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/task/progress?id=" + tokenId, new TaskStatusRequest(tokenId), Request.Method.POST, TaskStatusResponse.class, listener, errorListener);
    }

    @Deprecated
    /**
     * Retrieves the current status of a Task and provide a PIN
     * @param tokenId
     * 			ID of the TaskToken which will be checked
     * @param pin
     * 			PIN which will be submitted
     * @return A TaskStatusResponse Object with information about the task.
     */
    public void getTaskState(String tokenId, String pin, Response.Listener<TaskStatusResponse> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/task/progress?id=" + tokenId, new TaskStatusRequest(tokenId, pin), Request.Method.POST, TaskStatusResponse.class, listener, errorListener);
    }

    public void submitResponseToTask(String tokenId, String response, TaskResponseType type, Response.Listener<TaskStatusResponse> listener, Response.ErrorListener errorListener) 	{
	/**
	 * This method is used to provide a response to a running Task.
	 * @param tokenId
	 * 			ID of the TaskToken which will receive the response
	 * @param response
	 * 			Your provided response as a String. For Boolean fields (SAVE_PIN and CONTINUE) the String values 0, 1 are used
	 * @param type
	 * 			Type of the response you want to submit. Available types are: PIN, SAVE_PIN, CHALLENGE and CONTINUE
	 *
	 */
    	TaskStatusRequest request = new TaskStatusRequest(tokenId);
    	switch (type) {
    	case PIN:
			request.setPin(response);
			break;
		case SAVE_PIN:
			request.setSavePin(response);
			break;
		case CHALLENGE:
			request.setResponse(response);
			break;
		case CONTINUE:
			request.setContinue(response);
			break;
		default:
			break;
		}
    	this.queryApi("/task/progress?id=" + tokenId, request, Request.Method.POST, TaskStatusResponse.class, listener, errorListener);
    }

    public void submitResponseToTask(TaskTokenResponse tokenResponse, String response, TaskResponseType type, Response.Listener<TaskStatusResponse> listener, Response.ErrorListener errorListener) 	{
    	/**
    	 * This method is used to provide a response to a running Task.
    	 * @param tokenResponse
    	 * 			Response object of a task creating method
    	 * @param response
    	 * 			Your provided response as a String. For Boolean fields (SAVE_PIN and CONTINUE) the String values 0, 1 are used
    	 * @param type
    	 * 			Type of the response you want to submit. Available types are: PIN, SAVE_PIN, CHALLENGE and CONTINUE
    	 *
    	 */
    	this.submitResponseToTask(tokenResponse.getTaskToken(), response, type, listener, errorListener);
    }

    /**
     * Start communication with bank server.
     * @param tokenResponse
     * 				TokenResponse Object
     */
    public void startTask(TaskTokenResponse tokenResponse, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/task/start?id=" + tokenResponse.task_token, null, Request.Method.GET, null, listener, errorListener);
    }

    /**
     * Start communication with bank server.
     * @param taskToken
     * 				Token ID
     */
    public void startTask(String taskToken, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/task/start?id=" + taskToken, null, Request.Method.GET, null, listener, errorListener);
    }

    /**
     * Cancels a given task if possible
     * @param tokenResponse
     * 				Token Response Object
     */
    public void cancelTask(TaskTokenResponse tokenResponse, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/task/cancel?id=" + tokenResponse.task_token, null, Request.Method.POST, null, listener, errorListener);
    }

    /**
     * Cancels a given task if possible
     * @param taskToken
     * 				Token ID
     */
    public void cancelTask(String taskToken, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/task/cancel?id=" + taskToken, null, Request.Method.POST, null, listener, errorListener);
    }

    public void startProcess(ProcessToken processToken, Response.Listener<Void> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/process/start?id=" + processToken.getProcessToken(), null, Request.Method.GET, null, listener, errorListener);
    }

    public void createProcess(BusinessProcess process, Response.Listener<ProcessToken> listener, Response.ErrorListener errorListener) 	{
    	this.queryApi("/client/process", process, Request.Method.POST, ProcessToken.class, listener, errorListener);
    }

    @Override
    protected <T> T processResponse(HttpURLConnection connection, Type typeOfT) throws IOException, FigoError {
        // process response
        int code = connection.getResponseCode();
        if (code == 404) {
            return null;
        }
        return super.processResponse(connection, typeOfT);
    }
}
