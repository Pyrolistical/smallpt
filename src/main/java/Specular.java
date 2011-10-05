public class Specular implements Material {

	@Override
	public Vector getBSDF(final Sampler sampler, final Scene scene, final Ray r, final int depth, final int E, final IntersectionResult intersection, final Vector f) {
		final Sphere obj = intersection.object; // the hit object
		final Vector intersectionPoint = intersection.getIntersectionPoint();
		final Vector normal = intersection.getNormal();
		return obj.emission.plus(f.multiply(sampler.radiance(scene, new Ray(intersectionPoint, r.direction.minus(normal.scale(2 * normal.dot(r.direction)))), depth)));
	}

}
