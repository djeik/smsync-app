package me.jerrington.smsync;

/**
 * Class of exceptions that arise when the build method of a Builder with insufficient information
 * is called.
 */
public class IncompleteObjectException extends RuntimeException {
    final Class<?> targetClass;
    final String[] missingFields;

    public IncompleteObjectException(final Class<?> targetClass, final String missingField) {
        this.missingFields = new String[]{missingField};
        this.targetClass = targetClass;
    }

    public IncompleteObjectException(final Class<?> targetClass, final String[] missingFields) {
        this.missingFields = missingFields;
        this.targetClass = targetClass;
    }
}
