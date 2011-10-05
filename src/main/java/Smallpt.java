import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

// smallpt, a Path Tracer by Kevin Beason, 2008
class Smallpt implements Sampler {

	static double clamp(final double x) {
		return x < 0 ? 0 : x > 1 ? 1 : x;
	}

	static double increaseBrightness(final double x) {
		return Math.pow(clamp(x), 1 / 2.2);
	}

	static int toInt(final double x) {
		return (int) (clamp(x) * 255 + .5);
	}

	public static final Random random = new Random(1337);

	@Override
	public Vector radiance(final Scene scene, final Ray r, final int depth) {
		return radiance(scene, r, depth, 1);
	}

	@Override
	public Vector radiance(final Scene scene, final Ray r, int depth, final int E) {
		final IntersectionResult intersection = scene.intersect(r);
		if (intersection.isMiss()) {
			return new Vector(0, 0, 0);
		} // if miss, return black
		final Sphere obj = intersection.object; // the hit object
		Vector f = obj.color;
		final double p = Math.max(f.x, Math.max(f.y, f.z));
		if (++depth > 5 || p == 0) {
			if (random.nextDouble() < p) {
				f = f.scale(1 / p);
			} else {
				return obj.emission.scale(E);
			}
		} // R.R.
		return obj.material.getBSDF(this, scene, r, depth, E, intersection, f);
	}

	public static void main(final String[] argv) throws IOException {
		final long start = System.currentTimeMillis();
		final int w = argv.length == 3 ? Integer.valueOf(argv[0]) : 256;
		final int h = argv.length == 3 ? Integer.valueOf(argv[1]) : 256;
		final int samples = argv.length == 3 ? Integer.valueOf(argv[2]) : 6;
		System.err.println(String.format("Options %dx%d with %d samples", w, h, samples * samples));
		final Scene scene = new Scene(random);
		final Camera camera = new Camera(new Vector(0, 11.2, 214), new Vector(0, -0.042612, -1), (double) w / h, 4 * Math.PI / 25); // 28.8 degrees
		final Sampler sampler = new Smallpt();
		final ImageRenderer imageRenderer = new ImageRenderer();
		final Vector[][] image = imageRenderer.renderImage(sampler, scene, w, h, samples, camera);
		writeImage(w, h, image);
		final long end = System.currentTimeMillis();
		System.out.println(String.format("Finished in %dms", end - start));
	}

	private static void writeImage(final int w, final int h, final Vector[][] c) throws IOException {
		final File f = new File("target/image.png"); // Write image to PPM file.

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

		ImageIO.write(image, "png", f);
	}
}
