interface Material {

	Vector getBSDF(Sampler sampler, Scene scene, Ray ray, int depth, int E, IntersectionResult intersection, Vector color);
}
