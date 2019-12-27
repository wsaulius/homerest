package rev.gretty.homerest.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.service.IAccountService;

import javax.inject.Singleton;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;

@Singleton
public class FunctorBiConsumeService4Accounts<ToConsumeObj, IConsumeService> extends FunctorBiConsumeService {

    private static final Logger log = LoggerFactory.getLogger(FunctorBiConsumeService4Accounts.class);
    protected WeakReference<ReentrantLock> lock = null;

    protected WeakReference< Throwable > exception =
            new WeakReference<>( new Exception("") );

    // Do not call, protected
    protected FunctorBiConsumeService4Accounts() {
        this.lock = new WeakReference<>(new ReentrantLock());
    }

    // The only public
    public <T extends Serializable> FunctorBiConsumeService4Accounts(T resource, IConsumeService service) {

        super.iService = service;
        super.iConsume = (ToConsumeObj) resource;

        log.info("IN FUNCTOR  :: " + iService.getClass() + " :: " + resource.getClass());
        this.lock = new WeakReference<>(new ReentrantLock());
    }

    @NotNull
    @Override
    public BiConsumer andThen(@NotNull BiConsumer biConsumer) {
        return this;
    }

    @Override
    public void accept(Object toConsume, Object toServe) {

        Byte holdThemAllFlag = 0;

        BankAccount accountFrom = null;
        BankAccount accountTo = null;

        try {

            // USE re-entrant lock
            if (lock.get().tryLock(5, TimeUnit.MILLISECONDS)) {

                // The Only combination
                if (iService instanceof IAccountService &&
                        toConsume instanceof BankTransaction) {

                    // Now, determine the type
                    final BankTransaction transaction = (BankTransaction) toConsume;
                    String lockForAccountInTransact = transaction.getTransactionFrom();

                    final IAccountService service = (IAccountService) iService;

                    // Get the transaction FROM
                    Optional<BankAccount> getOrNoBankAccount =
                            service.getAccountByNumber(lockForAccountInTransact);

                    if (getOrNoBankAccount.isPresent() &&
                            StringUtils.isNotBlank(getOrNoBankAccount.get().getAccountNumber())) {

                        // SAVE it:
                        accountFrom = getOrNoBankAccount.get();

                        log.info(" GOT :: ACC :: FROM " + getOrNoBankAccount.get());
                        holdThemAllFlag++;

                    } else {
                        try {
                            getOrNoBankAccount.get();

                        } catch (NoSuchElementException ne) {
                            log.info(" FREE :: AS VOID from :: " + lockForAccountInTransact);
                            lock.get().unlock();

                            // Exit *early* here, could not not acquire resource.
                            return;

                        } finally {

                            // Do not unlock yet, search for TO:
                            lockForAccountInTransact = null;
                            getOrNoBankAccount = null;
                        }
                    }

                    lockForAccountInTransact = ((BankTransaction) iConsume).getTransactionTo();

                    // Get the transaction TO
                    getOrNoBankAccount = service.getAccountByNumber(lockForAccountInTransact);

                    if (getOrNoBankAccount.isPresent() &&
                            StringUtils.isNotBlank(getOrNoBankAccount.get().getAccountNumber())) {

                        holdThemAllFlag++;
                        accountTo = getOrNoBankAccount.get();

                        if ( 2 == holdThemAllFlag ) {

                            log.error("BINGO!");

                            final Collection forUpdate = new ConcurrentLinkedDeque<BankAccount>();
                            forUpdate.addAll(Arrays.asList(new BankAccount[]{accountFrom, accountTo}));

                            try {
                                // OK, so now after all preparations we have accounts locked
                                service.transactInAccounts(transaction, forUpdate);
                            } catch ( Exception e ) {

                                log.error( "INTERCEPT: " + e.getMessage() );
                                this.setException( e );
                                throw e;
                            }
                        }

                        log.info("LOCK BOTH :: ACC :: FROM " + accountFrom + " :: TO " + accountTo);

                    } else {
                        try {
                            getOrNoBankAccount.get();

                        } catch (NoSuchElementException ne) {
                            log.info(" FREE :: AS VOID from :: " + getOrNoBankAccount.get());
                            lock.get().unlock();

                            // Exit *early* here, could not not acquire resource.
                            return;

                        } finally {

                            // Do not unlock yet, just reset
                        }
                    }
                }
            }

            log.info("OUT FUNCTOR ::");

        } catch (Exception e) {

            synchronized ( this.exception ) {

                e.printStackTrace();

                log.error( "CAPTURED AS: " + e.toString() );
                this.setException( e );
            }

            lock.get().unlock();
            log.warn(e.getMessage());

        } finally {

            // Clean exit
            this.lock = null;
        }
    }

    // Experimental getter
    public Throwable getException() {
        return exception.get();
    }

    // Experimental setter/getter
    public void setException( Exception throwable ) {

        log.error( "THROWS: " + throwable.toString() );
        exception = new WeakReference<>( throwable );
    }
}
