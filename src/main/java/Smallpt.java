import java.io.File;
import java.util.Random;

// smallpt, a Path Tracer originally written by Kevin Beason, 2008
// Ported to Java and refactored by Ronald Chen
public class Smallpt {

	public static void main(final String[] argv) throws Exception {
		final long start = System.currentTimeMillis();
		final int w = argv.length == 3 ? Integer.valueOf(argv[0]) : 256;
		final int h = argv.length == 3 ? Integer.valueOf(argv[1]) : 256;
		final int samples = argv.length == 3 ? Integer.valueOf(argv[2]) : 6;
		System.err.println(String.format("Options %dx%d with %d samples", w, h, samples * samples));
		final ThreadLocal<Random> random = new ThreadLocal<Random>() {
			@Override
			protected Random initialValue() {
				return new Random(1337);
			}
		};
		final Scene scene = new Scene(random);
		final Camera camera = new Camera(new Vector(0, 11.2, 214), new Vector(0, -0.042612, -1), (double) w / h, 4 * Math.PI / 25); // 28.8 degrees
		final Sampler sampler = new Sampler(random);
		final ImageRenderer imageRenderer = new ImageRenderer();
		final Vector[][] image = imageRenderer.renderImage(sampler, scene, w, h, samples, camera);
		final File f = new File("target/image.png");
		ImageWriter.writeImage(w, h, image, f);
		final long end = System.currentTimeMillis();
		System.out.println(String.format("Finished in %dms", end - start));
	}
}
