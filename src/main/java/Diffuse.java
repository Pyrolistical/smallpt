import java.util.Random;

class Diffuse implements Material {

	private final Random random;

	public Diffuse(final Random random) {
		this.random = random;
	}

	@Override
	public Vector getBSDF(final Sampler sampler, final Scene scene, final Ray ray, final int depth, final int E, final IntersectionResult intersection, final Vector f) {
		final Sphere obj = intersection.object; // the hit object
		final Vector intersectionPoint = intersection.getIntersectionPoint();
		final Vector normal = intersection.getNormal();
		final Vector d = sampleAroundNormal(normal);

		// Loop over any lights
		Vector e = new Vector(0, 0, 0);
		for (final Sphere light : scene.lights) {
			final Vector lightDirection = light.center.minus(intersectionPoint).norm();
			final IntersectionResult lightIntersection = scene.intersect(new Ray(intersectionPoint, lightDirection));
			if (lightIntersection.isHit() && lightIntersection.object == light) {
				final double cos_a_max = Math.sqrt(1 - light.radius * light.radius / intersectionPoint.minus(light.center).dot(intersectionPoint.minus(light.center)));
				final double omega = 2 * Math.PI * (1 - cos_a_max);
				e = e.plus(f.multiply(light.emission.scale(lightDirection.dot(normal) * omega)).scale(1 / Math.PI));
			}
		}

		return obj.emission.scale(E).plus(e).plus(f.multiply(sampler.radiance(scene, new Ray(intersectionPoint, d), depth, 0)));
	}

	Vector sampleAroundNormal(final Vector normal) {
		final Vector sampleCosineHemisphere = sampleCosineHemisphere();

		final Vector d = mapUnitZVector(sampleCosineHemisphere, normal);
		return d;
	}

	/**
	 * Applies the rotation required from Unit Z to destinations to source.
	 */
	private Vector mapUnitZVector(final Vector source, final Vector destination) {
		final Vector w = destination;
		final Vector u = ((Math.abs(w.x) > .1 ? new Vector(0, 1, 0) : new Vector(1, 0, 0)).cross(w)).norm();
		final Vector v = w.cross(u);
		final Vector d = u.scale(source.x).plus(v.scale(source.y)).plus(w.scale(source.z)).norm();
		return d;
	}

	private Vector sampleCosineHemisphere() {
		final double u2 = random.nextDouble();
		final double u1 = random.nextDouble();
		final double theta = 2 * Math.PI * u2;
		final double r = Math.sqrt(u1);
		final double x = r * Math.cos(theta);
		final double y = r * Math.sin(theta);
		final double z = Math.sqrt(1 - u1);
		final Vector d = new Vector(x, y, z).norm();
		return d;
	}

}
