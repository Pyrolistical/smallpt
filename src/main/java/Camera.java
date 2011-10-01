public class Camera {

	final Vector position;
	final Vector direction;

	public Camera(final Vector position, final Vector direction) {
		this.position = position;
		this.direction = direction.norm();
	}
}
