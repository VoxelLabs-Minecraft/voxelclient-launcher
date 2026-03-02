package de.voxelclient.launcher.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Lädt das VoxelClient Icon aus den Resources und setzt es am JFrame.
 *
 * ── Wo das Icon ablegen? ──────────────────────────────────────────────────────
 *
 *   src/main/resources/
 *   ├── icon.ico          ← für Launch4j (Windows EXE Icon)
 *   ├── icon_256.png      ← 256×256  (Taskbar, Alt+Tab)
 *   ├── icon_128.png      ← 128×128
 *   ├── icon_64.png       ← 64×64
 *   ├── icon_48.png       ← 48×48
 *   ├── icon_32.png       ← 32×32
 *   └── icon_16.png       ← 16×16   (Titelleiste)
 *
 * Je mehr Größen du anbietest, desto schärfer erscheint das Icon überall.
 * Tipp: Erstelle ein 512×512 PNG, dann skaliere es runter.
 *
 * ── Konverter ─────────────────────────────────────────────────────────────────
 *   PNG → ICO:  https://convertico.com/
 *   ICO Editor: https://www.gimp.org/ (Export as .ico)
 */
public final class IconLoader {

    // Alle Größen die wir laden wollen (Windows nutzt die passendste)
    private static final int[] SIZES = {256, 128, 16};

    private IconLoader() {}

    /**
     * Setzt das VoxelClient Icon an einem JFrame.
     * Lädt alle verfügbaren Größen für optimale Darstellung.
     *
     * Aufruf in VoxelLauncher.buildUI():
     *   IconLoader.applyTo(this);
     */
    public static void applyTo(JFrame frame) {
        List<Image> icons = loadAll();
        if (!icons.isEmpty()) {
            frame.setIconImages(icons);
        } else {
            // Fallback: generiertes Icon wenn keine Datei gefunden
            frame.setIconImage(generateFallbackIcon());
        }
    }

    /**
     * Lädt alle icon_NNN.png aus dem Resources-Ordner.
     */
    public static List<Image> loadAll() {
        List<Image> images = new ArrayList<>();
        for (int size : SIZES) {
            Image img = load("/icon_" + size + ".png");
            if (img != null) images.add(img);
        }
        // Versuche auch ein generisches "icon.png"
        if (images.isEmpty()) {
            Image img = load("/icon.png");
            if (img != null) images.add(img);
        }
        return images;
    }

    /**
     * Einzelnes Icon aus Resources laden.
     * Pfad relativ zu src/main/resources/
     */
    public static Image load(String resourcePath) {
        try (InputStream in = IconLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) return null;
            return ImageIO.read(in);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Erstellt ein programmatisches Fallback-Icon mit dem VoxelClient Logo-Style,
     * falls keine Icon-Dateien vorhanden sind.
     *
     * So sieht es aus: Dunkler Hintergrund + Cyan "V" Zeichen + Glow
     */
    public static BufferedImage generateFallbackIcon() {
        int size = 64;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Hintergrund
        g.setColor(new Color(0x08, 0x0b, 0x0f));
        g.fillRoundRect(0, 0, size, size, 12, 12);

        // Äußerer Rand (Cyan)
        g.setColor(new Color(0x00, 0xe5, 0xff, 180));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(2, 2, size-4, size-4, 10, 10);

        // Innerer Glow
        g.setColor(new Color(0x00, 0xe5, 0xff, 30));
        g.fillRoundRect(4, 4, size-8, size-8, 8, 8);

        // "V" Symbol
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        FontMetrics fm = g.getFontMetrics();
        String letter = "V";
        int textX = (size - fm.stringWidth(letter)) / 2;
        int textY = (size - fm.getHeight()) / 2 + fm.getAscent();

        // Glow-Effekt (mehrfach leicht versetzt zeichnen)
        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(0x00, 0xe5, 0xff, 20 * i));
            g.drawString(letter, textX - i, textY);
            g.drawString(letter, textX + i, textY);
            g.drawString(letter, textX, textY - i);
            g.drawString(letter, textX, textY + i);
        }

        // Haupt-Text
        g.setColor(new Color(0x00, 0xe5, 0xff));
        g.drawString(letter, textX, textY);

        g.dispose();
        return img;
    }

    /**
     * Hilfsmethode: Skaliert ein Bild auf die gewünschte Größe.
     * Nützlich wenn du nur ein einziges großes PNG hast.
     */
    public static Image scale(Image source, int targetSize) {
        return source.getScaledInstance(targetSize, targetSize, Image.SCALE_SMOOTH);
    }
}