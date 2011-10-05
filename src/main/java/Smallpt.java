import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

// smallpt, a Path Tracer by Kevin Beason, 2008
class Smallpt {

	static Sphere spheres[] = {// Scene: radius, position, emission, color, material
			new Sphere(1e5, new Vector(-1e5 - 49, 0, 0), new Vector(0, 0, 0), new Vector(.75, .25, .25), Material.DIFFUSE),// Left
			new Sphere(1e5, new Vector(1e5 + 49, 0, 0), new Vector(0, 0, 0), new Vector(.25, .25, .75), Material.DIFFUSE),// Rght
			new Sphere(1e5, new Vector(0, 0, -1e5 - 81.6), new Vector(0, 0, 0), new Vector(.75, .75, .75), Material.DIFFUSE),// Back
			new Sphere(1e5, new Vector(0, 0, 1e5 + 88.4), new Vector(0, 0, 0), new Vector(0, 0, 0), Material.DIFFUSE),// Frnt
			new Sphere(1e5, new Vector(0, -1e5 - 40.8, 0), new Vector(0, 0, 0), new Vector(.75, .75, .75), Material.DIFFUSE),// Botm
			new Sphere(1e5, new Vector(0, 1e5 + 40.8, 0), new Vector(0, 0, 0), new Vector(.75, .75, .75), Material.DIFFUSE),// Top
			new Sphere(16.5, new Vector(-23, -24.3, -34.6), new Vector(0, 0, 0), new Vector(.999, .999, .999), Material.SPECULAR),// Mirr
			new Sphere(16.5, new Vector(23, -24.3, -3.6), new Vector(0, 0, 0), new Vector(.999, .999, .999), Material.REFRACTIVE),// Glas
			new Sphere(1.5, new Vector(0, 24.3, 0), new Vector(400, 400, 400), new Vector(0, 0, 0), Material.DIFFUSE)
	// Lite
	};

	static double clamp(final double x) {
		return x < 0 ? 0 : x > 1 ? 1 : x;
	}

	static double increaseBrightness(final double x) {
		return Math.pow(clamp(x), 1 / 2.2);
	}

	static int toInt(final double x) {
		return (int) (clamp(x) * 255 + .5);
	}

	static IntersectionResult intersect(final Ray ray) {
		IntersectionResult t = IntersectionResult.MISS;
		for (final Sphere s : spheres) {
			final IntersectionResult d = s.intersect(ray);
			if (d.isHit() && d.closerThan(t)) {
				t = d;
			}
		}
		return t;
	}

	public static final Random random = new Random(1337);

	static Vector radiance(final Ray r, final int depth) {
		return radiance(r, depth, 1);
	}

	static Vector radiance(final Ray r, int depth, final int E) {
		final IntersectionResult intersection = intersect(r);
		if (intersection.isMiss()) {
			return new Vector(0, 0, 0);
		} // if miss, return black
		final Sphere obj = intersection.object; // the hit object
		final Vector intersectionPoint = intersection.getIntersectionPoint();
		final Vector normal = intersection.getNormal();
		final Vector nl = normal.dot(r.direction) < 0 ? normal : normal.scale(-1);
		Vector f = obj.color;
		final double p = Math.max(f.x, Math.max(f.y, f.z));
		if (++depth > 5 || p == 0) {
			if (random.nextDouble() < p) {
				f = f.scale(1 / p);
			} else {
				return obj.emission.scale(E);
			}
		} // R.R.
		if (obj.material == Material.DIFFUSE) { // Ideal DIFFUSE reflection
			return diffuse(depth, E, intersection, obj, intersectionPoint, nl, f);
		} else if (obj.material == Material.SPECULAR) { // Ideal SPECULAR reflection
			return specular(r, depth, obj, intersectionPoint, normal, f);
		}
		return refraction(r, depth, obj, intersectionPoint, normal, nl, f);
	}

	private static Vector diffuse(final int depth, final int E, final IntersectionResult intersection, final Sphere obj, final Vector intersectionPoint, final Vector nl, final Vector f) {
		final Vector d = randomSampleDirection(nl);

		// Loop over any lights
		Vector e = new Vector(0, 0, 0);
		for (final Sphere s : spheres) {
			if (s.emission.x <= 0 && s.emission.y <= 0 && s.emission.z <= 0) {
				continue;
			} // skip non-lights

			final Vector sw = s.center.minus(intersectionPoint);
			final Vector su = (Math.abs(sw.x) > .1 ? new Vector(0, 1, 0) : new Vector(1, 0, 0)).cross(sw).norm();
			final Vector sv = sw.cross(su);
			final double cos_a_max = Math.sqrt(1 - s.radius * s.radius / intersectionPoint.minus(s.center).dot(intersectionPoint.minus(s.center)));
			final double eps1 = random.nextDouble();
			final double eps2 = random.nextDouble();
			final double cos_a = 1 - eps1 + eps1 * cos_a_max;
			final double sin_a = Math.sqrt(1 - cos_a * cos_a);
			final double phi = 2 * Math.PI * eps2;
			final Vector l = su.scale(Math.cos(phi) * sin_a).plus(sv.scale(Math.sin(phi) * sin_a)).plus(sw.scale(cos_a)).norm();
			final IntersectionResult shadowIntersection = intersect(new Ray(intersectionPoint, l));
			if (shadowIntersection.isHit() && shadowIntersection.object == s) { // shadow ray
				final double omega = 2 * Math.PI * (1 - cos_a_max);
				e = e.plus(f.multiply(s.emission.scale(l.dot(nl) * omega)).scale(1 / Math.PI));
			}
		}

		return obj.emission.scale(E).plus(e).plus(f.multiply(radiance(new Ray(intersectionPoint, d), depth, 0)));
	}

