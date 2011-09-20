import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

// smallpt, a Path Tracer by Kevin Beason, 2008
// Make : g++ -O3 -fopenmp smallpt.cpp -o smallpt
// Remove "-fopenmp" for g++ version < 4.2
// Usage: time ./smallpt 5000 && xv image.ppm
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
			new Sphere(600, new Vec(50, 681.6 - .27, 81.6), new Vec(12, 12, 12), new Vec(0, 0, 0), Refl_t.DIFF)
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

	static Vec radiance(final Ray r, int depth, final int Xi) {
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
		if (++depth > 5) {
			if (random.nextDouble() < p) {
				f = f.scale(1 / p);
			} else {
				return obj.e;
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
			return obj.e.plus(f.multiply(radiance(new Ray(x, d), depth, Xi)));
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
		final int w = 768;
		final int h = 768;
		final int samps = argv.length == 2 ? Integer.valueOf(argv[1]) / 4 : 1; // # samples
		final Ray cam = new Ray(new Vec(50, 52, 295.6), new Vec(0, -0.042612, -1).norm()); // cam pos, dir
		final Vec cx = new Vec(w * .5135 / h, 0, 0);
		final Vec cy = (cx.cross(cam.d)).norm().scale(.5135);
		Vec r = new Vec(0, 0, 0);
		final Vec[][] c = new Vec[h][];
		final int Xi = 0;
		// final omp parallel for schedule(dynamic, 1) private(r) // OpenMP
		for (int y = 0; y < h; y++) { // Loop over image rows
			System.err.println(String.format("\rRendering (%d spp) %5.2f%%", samps * 4, 100. * y / (h - 1)));
			for (int x = 0; x < w; x++) {
				for (int sy = 0, i = (h - y - 1) * w + x; sy < 2; sy++) {
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

		// final BufferedWriter bw = new BufferedWriter(new FileWriter(f));
		// try {
		// bw.write(String.format("P3\n%d %d\n%d\n", w, h, 255));
		// for (int i = 0; i < w * h; i++) {
		// bw.write(String.format("%d %d %d ", toInt(c[i].x), toInt(c[i].y), toInt(c[i].z)));
		// }
		// } finally {
		// bw.close();
		// }
	}
}
