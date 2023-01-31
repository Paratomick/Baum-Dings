public class MatheTest {
    public static void main(String[] args) {
        double errorSum = 0;
        int sumCount = 0;

        for(int x = 1; x < 100; x++) {
            for(int y = 0; y < 100; y++) {
                double trueValue = Math.sqrt(x*x+y*y);
                double approxValue;
                if(x >= y) {
                    approxValue = 0.96 * x + 0.4 * y;
                } else {
                    approxValue = 0.96 * y + 0.4 * x;
                }
                errorSum += (trueValue / approxValue - 1);
                System.out.println((trueValue / approxValue - 1) + " = " + trueValue + " : " + approxValue);
                sumCount++;
            }
        }

        System.out.println("SumCount: " + sumCount);
        System.out.println("ErrorSum: " + errorSum);
        System.out.println(" = : " + (errorSum / sumCount)); //-0.011042527161826531
    }
}
