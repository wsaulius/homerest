package rev.gretty.homerest.service.impl;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.persistence.HibernateSessionUtils;
import rev.gretty.homerest.service.IAccountService;
import rev.gretty.homerest.service.ITransactionService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.FlushModeType;
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
public class TransactionServiceImpl implements ITransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Inject
    IAccountService accountService;

    public Optional<BankTransaction> getTransaction(UUID accountId) throws Exception {
        return Optional.of(new BankTransaction(0));
    }

    @Override
    public Throwable processTransaction(BankTransaction bankTransaction) throws Exception {

        ConcurrencyAccess startConcurrent = null;

        try {
            if (bankTransaction.isDebit() || bankTransaction.isCredit()) {

                log.info(bankTransaction.getTransactionTo());
                log.info(bankTransaction.getTransactionFrom());

                startConcurrent =
                        new ConcurrencyAccess(bankTransaction, null);

                startConcurrent.run();
                return startConcurrent.getException();
            }
        } catch ( Exception e ) {
            return e;
        }

        // Yes, the nulls are handled.
        return startConcurrent.getException();
    };

    @Override
    public boolean putTransaction(BankTransaction bankTransaction) throws Exception {

        Session session = HibernateSessionUtils.getSessionFactory().openSession();

        Transaction transaction = null;
        try {

            transaction = session.beginTransaction();
            session.save(bankTransaction);
            transaction.commit();
        } catch (ConstraintViolationException e) {
            log.error(e.getConstraintName());

            if (transaction != null)
                transaction.rollback();

        } catch (HibernateException e) {

            log.error(e.getMessage());
            if (transaction != null)
                transaction.rollback();
            throw e;

        } finally {
            session.close();
        }

        return true;

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

            log.info( "FOUND :: " + listOfBankTranscations.size() );

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

    // Test the concept for BankAccounts, same for Transactions
    private class ConcurrencyAccess<T extends Serializable> implements Runnable {

        private T resource;
        private WeakReference<ReentrantLock> lock;
        private Condition condition = null;

        public Throwable getException() {
            return exception;
        }

        // Empty
        private Throwable exception = new Exception( "" );

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

                            try {

                                final IAccountService service = new AccountServiceImpl();
                                final FunctorBiConsumeService executableFunctor =
                                    new FunctorBiConsumeService4Accounts( resource, service );
                                        executableFunctor.accept(resource, service);

                                        if ( null != executableFunctor.getException() ) {
                                            synchronized ( this.exception ) {
                                                this.exception = executableFunctor.getException();
                                            }
                                        }

                            } catch (Throwable t) {
                                log.error(t.getMessage());
                            }

                            try {

                                // cannot pass this. breaks SOLID
                                final ITransactionService service = new TransactionServiceImpl();

                                final FunctorBiConsumeService executableFunctor =
                                        new FunctorBiConsumeService4Transactions( resource, service );
                                executableFunctor.accept( resource, service );

                                if ( null != executableFunctor.getException() ) {
                                    synchronized ( this.exception ) {
                                        this.exception = executableFunctor.getException();
                                    }
                                }

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

                // COULD break the chain (?) // TODO RESEARCH TEST
                // throw e;
                synchronized ( this.exception ) {
                    this.exception = new Exception( e.getMessage(), getException() );
                }

            } finally {

                // Release locks anyway
                lock = null;
            }
        }
    }

    @Override
    @Transactional
    public boolean closeTransaction(BankTransaction bankTransaction) throws Exception {

        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateSessionUtils.getSessionFactory().openSession();
            log.info( "SAVE!COMMIT:TRANSACT" );

                    transaction = session.beginTransaction();

                    final Query query = session.getNamedQuery("SQL_UPDATE_ACCOUNTS_IN_TRANSACTION");

                    query.setParameter("from_acc", bankTransaction.getTransactionFrom() )
                            .setParameter("to_acc", bankTransaction.getTransactionTo() )
                            .setParameter( "date_updated", new BankAccount().getUpdateAt() )
                            .setParameter("set_status", 60 ) // For finished trnx
                            .setFlushMode( FlushModeType.COMMIT );

                    query.setMaxResults(1);
                    query.executeUpdate();

                    session.flush();
                    transaction.commit();

                } catch (ConstraintViolationException e) {
                    log.error(e.getConstraintName());

                    if (transaction != null)
                        transaction.rollback();

                } catch (HibernateException e) {

                    e.printStackTrace();
                    log.error(e.getMessage());
                    if (transaction != null)
                        transaction.rollback();
                    throw e;

                } finally {
                    session.close();
                }

                return true;
    }
}
