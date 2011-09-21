class Vector {
	final double x, y, z; // position, also color (r,g,b)

	Vector(final double x_, final double y_, final double z_) {
		x = x_;
		y = y_;
		z = z_;
	}

	Vector plus(final Vector b) {
		return new Vector(x + b.x, y + b.y, z + b.z);
	}

	Vector minus(final Vector b) {
		return new Vector(x - b.x, y - b.y, z - b.z);
	}

	Vector scale(final double b) {
		return new Vector(x * b, y * b, z * b);
	}

	Vector multiply(final Vector b) {
		return new Vector(x * b.x, y * b.y, z * b.z);
	}

	Vector norm() {
		return scale(1 / Math.sqrt(dot(this)));
	}

	double dot(final Vector b) {
		return x * b.x + y * b.y + z * b.z;
	}

	Vector cross(final Vector b) {
		return new Vector(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x);
	}
}
