package rev.gretty.homerest.service.fortest;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.persistence.HibernateSessionUtils;
import rev.gretty.homerest.service.IAccountService;
import rev.gretty.homerest.service.ITransactionService;
import rev.gretty.homerest.service.impl.FunctorBiConsumeService4Accounts;
import rev.gretty.homerest.service.impl.TransactionServiceImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Singleton
@Transactional
public class TransactionServiceImpl4Test implements ITransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl4Test.class);

    @Inject
    private BankTransaction bankTransaction;

    public TransactionServiceImpl4Test() {
        log.info("Initialize  " + this);
    }

    @Override
    public Optional<BankTransaction> getTransaction(UUID accountId) throws Exception {
        return Optional.empty();
    }

    @Override
    public boolean putTransaction(BankTransaction bankTransaction) throws Exception {
        return new TransactionServiceImpl().putTransaction(bankTransaction);
    }

    @Override
    public boolean putAllTransactions(Collection<BankTransaction> collection) throws Exception {
        return false;
    }

    @Override
    public Collection<BankTransaction> getAllTransactions() throws Exception {

        Transaction transaction = null;
        List<BankTransaction> listOfBankTranscations = null;

        try (Session session = HibernateSessionUtils.getSessionFactory().openSession()) {

            // start a transaction
            transaction = session.beginTransaction();
            listOfBankTranscations = session.getNamedQuery("SQL_GET_ACCOUNTS_IN_TRANSACTION")
                    // TODO: Configurable
                    .setMaxResults(50)
                    .list();

            log.info("FOUND :: " + listOfBankTranscations.size());

            listOfBankTranscations.stream().forEach(oneBankTransactionToProcess -> {
                // log.info(" 4Test DB:: " + oneBankTransactionToProcess );

                try {

                    // PROCESS in DB
                    this.processTransaction(oneBankTransactionToProcess);

                } catch (Exception e) {

                    e.printStackTrace();
                    // do not throw out of this lambda expr

                }
            });

            // commit transaction
            transaction.commit();
        } catch (Exception e) {

            e.printStackTrace();
            if (transaction != null) {
                transaction.rollback();
            }

        }
        return listOfBankTranscations;
    }

    @Override
    public boolean closeTransaction(BankTransaction transaction) throws Exception {
        return false;
    }

    @Override
    public Throwable processTransaction(BankTransaction bankTransaction) throws Exception {

        new ConcurrencyAccess(bankTransaction, null).run();

        // ALL OK
        return new Throwable();
    }

    // Test the concept for BankAccounts, same for Transactions
    private class ConcurrencyAccess<T extends Serializable> implements Runnable {

        private T resource;
        private WeakReference<ReentrantLock> lock;
        private Condition condition = null;

        public ConcurrencyAccess(T entity, Condition condition) {

            this.resource = entity;
            this.lock = new WeakReference<>(new ReentrantLock());

            this.condition = condition;
        }

        @Override
        public void run() {

            int holdThemAllFlag = 0;
            try {

                log.info("Processing :: " + resource.toString());

                if (lock.get().tryLock(10, TimeUnit.MILLISECONDS)) {

                    {
                        if (this.resource instanceof BankTransaction) {
                            log.info("LOCK::TRNS " + resource.toString());

                            try {

                                final IAccountService service = new AccountServiceImpl4Test();
                                new FunctorBiConsumeService4Accounts(resource, service).accept(resource, service);

                            } catch (Throwable t) {

                                log.error(t.getMessage());
                            }
                            log.info("LOCK::FUNCTOR " + "EXIT");
                        }
                    }

                } else {

                    log.info("WAIT::MODE... ");
                    synchronized (resource) {

                        log.info("LOCK::WAIT " + resource.toString());
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();

            } catch (Exception e) {
                e.printStackTrace();

            } finally {

                // Release locks anyway
                lock = null;
            }
        }
    }

}
