class Sphere {
	double rad; // radius
	Vector p, e, c; // position, emission, color
	Material refl; // reflection type (DIFFuse, SPECular, REFRactive)

	Sphere(final double rad_, final Vector p_, final Vector e_, final Vector c_, final Material refl_) {
		rad = rad_;
		p = p_;
		e = e_;
		c = c_;
		refl = refl_;
	}

	double intersect(final Ray r) { // returns distance, 0 if nohit
		final Vector op = p.minus(r.o); // Solve t^2*d.d + 2*t*(o-p).d + (o-p).(o-p)-R^2 = 0
		double t;
		final double eps = 1e-4, b = op.dot(r.d);
		double det = b * b - op.dot(op) + rad * rad;
		if (det < 0) {
			return 0;
		} else {
			det = Math.sqrt(det);
		}
		return (t = b - det) > eps ? t : ((t = b + det) > eps ? t : 0);
	}
};
