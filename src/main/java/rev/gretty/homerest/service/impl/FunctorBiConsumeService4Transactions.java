package rev.gretty.homerest.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.entity.BankAccount;
import rev.gretty.homerest.entity.BankTransaction;
import rev.gretty.homerest.guicebound.bound.IBankTransaction;
import rev.gretty.homerest.service.IAccountService;
import rev.gretty.homerest.service.ITransactionService;

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
public class FunctorBiConsumeService4Transactions<ToConsumeObj, IConsumeService> extends FunctorBiConsumeService {

    private static final Logger log = LoggerFactory.getLogger(FunctorBiConsumeService4Transactions.class);
    protected WeakReference<ReentrantLock> lock = null;

    protected WeakReference< Throwable > exception =
            new WeakReference<>( new Exception("") );

    // Do not call, protected
    protected FunctorBiConsumeService4Transactions() {
        this.lock = new WeakReference<>(new ReentrantLock());

    }

    // The protected
    public <T extends Serializable> FunctorBiConsumeService4Transactions(T resource, IConsumeService service ) {

        super.iService = service;
        super.iConsume = (ToConsumeObj) resource;

        log.info("IN FUNCTOR  :: " + iService.getClass() + " :: " + resource.getClass());
        this.lock = new WeakReference<>(new ReentrantLock());
    }

    // The public only now
    public <T extends Serializable> FunctorBiConsumeService4Transactions(
            T resource, IConsumeService service, Throwable throwable ) {
        this( resource, service );

        // Pass the exception from previous functor
        this.exception = new WeakReference<>( throwable );

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

        BankTransaction toClose = null;

        try {

            // USE re-entrant lock
            if (lock.get().tryLock(5, TimeUnit.MILLISECONDS)) {

                // The Only combination
                if (iService instanceof ITransactionService &&
                        toConsume instanceof BankTransaction) {

                    // Now, determine the type
                    final BankTransaction transaction = (BankTransaction) toConsume;
                    final ITransactionService service = (ITransactionService) iService;

                    // Close transactions, not no excp handling - catch all
                    service.closeTransaction(transaction);
                }

                log.info("OUT FUNCTOR :: " + this.getClass());
            }

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
