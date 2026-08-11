package za.ac.cput.ui.theme;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads and registers the app's custom fonts (Inter, Playfair Display)
 * from the classpath, then hands out sized/styled Font instances.
 *
 * Inter is loaded as Regular + Italic variable-font instances; bold Inter
 * is synthetic (Font.deriveFont(BOLD, ...)) since only those two weight
 * files are bundled — acceptable at typical UI sizes (14–16px).
 *
 * Playfair Display has a true Bold cut bundled alongside Regular/Italic,
 * so headline bold requests resolve to a real bold font, not a synthetic
 * transform — important since headlines render much larger than body text
 * and synthetic bolding looks noticeably mushy at that scale.
 */
public class FontManager {

    private static final Logger LOGGER = Logger.getLogger(FontManager.class.getName());

    private static final String FONTS_PATH = "/fonts/";

    private static final String INTER_REGULAR    = "Inter-VariableFont_opsz,wght.ttf";
    private static final String INTER_ITALIC     = "Inter-Italic-VariableFont_opsz,wght.ttf";
    private static final String PLAYFAIR_REGULAR = "PlayfairDisplay-VariableFont_wght.ttf";
    private static final String PLAYFAIR_ITALIC  = "PlayfairDisplay-Italic-VariableFont_wght.ttf";
    private static final String PLAYFAIR_BOLD    = "PlayfairDisplay-Bold.ttf";

    // Logical family names we derive everything from after registration
    public static final String FAMILY_INTER    = "Inter";
    public static final String FAMILY_PLAYFAIR = "Playfair Display";

    private static final Map<String, Font> BASE_FONTS = new HashMap<>();
    private static boolean registered = false;

    /**
     * Loads all bundled TTFs into the JVM's graphics environment and caches
     * the base Font instances. Must be called once, before AppTheme applies
     * any UIManager font defaults.
     */
    public static void registerFonts() {
        if (registered) return;

        registerFont(INTER_REGULAR, FAMILY_INTER + "-Regular");
        registerFont(INTER_ITALIC, FAMILY_INTER + "-Italic");
        registerFont(PLAYFAIR_REGULAR, FAMILY_PLAYFAIR + "-Regular");
        registerFont(PLAYFAIR_ITALIC, FAMILY_PLAYFAIR + "-Italic");
        registerFont(PLAYFAIR_BOLD, FAMILY_PLAYFAIR + "-Bold");

        registered = true;
    }

    private static void registerFont(String fileName, String cacheKey) {
        try (InputStream is = FontManager.class.getResourceAsStream(FONTS_PATH + fileName)) {
            if (is == null) {
                LOGGER.log(Level.WARNING, "Font resource not found on classpath: {0}{1}", new Object[]{FONTS_PATH, fileName});
                return;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            BASE_FONTS.put(cacheKey, font);
        } catch (FontFormatException | IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load font: " + fileName, e);
        }
    }

    // ── Body text — Inter ────────────────────────────────────────
    // Used for all standard UI: labels, buttons, fields, tables, menus.
    // Bold is synthetic (deriveFont) — fine at UI text sizes.

    public static Font bodyFont(int style, float size) {
        Font base = pickInterBase(style);
        return base.deriveFont(style & Font.BOLD, size);
    }

    public static Font bodyFont(float size) {
        return bodyFont(Font.PLAIN, size);
    }

    // ── Headline / display text — Playfair Display ─────────────
    // Reserved for hero sections, landing page headings, dashboard
    // welcome banners. Bold resolves to the true Bold TTF, not a
    // synthetic transform.

    public static Font headlineFont(int style, float size) {
        boolean italic = (style & Font.ITALIC) != 0;
        boolean bold = (style & Font.BOLD) != 0;

        if (bold && !italic) {
            // True bold cut — no synthetic transform needed
            Font boldBase = fallbackIfMissing(FAMILY_PLAYFAIR + "-Bold", Font.SERIF);
            return boldBase.deriveFont(Font.PLAIN, size);
        }

        if (bold) {
            // Bold + Italic requested but only Bold (upright) and Italic
            // (regular-weight) cuts are bundled — no bold-italic file.
            // Fall back to synthetically bolding the italic cut rather
            // than silently dropping the italic.
            Font italicBase = fallbackIfMissing(FAMILY_PLAYFAIR + "-Italic", Font.SERIF);
            return italicBase.deriveFont(Font.BOLD, size);
        }

        Font base = pickPlayfairBase(style);
        return base.deriveFont(Font.PLAIN, size);
    }

    public static Font headlineFont(float size) {
        return headlineFont(Font.PLAIN, size);
    }

    // ── Resolution helpers ───────────────────────────────────────

    private static Font pickInterBase(int style) {
        boolean italic = (style & Font.ITALIC) != 0;
        String key = italic ? FAMILY_INTER + "-Italic" : FAMILY_INTER + "-Regular";
        return fallbackIfMissing(key, Font.SANS_SERIF);
    }

    private static Font pickPlayfairBase(int style) {
        boolean italic = (style & Font.ITALIC) != 0;
        String key = italic ? FAMILY_PLAYFAIR + "-Italic" : FAMILY_PLAYFAIR + "-Regular";
        return fallbackIfMissing(key, Font.SERIF);
    }

    private static Font fallbackIfMissing(String cacheKey, String logicalFallback) {
        Font base = BASE_FONTS.get(cacheKey);
        if (base != null) return base;
        LOGGER.log(Level.FINE, "Using system fallback for missing font: {0}", cacheKey);
        return new Font(logicalFallback, Font.PLAIN, 12);
    }
}