	private static Vector randomSampleDirection(final Vector nl) {
		final double r1 = 2 * Math.PI * random.nextDouble();
		final double r2 = random.nextDouble();
		final double r2s = Math.sqrt(r2);
		final Vector w = nl;
		final Vector u = ((Math.abs(w.x) > .1 ? new Vector(0, 1, 0) : new Vector(1, 0, 0)).cross(w)).norm();
		final Vector v = w.cross(u);
		final Vector d = u.scale(Math.cos(r1) * r2s).plus(v.scale(Math.sin(r1) * r2s)).plus(w.scale(Math.sqrt(1 - r2))).norm();
		return d;
	}

	private static Vector specular(final Ray r, final int depth, final Sphere obj, final Vector intersectionPoint, final Vector normal, final Vector f) {
		return obj.emission.plus(f.multiply(radiance(new Ray(intersectionPoint, r.direction.minus(normal.scale(2 * normal.dot(r.direction)))), depth)));
	}

	private static Vector refraction(final Ray r, final int depth, final Sphere obj, final Vector intersectionPoint, final Vector normal, final Vector nl, final Vector f) {
		final Ray reflRay = new Ray(intersectionPoint, r.direction.minus(normal.scale(2 * normal.dot(r.direction)))); // Ideal dielectric REFRACTION
		final double into = normal.dot(nl); // Ray from outside going in?
		final double nc = 1;
		final double nt = 1.5;
		final double nnt = Math.pow(nc / nt, Math.signum(into));
		final double ddn = r.direction.dot(nl);
		final double cos2t = 1 - nnt * nnt * (1 - ddn * ddn);
		if (cos2t < 0) {
			return obj.emission.plus(f.multiply(radiance(reflRay, depth)));
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
		final Vector xx = getXX(depth, intersectionPoint, reflRay, tdir, Re, Tr, P, RP, TP);
		return obj.emission.plus(f.multiply(xx));
	}

	private static Vector getXX(final int depth, final Vector x, final Ray reflRay, final Vector tdir, final double Re, final double Tr, final double P, final double RP, final double TP) {
		if (depth > 2) {
			if (random.nextDouble() < P) { // Russian roulette
				return radiance(reflRay, depth).scale(RP);
			} else {
				return radiance(new Ray(x, tdir), depth).scale(TP);
			}
		} else {
			return radiance(reflRay, depth).scale(Re).plus(radiance(new Ray(x, tdir), depth).scale(Tr));
		}
	}

	public static void main(final String[] argv) throws IOException {
		final long start = System.currentTimeMillis();
		final int w = argv.length == 3 ? Integer.valueOf(argv[0]) : 256;
		final int h = argv.length == 3 ? Integer.valueOf(argv[1]) : 256;
		final int samples = argv.length == 3 ? Integer.valueOf(argv[2]) : 6;
		System.err.println(String.format("Options %dx%d with %d samples", w, h, samples * samples));
		final Camera camera = new Camera(new Vector(0, 11.2, 214), new Vector(0, -0.042612, -1), (double) w / h, 4 * Math.PI / 25); // 28.8 degrees
		final Vector[][] image = renderImage(w, h, samples, camera);
		writeImage(w, h, image);
		final long end = System.currentTimeMillis();
		System.out.println(String.format("Finished in %dms", end - start));
	}

	private static Vector[][] renderImage(final int w, final int h, final int samples, final Camera camera) {
		final Vector[][] image = new Vector[h][];
		for (int y = 0; y < h; y++) { // Loop over image rows
			image[y] = new Vector[w];
			System.err.println(String.format("\rRendering (%d spp) %5.2f%%", samples * samples, 100. * y / (h - 1)));
			for (int x = 0; x < w; x++) {
				final List<Vector> radiances = samplePixel(w, h, samples, camera, y, x);
				final Vector radiance = combineRadiances(radiances);
				image[y][x] = radiance;
			}
		}
		return image;
	}

	private static List<Vector> samplePixel(final int w, final int h, final int samples, final Camera camera, final int y, final int x) {
		final List<Vector> radiances = new ArrayList<Vector>();
		for (int sy = 0; sy < samples; sy++) {
			final double dy = (double) sy / samples;
			for (int sx = 0; sx < samples; sx++) {
				final double dx = (double) sx / samples;
				final Ray sampleRay = camera.getSampleRay((dx + x) / w, (dy + y) / h);
				final Vector radiance = radiance(sampleRay, 0);
				radiances.add(new Vector(clamp(radiance.x), clamp(radiance.y), clamp(radiance.z)));
			}
		}
		return radiances;
	}

	private static Vector combineRadiances(final List<Vector> radiances) {
		Vector combinedradiance = new Vector(0, 0, 0);
		for (final Vector radiance : radiances) {
			combinedradiance = combinedradiance.plus(radiance);
		}
		combinedradiance = combinedradiance.scale(1d / radiances.size());
		return combinedradiance;
	}

	private static void writeImage(final int w, final int h, final Vector[][] c) throws IOException {
		final File f = new File("target/image.png"); // Write image to PPM file.

		final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < c.length; y++) {
			for (int x = 0; x < c[0].length; x++) {
				final int red = toInt(increaseBrightness(c[y][x].x));
				final int green = toInt(increaseBrightness(c[y][x].y));
				final int blue = toInt(increaseBrightness(c[y][x].z));
				final int rgb = new Color(red, green, blue).getRGB();
				image.setRGB(x, c.length - y - 1, rgb);
			}
		}

		ImageIO.write(image, "png", f);
	}
}
