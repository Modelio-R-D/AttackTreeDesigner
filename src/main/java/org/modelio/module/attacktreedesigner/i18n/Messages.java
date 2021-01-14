package org.modelio.module.attacktreedesigner.i18n;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Messages {
    private static ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle ("org.modelio.module.attacktreedesigner.i18n.messages");

    private Messages() {
    }

    public static String getString(final String key) {
        try {
            return RESOURCE_BUNDLE.getString (key);
        } catch (@SuppressWarnings ("unused") MissingResourceException e) {
            return '!' + key + '!';
        }
    }

    public static String getString(final String key, final String... params) {
        try {
            return MessageFormat.format (RESOURCE_BUNDLE.getString (key),(Object[]) params);
        } catch (@SuppressWarnings ("unused") MissingResourceException e) {
            return '!' + key + '!';
        }
    }

}
