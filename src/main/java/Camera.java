public class Camera {

	final Vector position;
	final Vector direction;
	final Vector right;
	final Vector up;
	final double fov; // radians

	public Camera(final Vector position, final Vector direction, final double aspectRatio, final double fov) {
		this.position = position;
		this.direction = direction.norm();
		right = new Vector(1, 0, 0).scale(aspectRatio);
		up = new Vector(0, 1, 0);
		this.fov = fov;
	}

	double getImagePlaneBoxSideLength() {
		// direction_length * 2 * tan(fov / 2) = side_length
		// direction_length == 1
		return 2 * Math.tan(fov / 2);
	}

	Vector getSampleDirection(final double x, final double y) {
		return right.scale(x).plus(up.scale(y)).minus(new Vector(.5, .5, 0)).scale(getImagePlaneBoxSideLength()).plus(direction);
	}
}
