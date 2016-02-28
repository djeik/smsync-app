package me.jerrington.smsync.data;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ISO8601 {
    public static final SimpleDateFormat FORMAT =
            new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss.SSSZ",
                    Locale.US
            );
}