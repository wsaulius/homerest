package rev.gretty.homerest.service.impl;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rev.gretty.homerest.service.IFunctionalsBankAcountHelper;

import javax.inject.Singleton;
import java.io.Serializable;
import java.util.function.BiConsumer;

@Singleton
public class FunctorBiConsumeService<ToConsumeObj, IConsumeService> implements IFunctionalsBankAcountHelper {

    private static final Logger log = LoggerFactory.getLogger(FunctorBiConsumeService.class);

    protected IConsumeService iService;
    protected ToConsumeObj iConsume;

    // Do not call, protected
    protected FunctorBiConsumeService() {
    }

    // The only public
    public <T extends Serializable> FunctorBiConsumeService(T resource, IConsumeService service) {

        this.iService = service;
        this.iConsume = (ToConsumeObj) resource;

        log.info("IN FUNCTOR BASE :: " + iService.getClass() + " :: " + resource.getClass());
    }

    @Override
    public void accept(Object o, Object o2) {

        log.info("ACCEPT IN FUNCTOR BASE :: " + iService.getClass());
        // Implementations are virtual
    }

    @NotNull
    @Override
    public BiConsumer andThen(@NotNull BiConsumer biConsumer) {
        return this;
    }

    @Override
    public Throwable getException() {
        return new Throwable();
    }
}
