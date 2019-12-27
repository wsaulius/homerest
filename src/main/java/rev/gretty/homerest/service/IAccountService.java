package rev.gretty.homerest.service;

import com.google.inject.ImplementedBy;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.service.impl.AccountServiceImpl;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@ImplementedBy(AccountServiceImpl.class)
public interface IAccountService {

    Optional<BankAccount> getAccountByUUID(UUID accountId) throws Exception;

    Optional<BankAccount> getAccountByNumber(String accountIdNumber) throws Exception;

    // INSERT
    boolean putAccount(final BankAccount bankAccount) throws Exception;

    // UPDATE
    boolean updateAccount(BankAccount bankAccount) throws Exception;

    // MULTIPLE INSERT
    boolean putAllAccounts(final Collection<BankAccount> collection) throws Exception;

    Collection< BankAccount> getAllAccounts() throws Exception;

    Collection< BankAccount> transactInAccounts(final BankTransaction transaction,
                          final Collection<BankAccount> collection) throws Exception;
}
