class IntersectionResult {
	final Ray ray;
	final double t;
	final int id;

	IntersectionResult(final Ray ray, final double t, final int id) {
		this.ray = ray;
		this.t = t;
		this.id = id;
	}

	Vector getIntersectionPoint() {
		return ray.getVector(t);
	}
}
