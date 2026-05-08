package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 1 – Specification-Based Testing (Chapter 2)
 *
 * Tests for PriceCalculator.calculate(basePrice, discountRate, taxRate)
 * using domain testing: equivalence partitioning + boundary value analysis.
 */
class PriceCalculatorSpecTest {

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // =====================================================================
    // Partition tests – basePrice dimension
    // =====================================================================

    /** Partition: zero base price — result must always be 0 regardless of rates */
    @Test
    void zeroBasePriceAlwaysReturnsZero() {
        assertThat(calculator.calculate(0, 20, 10)).isEqualTo(0.0);
        assertThat(calculator.calculate(0, 0, 0)).isEqualTo(0.0);
        assertThat(calculator.calculate(0, 100, 100)).isEqualTo(0.0);
    }

    /** Partition: positive base price with typical discount and tax */
    @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% => {3}")
    @CsvSource({
        "100.0, 10.0, 20.0, 108.0",    // typical case: 100 -> 90 -> 108
        "200.0, 25.0, 10.0, 165.0",    // bigger base, 25% discount
        "50.0, 50.0, 50.0, 37.5",      // half off then half tax
        "999.99, 0.0, 0.0, 999.99"     // no discount, no tax
    })
    void positiveBasePriceTypicalValues(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.001));
    }

    // =====================================================================
    // Boundary tests – discountRate dimension
    // =====================================================================

    /** Boundary (on-point): discountRate at lower bound (0%) — no reduction applied */
    @Test
    void discountRateAtLowerBoundZeroMeansNoDiscount() {
        double result = calculator.calculate(100, 0, 0);
        assertThat(result).isEqualTo(100.0);
    }

    /** Boundary (off-point): discountRate just above lower bound (small positive) */
    @Test
    void discountRateJustAboveZero() {
        double result = calculator.calculate(100, 0.01, 0);
        assertThat(result).isCloseTo(99.99, within(0.001));
    }

    /** Boundary (on-point): discountRate at upper bound (100%) — full discount wipes price */
    @Test
    void discountRateAtUpperBoundHundredMeansFullDiscount() {
        double result = calculator.calculate(100, 100, 0);
        assertThat(result).isEqualTo(0.0);
    }

    /** Boundary (off-point): discountRate just below upper bound (99.99%) */
    @Test
    void discountRateJustBelowHundred() {
        double result = calculator.calculate(100, 99.99, 0);
        assertThat(result).isCloseTo(0.01, within(0.001));
    }

    // =====================================================================
    // Boundary tests – taxRate dimension
    // =====================================================================

    /** Boundary (on-point): taxRate at lower bound (0%) — no tax added */
    @Test
    void taxRateAtLowerBoundZeroMeansNoTax() {
        double result = calculator.calculate(100, 0, 0);
        assertThat(result).isEqualTo(100.0);
    }

    /** Boundary (on-point): taxRate at upper bound (100%) — doubles the discounted price */
    @Test
    void taxRateAtUpperBoundHundredMeansDoublePrice() {
        double result = calculator.calculate(100, 0, 100);
        assertThat(result).isEqualTo(200.0);
    }

    /** Boundary: taxRate just above lower bound */
    @Test
    void taxRateJustAboveZero() {
        double result = calculator.calculate(100, 0, 0.01);
        assertThat(result).isCloseTo(100.01, within(0.001));
    }

    // =====================================================================
    // Combined boundary / edge cases
    // =====================================================================

    /** Boundary: maximum discount + maximum tax — should still be 0 (0 * anything = 0) */
    @Test
    void fullDiscountWithFullTaxStillZero() {
        double result = calculator.calculate(100, 100, 100);
        assertThat(result).isEqualTo(0.0);
    }

    /** Boundary: very large base price to check for overflow issues */
    @Test
    void veryLargeBasePrice() {
        double result = calculator.calculate(1_000_000, 10, 20);
        // 1M * 0.9 = 900k, * 1.2 = 1.08M
        assertThat(result).isCloseTo(1_080_000.0, within(0.001));
    }

    /** Partition: decimal precision — verify formula handles cents correctly */
    @Test
    void decimalPrecisionTest() {
        double result = calculator.calculate(19.99, 15, 8.5);
        // 19.99 * 0.85 = 16.9915, * 1.085 = 18.4357775
        assertThat(result).isCloseTo(18.4358, within(0.001));
    }

    /** Boundary: all zeros — should return exactly 0 */
    @Test
    void allZerosReturnsZero() {
        assertThat(calculator.calculate(0, 0, 0)).isEqualTo(0.0);
    }
}
