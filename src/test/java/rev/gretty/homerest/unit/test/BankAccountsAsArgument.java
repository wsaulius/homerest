package rev.gretty.homerest.unit.test;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class BankAccountsAsArgument implements ArgumentsProvider {

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
        return Stream.of(

                Arguments.of( "00228574981", true),
                Arguments.of( "00234094531", true),
                Arguments.of( "00905059421", true),
                Arguments.of( "04377596941", true),

                Arguments.of( "02496596217", true),
                Arguments.of( "09423042048", true),
                Arguments.of( "83219495059", true),
                Arguments.of( "32493248295", true),

                Arguments.of( "42934824939", true),
                Arguments.of( "33466789954", true),
                Arguments.of( "00239643455", true),
                Arguments.of( "75675655357", true),

                Arguments.of( "92434876567", true),
                Arguments.of( "34585678858", true),
                Arguments.of( "97959435439", true),
                Arguments.of( "69985948443", true));
    }

    // Iterative function to generate all permutations of a String in Java
    // using Collections
    public static Stream<? extends String > permutations(final String s)
    {
        // create an empty ArrayList to store (partial) permutations
        List<String> partial = new ArrayList<>();

        // initialize the list with the first character of the string
        partial.add(String.valueOf(s.charAt(0)));

        // do for every character of the specified string
        for (int i = 1; i < s.length(); i++)
        {
            // consider previously constructed partial permutation one by one

            // (iterate backwards to avoid ConcurrentModificationException)
            for (int j = partial.size() - 1; j >= 0 ; j--)
            {
                // remove current partial permutation from the ArrayList
                String str = partial.remove(j);

                // Insert next character of the specified string in all
                // possible positions of current partial permutation. Then
                // insert each of these newly constructed string in the list

                for (int k = 0; k <= str.length(); k++)
                {
                    // Advice: use StringBuilder for concatenation
                    partial.add(str.substring(0, k) + s.charAt(i) +
                            str.substring(k));
                }
            }
        }

        return partial.stream();
    }

}
