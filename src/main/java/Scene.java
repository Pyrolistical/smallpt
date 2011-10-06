import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Scene {

	private final Material DIFFUSE;
	private final Material SPECULAR;
	private final Material REFRACTIVE;

	final List<Sphere> lights = new ArrayList<Sphere>();

	private final List<Sphere> spheres = new ArrayList<Sphere>();

	public Scene(final Random random) {
		DIFFUSE = new Diffuse(random);
		SPECULAR = new Specular();
		REFRACTIVE = new Refractive(random);
		lights.add(new Sphere(1.5, new Vector(0, 24.3, 0), new Vector(400, 400, 400), new Vector(0, 0, 0), DIFFUSE));
		spheres.addAll(Arrays.asList(// Scene: radius, position, emission, color, material
				new Sphere(1e5, new Vector(-1e5 - 49, 0, 0), new Vector(0, 0, 0), new Vector(.75, .25, .25), DIFFUSE),// Left
				new Sphere(1e5, new Vector(1e5 + 49, 0, 0), new Vector(0, 0, 0), new Vector(.25, .25, .75), DIFFUSE),// Rght
				new Sphere(1e5, new Vector(0, 0, -1e5 - 81.6), new Vector(0, 0, 0), new Vector(.75, .75, .75), DIFFUSE),// Back
				new Sphere(1e5, new Vector(0, 0, 1e5 + 88.4), new Vector(0, 0, 0), new Vector(0, 0, 0), DIFFUSE),// Frnt
				new Sphere(1e5, new Vector(0, -1e5 - 40.8, 0), new Vector(0, 0, 0), new Vector(.75, .75, .75), DIFFUSE),// Botm
				new Sphere(1e5, new Vector(0, 1e5 + 40.8, 0), new Vector(0, 0, 0), new Vector(.75, .75, .75), DIFFUSE),// Top
				new Sphere(16.5, new Vector(-23, -24.3, -34.6), new Vector(0, 0, 0), new Vector(.999, .999, .999), SPECULAR),// Mirr
				new Sphere(16.5, new Vector(23, -24.3, -3.6), new Vector(0, 0, 0), new Vector(.999, .999, .999), REFRACTIVE)// Glas
				));
		spheres.addAll(lights);
	}

	public IntersectionResult intersect(final Ray ray) {
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
