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

	static double clamp(final double x) {
		return x < 0 ? 0 : x > 1 ? 1 : x;
	}

	static double increaseBrightness(final double x) {
		return Math.pow(clamp(x), 1 / 2.2);
	}

	static int toInt(final double x) {
		return (int) (clamp(x) * 255 + .5);
	}

	public static final Random random = new Random(1337);

	static Vector radiance(final Scene scene, final Ray r, final int depth) {
		return radiance(scene, r, depth, 1);
	}

	static Vector radiance(final Scene scene, final Ray r, int depth, final int E) {
		final IntersectionResult intersection = scene.intersect(r);
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
			return diffuse(scene, depth, E, intersection, obj, intersectionPoint, nl, f);
		} else if (obj.material == Material.SPECULAR) { // Ideal SPECULAR reflection
			return specular(scene, r, depth, obj, intersectionPoint, normal, f);
		}
		return refraction(scene, r, depth, obj, intersectionPoint, normal, nl, f);
	}

	private static Vector diffuse(final Scene scene, final int depth, final int E, final IntersectionResult intersection, final Sphere obj, final Vector intersectionPoint, final Vector nl, final Vector f) {
		final Vector d = randomSampleDirection(nl);

		// Loop over any lights
		Vector e = new Vector(0, 0, 0);
		for (final Sphere light : scene.lights) {
			final Vector lightDirection = light.center.minus(intersectionPoint).norm();
			final IntersectionResult lightIntersection = scene.intersect(new Ray(intersectionPoint, lightDirection));
			if (lightIntersection.isHit() && lightIntersection.object == light) {
				final double cos_a_max = Math.sqrt(1 - light.radius * light.radius / intersectionPoint.minus(light.center).dot(intersectionPoint.minus(light.center)));
				final double omega = 2 * Math.PI * (1 - cos_a_max);
				e = e.plus(f.multiply(light.emission.scale(lightDirection.dot(nl) * omega)).scale(1 / Math.PI));
			}
		}

		return obj.emission.scale(E).plus(e).plus(f.multiply(radiance(scene, new Ray(intersectionPoint, d), depth, 0)));
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

	private static Vector specular(final Scene scene, final Ray r, final int depth, final Sphere obj, final Vector intersectionPoint, final Vector normal, final Vector f) {
		return obj.emission.plus(f.multiply(radiance(scene, new Ray(intersectionPoint, r.direction.minus(normal.scale(2 * normal.dot(r.direction)))), depth)));
	}

	private static Vector refraction(final Scene scene, final Ray r, final int depth, final Sphere obj, final Vector intersectionPoint, final Vector normal, final Vector nl, final Vector f) {
		final Ray reflRay = new Ray(intersectionPoint, r.direction.minus(normal.scale(2 * normal.dot(r.direction)))); // Ideal dielectric REFRACTION
		final double into = normal.dot(nl); // Ray from outside going in?
		final double nc = 1;
		final double nt = 1.5;
		final double nnt = Math.pow(nc / nt, Math.signum(into));
		final double ddn = r.direction.dot(nl);
		final double cos2t = 1 - nnt * nnt * (1 - ddn * ddn);
		if (cos2t < 0) {
			return obj.emission.plus(f.multiply(radiance(scene, reflRay, depth)));
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
		final Vector xx = getXX(scene, depth, intersectionPoint, reflRay, tdir, Re, Tr, P, RP, TP);
		return obj.emission.plus(f.multiply(xx));
	}

	private static Vector getXX(final Scene scene, final int depth, final Vector x, final Ray reflRay, final Vector tdir, final double Re, final double Tr, final double P, final double RP, final double TP) {
		if (depth > 2) {
			if (random.nextDouble() < P) { // Russian roulette
				return radiance(scene, reflRay, depth).scale(RP);
			} else {
				return radiance(scene, new Ray(x, tdir), depth).scale(TP);
			}
		} else {
			return radiance(scene, reflRay, depth).scale(Re).plus(radiance(scene, new Ray(x, tdir), depth).scale(Tr));
		}
	}

	public static void main(final String[] argv) throws IOException {
		final long start = System.currentTimeMillis();
		final int w = argv.length == 3 ? Integer.valueOf(argv[0]) : 256;
		final int h = argv.length == 3 ? Integer.valueOf(argv[1]) : 256;
		final int samples = argv.length == 3 ? Integer.valueOf(argv[2]) : 6;
		System.err.println(String.format("Options %dx%d with %d samples", w, h, samples * samples));
		final Scene scene = new Scene();
		final Camera camera = new Camera(new Vector(0, 11.2, 214), new Vector(0, -0.042612, -1), (double) w / h, 4 * Math.PI / 25); // 28.8 degrees
		final Vector[][] image = renderImage(scene, w, h, samples, camera);
		writeImage(w, h, image);
		final long end = System.currentTimeMillis();
		System.out.println(String.format("Finished in %dms", end - start));
	}

	private static Vector[][] renderImage(final Scene scene, final int w, final int h, final int samples, final Camera camera) {
		final Vector[][] image = new Vector[h][];
		for (int y = 0; y < h; y++) { // Loop over image rows
			image[y] = new Vector[w];
			System.err.println(String.format("\rRendering (%d spp) %5.2f%%", samples * samples, 100. * y / (h - 1)));
			for (int x = 0; x < w; x++) {
				final List<Vector> radiances = samplePixel(scene, w, h, samples, camera, y, x);
				final Vector radiance = combineRadiances(radiances);
				image[y][x] = radiance;
			}
		}
		return image;
	}

	private static List<Vector> samplePixel(final Scene scene, final int w, final int h, final int samples, final Camera camera, final int y, final int x) {
		final List<Vector> radiances = new ArrayList<Vector>();
		for (int sy = 0; sy < samples; sy++) {
			final double dy = (double) sy / samples;
			for (int sx = 0; sx < samples; sx++) {
				final double dx = (double) sx / samples;
				final Ray sampleRay = camera.getSampleRay((dx + x) / w, (dy + y) / h);
				final Vector radiance = radiance(scene, sampleRay, 0);
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
