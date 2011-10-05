public class Specular implements Material {

	@Override
	public Vector getBSDF(final Sampler sampler, final Scene scene, final Ray r, final int depth, final int E, final IntersectionResult intersection, final Sphere obj, final Vector intersectionPoint, final Vector normal, final Vector f) {
		return obj.emission.plus(f.multiply(sampler.radiance(scene, new Ray(intersectionPoint, r.direction.minus(normal.scale(2 * normal.dot(r.direction)))), depth)));
	}

}
