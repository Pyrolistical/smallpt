import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ImageRenderer {

	private static double clamp(final double x) {
		return Math.min(Math.max(0, x), 1);
	}

	private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

	public Vector[][] renderImage(final ThreadLocal<Random> random, final Sampler sampler, final Scene scene, final int w, final int h, final int samples, final Camera camera) throws Exception {
		final List<Future<Vector[]>> rows = new ArrayList<Future<Vector[]>>();
		for (int y = 0; y < h; y++) {
			rows.add(executor.submit(createRowJob(random, sampler, scene, w, h, samples, camera, y)));
		}
		executor.shutdown();
		final Vector[][] image = new Vector[h][];
		for (int y = 0; y < h; y++) {
			image[y] = rows.get(y).get();
		}
		return image;
	}

	private Callable<Vector[]> createRowJob(final ThreadLocal<Random> random, final Sampler sampler, final Scene scene, final int w, final int h, final int samples, final Camera camera, final int y) {
		return new Callable<Vector[]>() {
			@Override
			public Vector[] call() throws Exception {
				if (random.get() == null) {
					random.set(new Random(1337));
				}
				final Vector[] row = new Vector[w];
				System.err.println(String.format("\rRendering (%d spp) %5.2f%%", samples * samples, 100. * y / (h - 1)));
				for (int x = 0; x < w; x++) {
					final List<Vector> radiances = samplePixel(sampler, scene, w, h, samples, camera, x, y);
					final Vector radiance = combineRadiances(radiances);
					row[x] = radiance;
				}
				return row;
			}
		};
	}

	private static List<Vector> samplePixel(final Sampler sampler, final Scene scene, final int w, final int h, final int samples, final Camera camera, final int x, final int y) {
		final List<Vector> radiances = new ArrayList<Vector>();
		for (int sy = 0; sy < samples; sy++) {
			final double dy = (double) sy / samples;
			for (int sx = 0; sx < samples; sx++) {
				final double dx = (double) sx / samples;
				final Ray sampleRay = camera.getSampleRay((dx + x) / w, (dy + y) / h);
				final Vector radiance = sampler.radiance(scene, sampleRay, 0);
				radiances.add(new Vector(clamp(radiance.x), clamp(radiance.y), clamp(radiance.z)));
			}
		}
		return radiances;
	}

	private static Vector combineRadiances(final List<Vector> radiances) {
		Vector combinedradiance = new Vector(0, 0, 0);
		for (final Vector radiance : radiances) {
			combinedradiance = combinedradiance.plus(radiance);
		}
		combinedradiance = combinedradiance.scale(1d / radiances.size());
		return combinedradiance;
	}
}
