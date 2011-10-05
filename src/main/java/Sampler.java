public interface Sampler {

	Vector radiance(Scene scene, Ray r, int depth);

	Vector radiance(Scene scene, Ray ray, int depth, int E);

}
