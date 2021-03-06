package sample.accounts;

import sample.utils.repository.Commitable;
import lajkonik.fp.IO;


/**
 * A service for retrieving accounts
 */
public interface AccountsRepository extends OrdersLog, Commitable<Order>
{
    /**
     * Gets an account by its ID
     * @param id ID
     * @return an instance of Account
     */
    IO<? extends Account> account(AccountNumber id);

}
