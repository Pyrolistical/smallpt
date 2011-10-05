import java.util.Random;

public class Refractive implements Material {

	private final Random random;

	public Refractive(final Random random) {
		this.random = random;
	}

	@Override
	public Vector getBSDF(final Sampler sampler, final Scene scene, final Ray r, final int depth, final int E, final IntersectionResult intersection, final Sphere obj, final Vector intersectionPoint, final Vector normal, final Vector f) {
		final Vector nl = normal.dot(r.direction) < 0 ? normal : normal.scale(-1);
		final Ray reflRay = new Ray(intersectionPoint, r.direction.minus(normal.scale(2 * normal.dot(r.direction)))); // Ideal dielectric REFRACTION
		final double into = normal.dot(nl); // Ray from outside going in?
		final double nc = 1;
		final double nt = 1.5;
		final double nnt = Math.pow(nc / nt, Math.signum(into));
		final double ddn = r.direction.dot(nl);
		final double cos2t = 1 - nnt * nnt * (1 - ddn * ddn);
		if (cos2t < 0) {
			return obj.emission.plus(f.multiply(sampler.radiance(scene, reflRay, depth)));
		}
		final Vector tdir = r.direction.scale(nnt).minus(normal.scale((Math.signum(into) * (ddn * nnt + Math.sqrt(cos2t))))).norm();
		final double a = nt - nc;
		final double b = nt + nc;
		final double R0 = a * a / (b * b);
		final double c = 1 - (into > 0 ? -ddn : tdir.dot(normal));
		final double Re = R0 + (1 - R0) * Math.pow(c, 5);
		final double Tr = 1 - Re;
		final double P = .25 + .5 * Re;
		final double RP = Re / P;
		final double TP = Tr / (1 - P);
		final Vector xx = getXX(sampler, scene, depth, intersectionPoint, reflRay, tdir, Re, Tr, P, RP, TP);
		return obj.emission.plus(f.multiply(xx));
	}

	private Vector getXX(final Sampler sampler, final Scene scene, final int depth, final Vector x, final Ray reflRay, final Vector tdir, final double Re, final double Tr, final double P, final double RP, final double TP) {
		if (depth > 2) {
			if (random.nextDouble() < P) { // Russian roulette
				return sampler.radiance(scene, reflRay, depth).scale(RP);
			} else {
				return sampler.radiance(scene, new Ray(x, tdir), depth).scale(TP);
			}
		} else {
			return sampler.radiance(scene, reflRay, depth).scale(Re).plus(sampler.radiance(scene, new Ray(x, tdir), depth).scale(Tr));
		}
	}
}
