import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageWriter {

	private static double clamp(final double x) {
		return Math.min(Math.max(0, x), 1);
	}

	private static double increaseBrightness(final double x) {
		return Math.pow(clamp(x), 1 / 2.2);
	}

	private static int toInt(final double x) {
		return (int) (clamp(x) * 255 + .5);
	}

	public static void writeImage(final int w, final int h, final Vector[][] c, final File destination) throws IOException {
		final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < c.length; y++) {
			for (int x = 0; x < c[0].length; x++) {
				final int red = toInt(increaseBrightness(c[y][x].x));
				final int green = toInt(increaseBrightness(c[y][x].y));
				final int blue = toInt(increaseBrightness(c[y][x].z));
				final int rgb = new Color(red, green, blue).getRGB();
				image.setRGB(x, c.length - y - 1, rgb);
			}
		}
		ImageIO.write(image, "png", destination);
	}
}
