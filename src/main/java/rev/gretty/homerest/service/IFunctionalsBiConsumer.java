package rev.gretty.homerest.service;

import java.util.List;
import java.util.function.BiConsumer;

public interface IFunctionalsBiConsumer< P , Q > {

    // BiConsumer for Dependent transactions (?? TODO: )
    default P consume(P a, Q b) {
        BiConsumer<List<P>, List<Q>> equals = (one, two) ->
        {
            if (one.size() != two.size()) {
                System.out.println("False");
            } else {
                for (int i = 0; i < one.size(); i++)

                    if (one.get(i) != two.get(i)) {
                        System.out.println("False");
                        return;
                    }

                System.out.println("True");
            }
        };
        return a;
    }

}

