class Ray {
	final Vector origin;
	final Vector direction;

	Ray(final Vector o_, final Vector d_) {
		origin = o_;
		direction = d_;
	}

	Vector getVector(final double t) {
		return origin.plus(direction.scale(t));
	}

	@Override
	public String toString() {
		return String.format("Ray[%s, %s]", origin, direction);
	}
}
