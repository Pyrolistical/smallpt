public class Ray {
	final Vector origin;
	final Vector direction;

	public Ray(final Vector origin, final Vector direction) {
		this.origin = origin;
		this.direction = direction;
	}

	public Vector getVector(final double t) {
		return origin.plus(direction.scale(t));
	}

	@Override
	public String toString() {
		return String.format("Ray[%s, %s]", origin, direction);
	}
}
