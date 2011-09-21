import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

// smallpt, a Path Tracer by Kevin Beason, 2008
class Smallpt {

	static Sphere spheres[] = {// Scene: radius, position, emission, color, material
			new Sphere(1e5, new Vec(1e5 + 1, 40.8, 81.6), new Vec(0, 0, 0), new Vec(.75, .25, .25), Refl_t.DIFF),// Left
			new Sphere(1e5, new Vec(-1e5 + 99, 40.8, 81.6), new Vec(0, 0, 0), new Vec(.25, .25, .75), Refl_t.DIFF),// Rght
			new Sphere(1e5, new Vec(50, 40.8, 1e5), new Vec(0, 0, 0), new Vec(.75, .75, .75), Refl_t.DIFF),// Back
			new Sphere(1e5, new Vec(50, 40.8, -1e5 + 170), new Vec(0, 0, 0), new Vec(0, 0, 0), Refl_t.DIFF),// Frnt
			new Sphere(1e5, new Vec(50, 1e5, 81.6), new Vec(0, 0, 0), new Vec(.75, .75, .75), Refl_t.DIFF),// Botm
			new Sphere(1e5, new Vec(50, -1e5 + 81.6, 81.6), new Vec(0, 0, 0), new Vec(.75, .75, .75), Refl_t.DIFF),// Top
			new Sphere(16.5, new Vec(27, 16.5, 47), new Vec(0, 0, 0), new Vec(1, 1, 1).scale(.999), Refl_t.SPEC),// Mirr
			new Sphere(16.5, new Vec(73, 16.5, 78), new Vec(0, 0, 0), new Vec(1, 1, 1).scale(.999), Refl_t.REFR),// Glas
			new Sphere(1.5, new Vec(50, 81.6 - 16.5, 81.6), new Vec(400, 400, 400), new Vec(0, 0, 0), Refl_t.DIFF)
	// Lite
	};

	static double clamp(final double x) {
		return x < 0 ? 0 : x > 1 ? 1 : x;
	}

	static int toInt(final double x) {
		return (int) (Math.pow(clamp(x), 1 / 2.2) * 255 + .5);
	}

	static void intersect(final Ray r, final Intersection intersection) {
		final double inf = intersection.t = 1e20;
		for (int i = spheres.length - 1; i >= 0; i--) {
			final double d = spheres[i].intersect(r);
			if (d > 0 && d < intersection.t) {
				intersection.t = d;
				intersection.id = i;
			}
		}
		intersection.b = intersection.t < inf;
	}

	public static final Random random = new Random(1337);

	static Vec radiance(final Ray r, final int depth, final int Xi) {
		return radiance(r, depth, Xi, 1);
	}

	static Vec radiance(final Ray r, int depth, final int Xi, final int E) {
		final Intersection intersection = new Intersection();
		intersect(r, intersection);
		if (!intersection.b) {
			return new Vec(0, 0, 0);
		} // if miss, return black
		final Sphere obj = spheres[intersection.id]; // the hit object
		final Vec x = r.o.plus(r.d.scale(intersection.t));
		final Vec n = (x.minus(obj.p)).norm();
		final Vec nl = n.dot(r.d) < 0 ? n : n.scale(-1);
		Vec f = obj.c;
		final double p = f.x > f.y && f.x > f.z ? f.x : f.y > f.z ? f.y : f.z; // max refl
		if (++depth > 5 || p == 0) {
			if (random.nextDouble() < p) {
				f = f.scale(1 / p);
			} else {
				return obj.e.scale(E);
			}
		} // R.R.
		if (obj.refl == Refl_t.DIFF) { // Ideal DIFFUSE reflection
			final double r1 = 2 * Math.PI * random.nextDouble();
			final double r2 = random.nextDouble();
			final double r2s = Math.sqrt(r2);
			final Vec w = nl;
			final Vec u = ((Math.abs(w.x) > .1 ? new Vec(0, 1, 0) : new Vec(1, 0, 0)).cross(w)).norm();
			final Vec v = w.cross(u);
			final Vec d = (u.scale(Math.cos(r1) * r2s).plus(v.scale(Math.sin(r1) * r2s)).plus(w.scale(Math.sqrt(1 - r2)))).norm();

			// Loop over any lights
			Vec e = new Vec(0, 0, 0);
			for (int i = 0; i < spheres.length; i++) {
				final Sphere s = spheres[i];
				if (s.e.x <= 0 && s.e.y <= 0 && s.e.z <= 0) {
					continue;
				} // skip non-lights

				final Vec sw = s.p.minus(x);
				final Vec su = ((Math.abs(sw.x) > .1 ? new Vec(0, 1, 0) : new Vec(1, 0, 0)).cross(sw)).norm();
				final Vec sv = sw.cross(su);
				final double cos_a_max = Math.sqrt(1 - s.rad * s.rad / (x.minus(s.p)).dot(x.minus(s.p)));
				final double eps1 = random.nextDouble();
				final double eps2 = random.nextDouble();
				final double cos_a = 1 - eps1 + eps1 * cos_a_max;
				final double sin_a = Math.sqrt(1 - cos_a * cos_a);
				final double phi = 2 * Math.PI * eps2;
				final Vec l = su.scale(Math.cos(phi) * sin_a).plus(sv.scale(Math.sin(phi) * sin_a)).plus(sw.scale(cos_a)).norm();
				intersect(new Ray(x, l), intersection);
				if (intersection.b && intersection.id == i) { // shadow ray
					final double omega = 2 * Math.PI * (1 - cos_a_max);
					e = e.plus(f.multiply(s.e.scale(l.dot(nl) * omega)).scale(1 / Math.PI));
				}
			}

			return obj.e.scale(E).plus(e).plus(f.multiply(radiance(new Ray(x, d), depth, Xi, 0)));
		} else if (obj.refl == Refl_t.SPEC) { // Ideal SPECULAR reflection
			return obj.e.plus(f.multiply(radiance(new Ray(x, r.d.minus(n.scale(2 * n.dot(r.d)))), depth, Xi)));
		}
		final Ray reflRay = new Ray(x, r.d.minus(n.scale(2 * n.dot(r.d)))); // Ideal dielectric REFRACTION
		final boolean into = n.dot(nl) > 0; // Ray from outside going in?
		final double nc = 1;
		final double nt = 1.5;
		final double nnt = into ? nc / nt : nt / nc;
		final double ddn = r.d.dot(nl);
		final double cos2t = 1 - nnt * nnt * (1 - ddn * ddn);
		if (cos2t < 0) {
			return obj.e.plus(f.multiply(radiance(reflRay, depth, Xi)));
		}
		final Vec tdir = (r.d.scale(nnt).minus(n.scale(((into ? 1 : -1) * (ddn * nnt + Math.sqrt(cos2t)))))).norm();
		final double a = nt - nc;
		final double b = nt + nc;
		final double R0 = a * a / (b * b);
		final double c = 1 - (into ? -ddn : tdir.dot(n));
		final double Re = R0 + (1 - R0) * c * c * c * c * c;
		final double Tr = 1 - Re;
		final double P = .25 + .5 * Re;
		final double RP = Re / P;
		final double TP = Tr / (1 - P);
		final Vec xx = getXX(depth, Xi, x, reflRay, tdir, Re, Tr, P, RP, TP);
		return obj.e.plus(f.multiply(xx));
	}

