package rev.gretty.homerest.service;

import com.google.inject.ImplementedBy;
import rev.gretty.homerest.service.impl.FunctorBiConsumeService4Accounts;

import java.util.function.BiConsumer;

@ImplementedBy(FunctorBiConsumeService4Accounts.class)
public interface IFunctionalsBankAcountHelper<One, Two> extends BiConsumer< One, Two >
{
    // Default for binding
    public Throwable getException();
}
