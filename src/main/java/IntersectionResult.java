class IntersectionResult {
	final Ray ray;
	final double t;
	final Sphere object;

	IntersectionResult(final Ray ray, final double t, final Sphere object) {
		this.ray = ray;
		this.t = t;
		this.object = object;
	}

	Vector getIntersectionPoint() {
		return ray.getVector(t);
	}
}
