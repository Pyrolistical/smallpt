public class Sphere {
	final double radius;
	final Vector center;
	final Vector emission;
	final Vector color;
	final Material material;

	public Sphere(final double radius, final Vector center, final Vector emission, final Vector color, final Material material) {
		this.radius = radius;
		this.center = center;
		this.emission = emission;
		this.color = color;
		this.material = material;
	}

	public IntersectionResult intersect(final Ray ray) {
		final Vector v = center.minus(ray.origin);
		if (material.isOpaque() && v.squaredLength() < radius * radius) {
			return IntersectionResult.MISS;
		}
		final double b = v.dot(ray.direction);
		final double discriminant = b * b - v.dot(v) + radius * radius;
		if (discriminant < 0) {
			return IntersectionResult.MISS;
		}
		final double d = Math.sqrt(discriminant);
		final double tfar = b + d;
		final double eps = 1e-4;
		if (tfar <= eps) {
			return IntersectionResult.MISS;
		}
		final double tnear = b - d;
		if (tnear <= eps) {
			return new IntersectionResult(ray, tfar, this);
		}
		return new IntersectionResult(ray, tnear, this);
	}

	public Vector getNormal(final Vector intersectionPoint) {
		return intersectionPoint.minus(center).norm();
	}
};
