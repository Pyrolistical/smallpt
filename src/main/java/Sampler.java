import java.util.Random;

public class Sampler {

	private final ThreadLocal<Random> random;

	public Sampler(final ThreadLocal<Random> random) {
		this.random = random;
	}

	public Vector radiance(final Scene scene, final Ray r, final int depth) {
		if (depth > 5) {
			return Vector.ZERO;
		}
		final IntersectionResult intersection = scene.intersect(r);
		if (intersection.isMiss()) {
			return Vector.ZERO;
		}
		return intersection.object.material.getBSDF(this, scene, r, depth + 1, intersection);
	}
}
