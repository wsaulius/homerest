package rev.gretty.homerest.service.fortest;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.exception.BankAccountDataException;
import rev.gretty.homerest.persistence.HibernateSessionUtils;
import rev.gretty.homerest.service.IAccountService;
import rev.gretty.homerest.service.impl.AccountServiceImpl;

import javax.inject.Singleton;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

@Singleton
@Transactional
public class AccountServiceImpl4Test implements IAccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountServiceImpl4Test.class);

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

        Session session = HibernateSessionUtils.getSessionFactory().openSession();

        Transaction transaction = null;
        try {

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
    public boolean updateAccount(BankAccount bankAccount) throws Exception {
        return false;
    }

    @Override
    public boolean putAllAccounts(Collection<BankAccount> collection) throws Exception {
        return false;
    }

    @Override
    @Transactional
    public Collection<BankAccount> getAllAccounts() throws Exception {

        Transaction transaction = null;
        List<BankAccount> listOfAccounts = null;

        try (Session session = HibernateSessionUtils.getSessionFactory().openSession()) {
            // start a transaction
            transaction = session.beginTransaction();
            listOfAccounts = session.createQuery("from BankAccount").getResultList();

            // commit transaction
            transaction.commit();
        } catch (Exception e) {

            e.printStackTrace();
            if (transaction != null) {
                transaction.rollback();
            }

        }
        return listOfAccounts;
    }

    @Override
    public Collection<BankAccount> transactInAccounts( final BankTransaction transaction,
            final Collection<BankAccount> collection) throws Exception {

        log.info( "TRANSCT in " + this.getClass() + " but routed to "  + AccountServiceImpl.class );
        return new AccountServiceImpl().transactInAccounts( transaction, collection );

    }
}
