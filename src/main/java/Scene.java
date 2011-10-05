import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Scene {

	final List<Sphere> lights = Arrays.asList(new Sphere(1.5, new Vector(0, 24.3, 0), new Vector(400, 400, 400), new Vector(0, 0, 0), Material.DIFFUSE));

	private final List<Sphere> spheres = new ArrayList<Sphere>(Arrays.asList(// Scene: radius, position, emission, color, material
			new Sphere(1e5, new Vector(-1e5 - 49, 0, 0), new Vector(0, 0, 0), new Vector(.75, .25, .25), Material.DIFFUSE),// Left
			new Sphere(1e5, new Vector(1e5 + 49, 0, 0), new Vector(0, 0, 0), new Vector(.25, .25, .75), Material.DIFFUSE),// Rght
			new Sphere(1e5, new Vector(0, 0, -1e5 - 81.6), new Vector(0, 0, 0), new Vector(.75, .75, .75), Material.DIFFUSE),// Back
			new Sphere(1e5, new Vector(0, 0, 1e5 + 88.4), new Vector(0, 0, 0), new Vector(0, 0, 0), Material.DIFFUSE),// Frnt
			new Sphere(1e5, new Vector(0, -1e5 - 40.8, 0), new Vector(0, 0, 0), new Vector(.75, .75, .75), Material.DIFFUSE),// Botm
			new Sphere(1e5, new Vector(0, 1e5 + 40.8, 0), new Vector(0, 0, 0), new Vector(.75, .75, .75), Material.DIFFUSE),// Top
			new Sphere(16.5, new Vector(-23, -24.3, -34.6), new Vector(0, 0, 0), new Vector(.999, .999, .999), Material.SPECULAR),// Mirr
			new Sphere(16.5, new Vector(23, -24.3, -3.6), new Vector(0, 0, 0), new Vector(.999, .999, .999), Material.REFRACTIVE)// Glas
			));

	public Scene() {
		spheres.addAll(lights);
	}

	IntersectionResult intersect(final Ray ray) {
		IntersectionResult t = IntersectionResult.MISS;
		for (final Sphere s : spheres) {
			final IntersectionResult d = s.intersect(ray);
			if (d.isHit() && d.closerThan(t)) {
				t = d;
			}
		}
		return t;
	}

}
