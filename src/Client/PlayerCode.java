package Client;

import java.util.Random;

public class PlayerCode {

    static String generate() {
        final char leftLimit = '0';
        final char rightLimit = 'z';
        final int CODE_LENGTH = 12;

        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= '9' || (i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z')))
                .limit(CODE_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

}
