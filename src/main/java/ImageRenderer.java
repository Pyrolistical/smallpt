import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageRenderer {

	private static double clamp(final double x) {
		return Math.min(Math.max(0, x), 1);
	}

	public void renderImage(final Sampler sampler, final Scene scene, final int w, final int h, final int samples) throws Exception {
		final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				executor.submit(createRowJob(sampler, scene, w, h, samples, x, y));
			}
		}
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
	}

	private Runnable createRowJob(final Sampler sampler, final Scene scene, final int w, final int h, final int samples, final int x, final int y) {
		return new Runnable() {
			@Override
			public void run() {
				System.err.println(String.format("\rRendering (%d spp) %5.4f%%", samples * samples, 100. * (x + h * y) / (w * h)));
				samplePixel(sampler, scene, w, h, samples, x, y);
			}
		};
	}

	private static void samplePixel(final Sampler sampler, final Scene scene, final int w, final int h, final int samples, final int x, final int y) {
		final List<Vector> radiances = new ArrayList<Vector>(samples);
		for (int sy = 0; sy < samples; sy++) {
			final double dy = (double) sy / samples;
			for (int sx = 0; sx < samples; sx++) {
				final double dx = (double) sx / samples;
				final Ray sampleRay = scene.camera.getSampleRay((dx + x) / w, (dy + y) / h);
				final Vector radiance = sampler.radiance(scene, sampleRay, 0);
				radiances.add(new Vector(clamp(radiance.x), clamp(radiance.y), clamp(radiance.z)));
			}
		}
		scene.image[y][x] = combineRadiances(radiances);
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
