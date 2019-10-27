package sample.accounts;

import lajkonik.fp.IO;
import sample.utils.repository.Transaction;
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
    private int counter = 1;
    

    public SimpleAccountsRepository(List<Order> ordersLog, Account... accounts) {
        this.ordersLog = new ArrayList<>(ordersLog);
        this.accounts = Arrays.asList(accounts);
    }

    @Override
    public IO<? extends Account> account(AccountNumber number) {
        return IO.of(()-> { try { return getAccount(number);
                                } catch (Exception ex ) { 
                                                            throw new RuntimeException(ex);}}); //TODO rework
    }

    private Account getAccount(AccountNumber number) {
        return accounts.stream().filter( p -> p.number().equals(number))
                                                        .findFirst()
                                                        .map(p -> new DefaultAccount(p.number(), p.ownerName(), queryForAccount(number)))
                                                        .orElseThrow(() -> new TransferException("Not found"));
    }
    
    @Override
    public List<Order> queryForAccount(AccountNumber accountNumber) {
        return ordersLog.stream().filter(o -> o.accountNumber().equals(accountNumber)).collect(Collectors.toList());
    }

    @Override
    public synchronized IO<Integer> commit(Transaction<Order> transaction) {
        return IO.of(() -> { 
                transaction.transactionLog().forEach((o) -> {
                    ordersLog.add(o);
            });
            return counter++;
        }); 
    }
}
