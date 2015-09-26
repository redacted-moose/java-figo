package me.figo.console_demo;

import com.android.volley.Response;

import java.io.IOException;

import me.figo.FigoError;
import me.figo.FigoSession;
import me.figo.models.Account;
import me.figo.models.Transaction;

public class ConsoleDemo {

    public static void main(String[] args, Response.Listener<T> listener, Response.ErrorListener errorListener) throws FigoError, IOException {
        FigoSession session = new FigoSession("ASHWLIkouP2O6_bgA2wWReRhletgWKHYjLqDaqb0LFfamim9RjexTo22ujRIP_cjLiRiSyQXyt2kM1eXU2XLFZQ0Hro15HikJQT_eNeT_9XQ");

        // print out a list of accounts including its balance
        for (Account account : session.getAccounts(listener, errorListener)) {
            System.out.println(account.getName());
            System.out.println(session.getAccountBalance(account, listener, errorListener).getBalance());
        }

        // print out the list of all transactions on a specific account
        for (Transaction transaction : session.getTransactions(session.getAccount("A1.2", listener, errorListener), listener, errorListener)) {
            System.out.println(transaction.getPurposeText());
        }
    }
}
