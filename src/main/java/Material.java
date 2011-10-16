public interface Material {

	Vector getBSDF(Sampler sampler, Scene scene, Ray ray, int depth, IntersectionResult intersection);

	boolean isOpaque();
}
