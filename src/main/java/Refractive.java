import java.util.Random;

public class Refractive implements Material {

	private final Specular specular = new Specular();

	private final Random random;

	public Refractive(final Random random) {
		this.random = random;
	}

	@Override
	public Vector getBSDF(final Sampler sampler, final Scene scene, final Ray r, final int depth, final IntersectionResult intersection, final Vector f) {
		final Sphere obj = intersection.object;
		final Vector intersectionPoint = intersection.getIntersectionPoint();
		final Vector normal = intersection.getNormal();
		final Vector incident = r.direction;
		final Vector nl = normal.dot(incident) < 0 ? normal : normal.scale(-1);
		final double into = normal.dot(nl); // Ray from outside going in?
		final double refractiveIndexAir = 1;
		final double refractiveIndexGlass = 1.5;
		final double refractiveIndexRatio = Math.pow(refractiveIndexAir / refractiveIndexGlass, Math.signum(into));
		final double cosI = incident.dot(nl);
		final double cos2t = 1 - refractiveIndexRatio * refractiveIndexRatio * (1 - cosI * cosI);
		if (cos2t < 0) {
			return specular.getBSDF(sampler, scene, r, depth, intersection, f);
		}
		final Vector refractedDirection = incident.scale(refractiveIndexRatio).minus(normal.scale((Math.signum(into) * (cosI * refractiveIndexRatio + Math.sqrt(cos2t))))).norm();
		final double a = refractiveIndexGlass - refractiveIndexAir;
		final double b = refractiveIndexGlass + refractiveIndexAir;
		final double R0 = a * a / (b * b);
		final double c = 1 - (into > 0 ? -cosI : refractedDirection.dot(normal));
		final double Re = R0 + (1 - R0) * Math.pow(c, 5);
		final Vector radiance = russianRouletteBSDF(sampler, scene, depth, intersectionPoint, incident, nl, refractedDirection, Re);
		return obj.emission.plus(f.multiply(radiance));
	}

	private Vector russianRouletteBSDF(final Sampler sampler, final Scene scene, final int depth, final Vector intersectionPoint, final Vector incident, final Vector normal, final Vector refractedDirection, final double Re) {
		final Ray reflectionRay = specular.getReflectionRay(intersectionPoint, incident, normal);
		final Ray refractionRay = new Ray(intersectionPoint, refractedDirection);
		final double Tr = 1 - Re;
		if (depth > 2) {
			final double P = .25 + .5 * Re;
			if (random.nextDouble() < P) { // Russian roulette
				final Vector recursiveReflectionRadiance = sampler.radiance(scene, reflectionRay, depth);
				final double RP = Re / P;
				return recursiveReflectionRadiance.scale(RP);
			} else {
				final Vector recursiveRefactionRadiance = sampler.radiance(scene, refractionRay, depth);
				final double TP = Tr / (1 - P);
				return recursiveRefactionRadiance.scale(TP);
			}
		} else {
			final Vector recursiveReflectionRadiance = sampler.radiance(scene, reflectionRay, depth);
			final Vector recursiveRefactionRadiance = sampler.radiance(scene, refractionRay, depth);
			return recursiveReflectionRadiance.scale(Re).plus(recursiveRefactionRadiance.scale(Tr));
		}
	}

	@Override
	public boolean isOpaque() {
		return false;
	}

}
