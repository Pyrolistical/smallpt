public class Camera {

	private final Vector position;
	private final Vector direction;
	private final Vector right;
	private final Vector up;
	private final double fov; // radians

	public Camera(final Vector position, final Vector direction, final double aspectRatio, final double fov) {
		this.position = position;
		this.direction = direction.norm();
		right = new Vector(1, 0, 0).scale(aspectRatio);
		up = new Vector(0, 1, 0);
		this.fov = fov;
	}

	private double getImagePlaneBoxSideLength() {
		// direction_length * 2 * tan(fov / 2) = side_length
		// where direction_length == 1
		return 2 * Math.tan(fov / 2);
	}

	private Vector getSampleDirection(final double x, final double y) {
		return right.scale(x).plus(up.scale(y)).minus(new Vector(.5, .5, 0)).scale(getImagePlaneBoxSideLength()).plus(direction).norm();
	}

	public Ray getSampleRay(final double x, final double y) {
		return new Ray(position, getSampleDirection(x, y));
	}

}
