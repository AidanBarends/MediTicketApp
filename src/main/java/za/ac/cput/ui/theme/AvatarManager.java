package za.ac.cput.ui.theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores profile photos on local disk, keyed by userId, entirely outside
 * the database — no backend endpoint, no entity field. Lives at
 * ~/.mediticket/avatars/{userId}.png. Persists across app restarts on
 * this machine only; does not sync across devices. Chosen deliberately
 * over a DB/blob-storage approach to avoid a schema migration + upload
 * endpoint for a purely cosmetic feature.
 */
public class AvatarManager {

    private static final Logger LOGGER = Logger.getLogger(AvatarManager.class.getName());
    private static final Path AVATAR_DIR = Path.of(System.getProperty("user.home"), ".mediticket", "avatars");

    private AvatarManager() {}

    public static boolean hasAvatar(int userId) {
        return Files.exists(pathFor(userId));
    }

    public static ImageIcon getCircularAvatar(int userId, int diameter) {
        Path path = pathFor(userId);
        if (!Files.exists(path)) {
            return ImageManager.getCircularAvatar(null, diameter);
        }
        try {
            BufferedImage raw = ImageIO.read(path.toFile());
            if (raw == null) return ImageManager.getCircularAvatar(null, diameter);

            int size = Math.min(raw.getWidth(), raw.getHeight());
            int x = (raw.getWidth() - size) / 2;
            int y = (raw.getHeight() - size) / 2;
            BufferedImage square = raw.getSubimage(x, y, size, size);
            Image scaled = square.getScaledInstance(diameter, diameter, Image.SCALE_SMOOTH);

            BufferedImage circular = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = circular.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, diameter, diameter));
            g2.drawImage(scaled, 0, 0, null);
            g2.dispose();
            return new ImageIcon(circular);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load avatar for user " + userId, e);
            return ImageManager.getCircularAvatar(null, diameter);
        }
    }

    /** Copies the chosen file into the avatar directory, overwriting any previous photo. */
    public static boolean saveAvatar(int userId, File sourceFile) {
        try {
            Files.createDirectories(AVATAR_DIR);
            BufferedImage image = ImageIO.read(sourceFile);
            if (image == null) return false; // not a readable image
            ImageIO.write(image, "png", pathFor(userId).toFile());
            return true;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to save avatar for user " + userId, e);
            return false;
        }
    }

    public static void deleteAvatar(int userId) {
        try {
            Files.deleteIfExists(pathFor(userId));
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to delete avatar for user " + userId, e);
        }
    }

    private static Path pathFor(int userId) {
        return AVATAR_DIR.resolve(userId + ".png");
    }
}