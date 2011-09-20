class Vec {
	final double x, y, z; // position, also color (r,g,b)

	Vec(final double x_, final double y_, final double z_) {
		x = x_;
		y = y_;
		z = z_;
	}

	Vec plus(final Vec b) {
		return new Vec(x + b.x, y + b.y, z + b.z);
	}

	Vec minus(final Vec b) {
		return new Vec(x - b.x, y - b.y, z - b.z);
	}

	Vec scale(final double b) {
		return new Vec(x * b, y * b, z * b);
	}

	Vec multiply(final Vec b) {
		return new Vec(x * b.x, y * b.y, z * b.z);
	}

	Vec norm() {
		return scale(1 / Math.sqrt(dot(this)));
	}

	double dot(final Vec b) {
		return x * b.x + y * b.y + z * b.z;
	}

	Vec cross(final Vec b) {
		return new Vec(y * b.z - z * b.y, z * b.x - x * b.z, x * b.y - y * b.x);
	}
}
