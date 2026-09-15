// Copyright 2026 AstroImageJ contributors.  Licensed under the same terms as AIJ.
package ij;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Minimal i18n helper for AstroImageJ (available to all modules: ij, Astronomy_, ...).
 *
 * <p>Selects the resource bundle locale by the system property {@code aij.lang}
 * (e.g. {@code -Daij.lang=zh_CN}).  If the property is missing or the requested
 * bundle cannot be loaded, untranslated strings fall back gracefully to the
 * original English text (the key itself).</p>
 *
 * <p>Typical use:</p>
 * <pre>
 *     gd.addCheckbox(I18n.t("multiap.use_radec"), useWCS, ...);
 *     IJ.showMessage(I18n.t("error.no_image_open"));
 *     IJ.log(I18n.t("status.found_n_stars", n));
 * </pre>
 */
public final class I18n {

    /** Property key on the JVM command line. */
    public static final String LANG_PROP = "aij.lang";

    private static final ResourceBundle BUNDLE = loadBundle();

    private I18n() {}

    public static Locale getLocale() {
        return BUNDLE != null ? BUNDLE.getLocale() : Locale.getDefault();
    }

    public static String t(String key) {
        if (BUNDLE == null) return key;
        try {
            return BUNDLE.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public static String t(String key, Object... args) {
        String pattern = t(key);
        if (args == null || args.length == 0) return pattern;
        try {
            return MessageFormat.format(pattern, args);
        } catch (IllegalArgumentException e) {
            return pattern;
        }
    }

    private static ResourceBundle loadBundle() {
        Locale locale = parseLocale(System.getProperty(LANG_PROP));
        ClassLoader[] loaders = new ClassLoader[] {
                I18n.class.getClassLoader(),
                Thread.currentThread().getContextClassLoader(),
                ClassLoader.getSystemClassLoader()
        };
        for (ClassLoader cl : loaders) {
            if (cl == null) continue;
            try {
                ResourceBundle b = ResourceBundle.getBundle("messages", locale, cl);
                logSafe("I18n: loaded messages bundle via " + cl.getClass().getName()
                        + " for locale " + b.getLocale());
                return b;
            } catch (MissingResourceException ignore) { /* try next */ }
        }
        // Fallback: read the .properties stream directly
        try {
            String name = "messages_" + locale + ".properties";
            java.io.InputStream raw = I18n.class.getClassLoader().getResourceAsStream(name);
            if (raw == null) {
                name = "messages.properties";
                raw = I18n.class.getClassLoader().getResourceAsStream(name);
            }
            if (raw != null) {
                try (java.io.InputStream in = raw) {
                    ResourceBundle b = new java.util.PropertyResourceBundle(in);
                    logSafe("I18n: loaded " + name + " via direct getResourceAsStream");
                    return b;
                }
            }
        } catch (Exception e) {
            logSafe("I18n: fallback stream load failed: " + e);
        }
        logSafe("I18n: NO bundle could be loaded for locale " + locale
                + "; UI strings will be returned as-is.");
        return null;
    }

    private static Locale parseLocale(String tag) {
        if (tag == null || tag.isBlank()) return Locale.ENGLISH;
        String[] parts = tag.replace('-', '_').split("_", 3);
        return switch (parts.length) {
            case 1 -> new Locale(parts[0]);
            case 2 -> new Locale(parts[0], parts[1]);
            default -> new Locale(parts[0], parts[1], parts[2]);
        };
    }

    private static void logSafe(String msg) {
        try {
            IJ.log(msg);
        } catch (Throwable t) {
            System.err.println(msg);
        }
    }
}
