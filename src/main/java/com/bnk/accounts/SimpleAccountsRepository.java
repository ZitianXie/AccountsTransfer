package com.bnk.accounts;

import com.bnk.utils.repository.Transaction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


/**
 * In-memory 'database'
 */
public class SimpleAccountsRepository implements AccountsRepository {
    
    private final Collection<Account> accounts;
    private final List<Order> ordersLog;
    

    public SimpleAccountsRepository(List<Order> ordersLog, Account... accounts) {
        this.ordersLog = new ArrayList<>(ordersLog);
        this.accounts = Arrays.asList(accounts);
    }

    @Override
    public Account account(AccountNumber number) {
        final Account a = accounts.stream().filter( p -> p.number().equals(number) ).findFirst().orElse(null);
        return new DefaultAccount(a.number(), a.ownerName(), queryForAccount(number));
    }


    @Override
    public synchronized List<Order> queryForAccount(AccountNumber accountNumber) {
        return ordersLog.stream().filter(o -> o.accountNumber().equals(accountNumber)).collect(Collectors.toList());
    }

    @Override
    public synchronized void commit(Transaction<Order> transaction) {
        transaction.transactionLog().forEach((o) -> {
            ordersLog.add(o);
        });
    }


}
