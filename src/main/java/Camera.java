public class Camera {

	final Vector position;
	final Vector direction;
	final Vector right;
	final Vector up;

	public Camera(final Vector position, final Vector direction, final double aspectRatio) {
		this.position = position;
		this.direction = direction.norm();
		right = new Vector(1, 0, 0).scale(aspectRatio);
		up = new Vector(0, 1, 0);
	}
}
