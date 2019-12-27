package rev.gretty.homerest.unit.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import rev.gretty.homerest.entity.BankTransaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class BankTransactionsAsArgument implements ArgumentsProvider {

        protected BankTransaction toTest = null;

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {

                List toReturn = new ArrayList< BankTransaction >();

                toTest = new BankTransaction();

                toTest.setCreditTransaction();
                toTest.setCurrencyUnit( "EUR" );
                toTest.setTransactionAmount( BigDecimal.TEN.multiply ( new BigDecimal(  10 )) );
                toTest.setTransactionFrom( "LT043500000228574981" );
                toTest.setTransactionTo( "LT133500075675655357" );

                toReturn.add( toTest );

                toTest.setDebitTransaction();
                toTest.setTransactionAmount( BigDecimal.TEN );

                toReturn.add( toTest );

                toTest.setTransactionAmount( BigDecimal.TEN );

                toReturn.add( toTest );

                toTest.setTransactionAmount( BigDecimal.TEN );

                toReturn.add( toTest );

                toTest.setTransactionAmount( new BigDecimal( "20.0" ) );

                toReturn.add( toTest );

                toTest.setCreditTransaction();
                toTest.setTransactionAmount( new BigDecimal( "21.22" ) );

                toReturn.add( toTest );

        return toReturn.stream().map(Arguments::of);
    }

}
