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
import rev.gretty.homerest.exception.BankAccountDataException;
import rev.gretty.homerest.exception.BankTransactionProcessingException;
import rev.gretty.homerest.exception.CurrencyAmountException;
import rev.gretty.homerest.persistence.HibernateSessionUtils;
import rev.gretty.homerest.service.IAccountService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.FlushModeType;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;

@Singleton
@Transactional
public class AccountServiceImpl implements IAccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    // NO inject possible here, nor needed
    // private IAccountService accountService;

    @Inject
    private BankTransaction bankTransaction;

    public AccountServiceImpl() {

        //accountService = new AccountServiceImpl4Test();
        log.info( "Initialize  " + this.getClass() );
    }

    @Override
    @Transactional
    public Optional<BankAccount> getAccountByUUID(UUID accountId) throws Exception {

        BankAccount bankAccount = new BankAccount();
        Transaction transaction = null;
        Optional<BankAccount> optionalBankAccount = Optional.of(bankAccount);

        try (Session session = HibernateSessionUtils.getSessionFactory().openSession()) {

            // start a transaction
            transaction = session.beginTransaction();

            TypedQuery<BankAccount> query =
                    session.createQuery("from BankAccount AA where AA.id = :account_id",
                            BankAccount.class);

            query.setParameter("account_id", accountId);
            List<BankAccount> results = query.getResultList();

            if (results.size() > 1) {
                throw new BankAccountDataException(
                        "There should be only one unique UUID matching record. " +
                                "Check unique constraints for " + BankAccount.class);
            }

            optionalBankAccount = Optional.of(results.get(0));

            // commit transaction
            transaction.commit();

        } catch (ClassCastException e) {

            e.printStackTrace();
            log.error(e.getMessage());

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return optionalBankAccount;
    }

    @Override
    @Transactional
    public Optional<BankAccount> getAccountByNumber(String accountIdNumber) throws Exception {

        BankAccount bankAccount = new BankAccount();
        Transaction transaction = null;
        Optional<BankAccount> optionalBankAccount = Optional.of(bankAccount);

        Session session = HibernateSessionUtils.getSessionFactory().openSession();

        try {

            // start a transaction
            transaction = session.beginTransaction();
            log.info( "GET ACC ID:: " + accountIdNumber );

            final ConcurrentSkipListSet<BankAccount> concurrentReadList = new  ConcurrentSkipListSet<BankAccount>();

            concurrentReadList.addAll( session.getNamedQuery("SQL_GET_ACCOUNTS_BY_NUMBER")
                    .setParameter("account_number", accountIdNumber)

                    // TODO: Configure in properties
                    .setMaxResults(50)
                    .list() );

            // PARALLEL is working fine here
            concurrentReadList.parallelStream().forEach(account -> {

                log.info(" DB:: " + ((BankAccount) account).getId());
            });

            if (concurrentReadList.size() == 0) {
                throw new BankAccountDataException(
                        "There should be only one unique UUID matching record. " +
                                "Check unique constraints for " + BankAccount.class);
            }

            optionalBankAccount = Optional.of( (BankAccount) concurrentReadList.first() );

            // commit transaction
            transaction.commit();

        } catch (ClassCastException e) {

            e.printStackTrace();
            log.error(e.getMessage());

        } catch (Exception e) {

            if (transaction != null) {
                try {
                    transaction.rollback();
                } catch ( IllegalStateException is ) {
                    // Happens sometimes
                }
            }
            throw e;

        } finally {
            session.close();
        }

        return optionalBankAccount;
    }

    @Override
    @Transactional
    public boolean putAccount(BankAccount bankAccount) throws Exception {

        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateSessionUtils.getSessionFactory().openSession();

            transaction = session.beginTransaction();
            session.save(bankAccount);
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
    @Transactional
    public boolean updateAccount(BankAccount bankAccount) throws Exception {

        Session session = null;
        Transaction transaction = null;

        log.info( "SAVE!COMMIT" );
        try {
            session = HibernateSessionUtils.getSessionFactory().openSession();

            transaction = session.beginTransaction();

            final Query query = session.getNamedQuery("SQL_UPDATE_ACCOUNTS_BY_NUMBER");

                query.setParameter("set_status", bankAccount.getAccountStatus() )
                        .setParameter("date_updated", bankAccount.getUpdateAt() )
                        .setParameter("balance", bankAccount.getCurrentBalance() )
                        .setParameter( "account_number", bankAccount.getAccountNumber() )
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


    @Override
    public boolean putAllAccounts(Collection<BankAccount> collection) throws Exception {
        return false;
    }

    // Test the concept for BankAccounts, same for Transactions
    protected class ConcurrencyAccess< T extends Serializable > implements Runnable {

        private T resource;
        private WeakReference<ReentrantLock> lock;
        private Condition condition = null;

        public ConcurrencyAccess(T entity, Condition condition){

            log.info( "Init THR:: run as thread " + Thread.currentThread().getName() );

            this.resource = entity;
            this.lock = new WeakReference<>( new ReentrantLock() );

            this.condition = condition;
        }

        @Override
        public void run() {

            int holdThemAllFlag = 0;

            // By default, parallel streams will be joined automatically as ForkJoinPool
            log.info( "THR:: run as thread " + Thread.currentThread().getName() );

            try {

                if ( lock.get().tryLock(10, TimeUnit.MILLISECONDS ) ) {

                        if ( this.resource instanceof BankTransaction ) {
                            // log.info( "LOCK::TRNS " + resource.toString() );

                            final IAccountService accountService = new AccountServiceImpl();

                            new FunctorBiConsumeService4Accounts( resource, accountService )
                            .accept( resource, accountService );
                        }

                } else {

                    synchronized ( resource ) {
                        log.info( "LOCK::WAIT " + resource.toString() );
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
            // System.out.println( "UNLOCK:: " + resource.toString() );
        }
    }

    @Override
    @Transactional
    public Collection<BankAccount> getAllAccounts() throws Exception {

        final ConcurrentSkipListSet<BankAccount> concurrentReadList = new ConcurrentSkipListSet<>();
            Transaction transaction = null;
            Session session = null;

            try {
                 session = HibernateSessionUtils.getSessionFactory().openSession();
                // start a transaction
                transaction = session.beginTransaction();
                concurrentReadList.addAll( session.createQuery("from BankAccount").getResultList() );

                // commit transaction
                transaction.commit();
            } catch (Exception e) {

                if (transaction != null) {
                    transaction.rollback();
                }

                e.printStackTrace();

            } finally {
                session.close();
            }

        return concurrentReadList;
    }

    @Override
    @Transactional
    public Collection< BankAccount> transactInAccounts( final BankTransaction bankTransaction,
                                       final Collection<BankAccount> collection) throws Exception {

        // OK, so transfer accounts
        log.info( "TRANSCT for :: " + collection.size() );

         BankAccount balanceFrom = null;
         BankAccount balanceTo = null;

         // Compare by currencies, conversion is not implemented

        synchronized ( collection ) {
            ArrayList<BankAccount> asArray = new ArrayList( collection );

            // There always should be two:
              balanceFrom = asArray.get( 0 );
              balanceTo = asArray.get( 1 );
        }

        // Do that in parallel if needed. // TODO: TOO expensive (time!)
        for ( BankAccount forIteratedIDsOnly : collection ) {

            // VERY IMPORTANT! UPDATE READ BEFORE ALL
            final BankAccount account = this.getAccountByNumber( forIteratedIDsOnly.getAccountNumber() ).get();

            log.info( "AMOUNT of: " + account.getCurrentBalance() );

            // One by one read: in *concurrent* mode // Auto-destroy it
            SoftReference<ReentrantLock> outerLock = new SoftReference<ReentrantLock>( new ReentrantLock( ) );
            try {
                outerLock.get().lockInterruptibly();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {

                if ( balanceFrom.getCurrencyUnit().contentEquals( balanceTo.getCurrencyUnit()) )
                {

                    if ( balanceFrom.getCurrencyUnit().contentEquals( bankTransaction.getCurrencyUnit() )) {
                        log.info("Accounts with matching currencies found. Continue processing");
                    } else {

                        throw new BankTransactionProcessingException(
                                String.format(" Bank transaction requested is in different Currency: %s and (%s)",
                                        bankTransaction.getCurrencyUnit(), balanceFrom.getCurrencyUnit(),
                                new CurrencyAmountException( "Current implementation does not foreign currencies. Rollbacked.")));
                    }

                } else {
                    throw new BankTransactionProcessingException(
                            String.format(" Bank accounts are found in different Currency: %s (%s) and %s (%s)",
                                    balanceFrom.getAccountNumber(), balanceFrom.getCurrencyUnit(),
                                    balanceTo.getAccountNumber(), balanceTo.getCurrencyUnit() ),
                            new CurrencyAmountException( "Current implementation does not foreign currencies. Rollbacked."));
                }

                if ( bankTransaction.isCredit() &&
                        bankTransaction.getTransactionFrom().contentEquals( account.getAccountNumber()) ) {

                    log.info("CREDIT TRANSCT FROM object :: " + account.toString() + "@" +
                            Thread.currentThread().getName());

                    log.info( "UPDT EUR:: " + bankTransaction.getTransactionAmount() );

                    synchronized (account) {
                        try {

                            balanceFrom = this.getAccountByNumber( bankTransaction.getTransactionFrom() ).get();
                            log.info( "Before UPDT FROM: " +  balanceFrom );

                            balanceFrom.setAccountStatus(10); // In-process balance outgoing
                            balanceFrom.setUpdateAt(null);

                            BigDecimal newBalance =
                                    balanceFrom.getCurrentBalance().add(
                                            bankTransaction.getTransactionAmount().multiply(
                                        new BigDecimal( bankTransaction.getTransactionType() ).negate() ));

                            if ( newBalance.compareTo( BigDecimal.ZERO ) == -1 ) {
                                throw new BankTransactionProcessingException( newBalance + " is negative.",
                                        new CurrencyAmountException( "Current implementation does not allow negative amounts. Rollbacked. ") );
                            }

                            balanceFrom.setCurrentBalance( newBalance );
                            newBalance = BigDecimal.ZERO;

                            balanceTo = this.getAccountByNumber( bankTransaction.getTransactionTo() ).get();
                            log.info( "Before UPDT TO: " +  balanceTo );

                            balanceTo.setAccountStatus(20); // In-process balance incoming
                            balanceTo.setUpdateAt( null );

                            newBalance = balanceTo.getCurrentBalance().add(
                                            bankTransaction.getTransactionAmount().multiply(
                                                    new BigDecimal( bankTransaction.getTransactionType() ) ));

                            if ( newBalance.compareTo( BigDecimal.ZERO ) == -1 ) {
                                throw new BankTransactionProcessingException( newBalance +  " is a negative number",
                                        new CurrencyAmountException( "Current implementation does not allow negative amounts. Rollbacked. ") );
                            }

                            balanceTo.setCurrentBalance( newBalance );

                            synchronized ( bankTransaction ) {

                                log.info( "UPDATED AS :" + balanceFrom.getAccountNumber() + " " + balanceFrom );
                                this.updateAccount(balanceFrom);

                                log.info( "UPDATED AS :" + balanceTo.getAccountNumber() + " "  + balanceTo );
                                this.updateAccount(balanceTo);
                            }

                        } catch (Exception e) {

                            e.printStackTrace();
                            throw new BankTransactionProcessingException(
                                    "Account " + account.getAccountNumber() + " update failed. ", e);
                        }
                    }
                } else

                if ( bankTransaction.isDebit() &&
                        bankTransaction.getTransactionTo().contentEquals(account.getAccountNumber())) {

                    log.info("DEBIT TRANSCT TO object :: " + account.toString() + "@" +
                            Thread.currentThread().getName());

                        try {

                            balanceFrom = this.getAccountByNumber( bankTransaction.getTransactionFrom() ).get();
                            log.info( "Before UPDT FROM: " +  balanceFrom );

                            balanceFrom.setAccountStatus(10); // In-process balance outgoing
                            balanceFrom.setUpdateAt(null);

                            BigDecimal newBalance =
                                    balanceFrom.getCurrentBalance().add(
                                            bankTransaction.getTransactionAmount().multiply(
                                                    new BigDecimal( bankTransaction.getTransactionType() ) ));

                            if ( newBalance.compareTo( BigDecimal.ZERO ) == -1 ) {
                                throw new BankTransactionProcessingException( newBalance + " is a negative number.",
                                        new CurrencyAmountException( "Current implementation does not allow negative amounts. Rollbacked. ") );
                            }

                            balanceFrom.setCurrentBalance( newBalance );
                            newBalance = BigDecimal.ZERO;

                            balanceTo = this.getAccountByNumber( bankTransaction.getTransactionTo() ).get();
                            log.info( "Before UPDT TO: " +  balanceTo );

                            balanceTo.setAccountStatus(20); // In-process balance incoming
                            balanceTo.setUpdateAt( null );

                            newBalance = balanceTo.getCurrentBalance().add(
                                    bankTransaction.getTransactionAmount().multiply(
                                            new BigDecimal( bankTransaction.getTransactionType() ).negate() ));

                            if ( newBalance.compareTo( BigDecimal.ZERO ) == -1 ) {
                                throw new BankTransactionProcessingException( newBalance + " is negative.",
                                        new CurrencyAmountException( "Current implementation does not allow negative amounts. Rollbacked. ") );
                            }

                            balanceTo.setCurrentBalance( newBalance );

                            synchronized ( bankTransaction ) {

                                log.info( "UPDATED AS :" + balanceFrom.getAccountNumber() + " " + balanceFrom );
                                this.updateAccount(balanceFrom);

                                log.info( "UPDATED AS :" + balanceTo.getAccountNumber() + " "  + balanceTo );
                                this.updateAccount(balanceTo);
                            }

                        } catch (Exception e) {

                            e.printStackTrace();
                            throw new BankTransactionProcessingException(
                                    "Account " + account.getAccountNumber() + " update failed. ", e);
                        }

                } else

                if (bankTransaction.getTransactionFrom().contentEquals(
                        bankTransaction.getTransactionTo())) {

                    throw new BankTransactionProcessingException(
                            // Must transfer to/from the same account
                            "There should be Currencies implemented for this transaction. Not in this MVP.");
                };

            } catch (Exception e) {

                log.error( e.getMessage() );
                throw e;

            } finally {

                outerLock.get().unlock();
            }
        }

        return Arrays.asList(new BankAccount[]{balanceFrom, balanceTo});
    }

}
