class Sphere {
	double rad; // radius
	Vec p, e, c; // position, emission, color
	Refl_t refl; // reflection type (DIFFuse, SPECular, REFRactive)

	Sphere(final double rad_, final Vec p_, final Vec e_, final Vec c_, final Refl_t refl_) {
		rad = rad_;
		p = p_;
		e = e_;
		c = c_;
		refl = refl_;
	}

	double intersect(final Ray r) { // returns distance, 0 if nohit
		final Vec op = p.minus(r.o); // Solve t^2*d.d + 2*t*(o-p).d + (o-p).(o-p)-R^2 = 0
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
