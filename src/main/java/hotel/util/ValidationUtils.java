package hotel.util;

import java.util.regex.Pattern;

import org.apache.commons.validator.routines.EmailValidator;

public final class ValidationUtils {

    private static final Pattern PHONE_PATTERN_INSTANCE = Pattern.compile("^[+]?[0-9]{7,15}$");
    private static final EmailValidator EMAIL_VALIDATOR_INSTANCE = EmailValidator.getInstance();

    private ValidationUtils() {
    }

    public static Pattern getPhonePattern() {
        return PHONE_PATTERN_INSTANCE;
    }

    public static EmailValidator getEmailValidator() {
        return EMAIL_VALIDATOR_INSTANCE;
    }
}