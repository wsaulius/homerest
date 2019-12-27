package rev.gretty.homerest.service;

import com.google.inject.ImplementedBy;
import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.service.impl.TransactionServiceImpl;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Service Interface, used by explicit setting in @GuiceBoundConfigModule
 * Initialized with a DEVT/PROD instance by default
 *
 */

@ImplementedBy(TransactionServiceImpl.class)
public interface ITransactionService {

    Optional<BankTransaction> getTransaction(UUID accountId) throws Exception;

    boolean putTransaction(final BankTransaction bankTransaction) throws Exception;

    Throwable processTransaction(final BankTransaction bankTransaction) throws Exception;

    boolean putAllTransactions(final Collection<BankTransaction> collection) throws Exception;

    Collection<BankTransaction> getAllTransactions() throws Exception;

    boolean closeTransaction(BankTransaction transaction) throws Exception;
}
