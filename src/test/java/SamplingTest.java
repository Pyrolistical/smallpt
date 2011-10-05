import java.util.Random;

import org.apache.commons.collections.SortedBag;
import org.apache.commons.collections.bag.TreeBag;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class SamplingTest {

	@Test
	public void testSampleAroundNormal() {
		final int bins = 20;
		final SortedBag xhistogram = new TreeBag();
		final SortedBag yhistogram = new TreeBag();
		final SortedBag zhistogram = new TreeBag();
		final Diffuse diffuse = new Diffuse(new Random(1337));
		for (int i = 0; i < 500; i++) {
			final Vector sample = diffuse.sampleAroundNormal(new Vector(-1, 0, 0));
			xhistogram.add((int) Math.floor(sample.x * bins / 2));
			yhistogram.add((int) Math.floor(sample.y * bins / 2));
			zhistogram.add((int) Math.floor(sample.z * bins / 2));
		}
		printHistogram(bins, xhistogram);
		printHistogram(bins, yhistogram);
		printHistogram(bins, zhistogram);
	}

	private void printHistogram(final int bins, final SortedBag histogram) {
		for (int i = -bins / 2; i < bins / 2; i++) {
			System.out.println(2d * i / bins + ": " + StringUtils.repeat("*", histogram.getCount(i)));
		}
		System.out.println();
	}

}