	private static Vec getXX(final int depth, final int Xi, final Vec x, final Ray reflRay, final Vec tdir, final double Re, final double Tr, final double P, final double RP, final double TP) {
		if (depth > 2) {
			if (random.nextDouble() < P) { // Russian roulette
				return radiance(reflRay, depth, Xi).scale(RP);
			} else {
				return radiance(new Ray(x, tdir), depth, Xi).scale(TP);
			}
		} else {
			return radiance(reflRay, depth, Xi).scale(Re).plus(radiance(new Ray(x, tdir), depth, Xi).scale(Tr));
		}
	}

	public static void main(final String[] argv) throws IOException {
		final int w = argv.length == 3 ? Integer.valueOf(argv[0]) : 256; // # samples
		final int h = argv.length == 3 ? Integer.valueOf(argv[1]) : 256; // # samples
		final int samps = argv.length == 3 ? Integer.valueOf(argv[2]) / 4 : 1; // # samples
		System.err.println(String.format("Options %dx%d with %d samples", w, h, samps));
		final Ray cam = new Ray(new Vec(50, 52, 295.6), new Vec(0, -0.042612, -1).norm()); // cam pos, dir
		final Vec cx = new Vec(w * .5135 / h, 0, 0);
		final Vec cy = (cx.cross(cam.d)).norm().scale(.5135);
		final Vec[][] c = new Vec[h][];
		final int Xi = 0;
		// final omp parallel for schedule(dynamic, 1) private(r) // OpenMP
		for (int y = 0; y < h; y++) { // Loop over image rows
			System.err.println(String.format("\rRendering (%d spp) %5.2f%%", samps * 4, 100. * y / (h - 1)));
			for (int x = 0; x < w; x++) {
				for (int sy = 0, i = (h - y - 1) * w + x; sy < 2; sy++) {
					Vec r = new Vec(0, 0, 0);
					for (int sx = 0; sx < 2; sx++, r = new Vec(0, 0, 0)) { // 2x2 subpixel cols
						for (int s = 0; s < samps; s++) {
							final double r1 = 2 * random.nextDouble();
							final double dx = r1 < 1 ? Math.sqrt(r1) - 1 : 1 - Math.sqrt(2 - r1);
							final double r2 = 2 * random.nextDouble();
							final double dy = r2 < 1 ? Math.sqrt(r2) - 1 : 1 - Math.sqrt(2 - r2);
							final Vec d = cx.scale((((sx + .5 + dx) / 2 + x) / w - .5)).plus(cy.scale((((sy + .5 + dy) / 2 + y) / h - .5))).plus(cam.d);
							r = r.plus(radiance(new Ray(cam.o.plus(d.scale(140)), d.norm()), 0, Xi).scale((1. / samps)));
						} // Camera rays are pushed ^^^^^ forward to start in interior
						if (c[y] == null) {
							c[y] = new Vec[w];
						}
						if (c[y][x] == null) {
							c[y][x] = new Vec(0, 0, 0);
						}
						c[y][x] = c[y][x].plus(new Vec(clamp(r.x), clamp(r.y), clamp(r.z)).scale(.25));
					}
				}
			}
		}
		final File f = new File("target/image.png"); // Write image to PPM file.

		final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < c.length; y++) {
			for (int x = 0; x < c[0].length; x++) {
				final int a[] = new int[3];
				a[0] = toInt(c[y][x].x);
				a[1] = toInt(c[y][x].y);
				a[2] = toInt(c[y][x].z);
				image.setRGB(x, c.length - y - 1, new Color(a[0], a[1], a[2]).getRGB());
			}
		}

		ImageIO.write(image, "png", f);

	}
}
