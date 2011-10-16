public class Vector {

	public static final Vector ZERO = new Vector(0, 0, 0);

	final double x;
	final double y;
	final double z;

	public Vector(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector plus(final Vector b) {
		return new Vector(x + b.x, y + b.y, z + b.z);
	}

	public Vector minus(final Vector b) {
		return new Vector(x - b.x, y - b.y, z - b.z);
	}

	public Vector scale(final double b) {
		return new Vector(x * b, y * b, z * b);
	}

	public Vector multiply(final Vector b) {
		return new Vector(x * b.x, y * b.y, z * b.z);
	}

	public Vector norm() {
		return scale(1 / length());
	}

	public double length() {
		return Math.sqrt(squaredLength());
	}

	public double squaredLength() {
		return dot(this);
	}

	public double dot(final Vector b) {
		return x * b.x + y * b.y + z * b.z;
	}

	public Vector cross(final Vector b) {
		return new Vector(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x);
	}

	@Override
	public String toString() {
		return String.format("Vector[%f, %f, %f]", x, y, z);
	}
}
