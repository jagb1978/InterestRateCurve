import com.company.exceptions.InterpolationException;
import com.company.interpolation.MonotoneConvex;
import org.junit.Test;

public class MonotoneConvexTest {
    private double[] values = new double[]{0.03, 0.03, 0.05, 0.047, 0.06, 0.06};
    private double[] terms = new double[]{0, 1, 2, 3, 4, 5};

    @Test
    public void test() throws InterpolationException {
        MonotoneConvex monotoneConvex = new MonotoneConvex(this.values, this.terms);
        double i = 0.0;
        while (i <= 6) {
            System.out.println("Term: " + String.format("%.2f", i) + " Rate: " + String.format("%.3f", monotoneConvex.getModeledRate(i) * 100)
                    + " forward: " + String.format("%.3f", monotoneConvex.getForwardRate(i) * 100));
            i += 0.01;
        }
    }
}
