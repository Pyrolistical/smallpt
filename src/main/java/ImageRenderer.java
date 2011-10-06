import java.util.ArrayList;
import java.util.List;

public class ImageRenderer {

	private static double clamp(final double x) {
		return Math.min(Math.max(0, x), 1);
	}

	public Vector[][] renderImage(final Sampler sampler, final Scene scene, final int w, final int h, final int samples, final Camera camera) {
		final Vector[][] image = new Vector[h][];
		for (int y = 0; y < h; y++) { // Loop over image rows
			image[y] = new Vector[w];
			System.err.println(String.format("\rRendering (%d spp) %5.2f%%", samples * samples, 100. * y / (h - 1)));
			for (int x = 0; x < w; x++) {
				final List<Vector> radiances = samplePixel(sampler, scene, w, h, samples, camera, x, y);
				final Vector radiance = combineRadiances(radiances);
				image[y][x] = radiance;
			}
		}
		return image;
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
