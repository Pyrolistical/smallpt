public class Specular implements Material {

	@Override
	public Vector getBSDF(final Sampler sampler, final Scene scene, final Ray r, final int depth, final IntersectionResult intersection, final Vector f) {
		final Sphere obj = intersection.object; // the hit object
		final Vector intersectionPoint = intersection.getIntersectionPoint();
		final Vector normal = intersection.getNormal();
		final Ray reflectionRay = getReflectionRay(intersectionPoint, r.direction, normal);
		return obj.emission.plus(f.multiply(sampler.radiance(scene, reflectionRay, depth)));
	}

	public Ray getReflectionRay(final Vector intersectionPoint, final Vector incident, final Vector normal) {
		return new Ray(intersectionPoint, getReflectionDirection(incident, normal));
	}

	public Vector getReflectionDirection(final Vector incident, final Vector normal) {
		final double cosI = normal.dot(incident);
		final Vector reflectedDirection = incident.minus(normal.scale(2 * cosI));
		return reflectedDirection;
	}

	@Override
	public boolean isOpaque() {
		return true;
	}

}
