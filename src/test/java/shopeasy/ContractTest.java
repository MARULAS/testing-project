package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 3 – Design by Contract (Chapter 4)
 *
 * <p>Tests that verify:
 * <ol>
 *   <li>Contracts hold for valid inputs (positive tests).</li>
 *   <li>Contracts are violated (AssertionError) for invalid inputs (negative tests).</li>
 * </ol>
 *
 * <p>Run with assertions enabled: {@code -ea} is configured in Maven Surefire.
 */
class ContractTest {

    private ShoppingCart cart;
    private PriceCalculator calculator;
    private Product product;

    @BeforeEach
    void setUp() {
        cart       = new ShoppingCart();
        calculator = new PriceCalculator();
        product    = new Product("P001", "Widget", 10.0, 50);
    }

    // =====================================================================
    // ShoppingCart.addItem – pre-condition tests
    // =====================================================================

    /** Valid input: addItem with non-null product and positive quantity should not throw */
    @Test
    void addItem_validInput_shouldNotThrow() {
        assertThatCode(() -> cart.addItem(product, 1)).doesNotThrowAnyException();
    }

    /** Pre-condition violation: null product should trigger AssertionError */
    @Test
    void addItem_nullProduct_shouldViolatePreCondition() {
        assertThatThrownBy(() -> cart.addItem(null, 1))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition violation: zero quantity should trigger AssertionError */
    @Test
    void addItem_zeroQuantity_shouldViolatePreCondition() {
        assertThatThrownBy(() -> cart.addItem(product, 0))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition violation: negative quantity should trigger AssertionError */
    @Test
    void addItem_negativeQuantity_shouldViolatePreCondition() {
        assertThatThrownBy(() -> cart.addItem(product, -5))
                .isInstanceOf(AssertionError.class);
    }

    // =====================================================================
    // ShoppingCart.addItem – post-condition / invariant tests
    // =====================================================================

    /** Post-condition: after adding a new product, cart size increases by 1 */
    @Test
    void addItem_newProduct_cartSizeIncreases() {
        int before = cart.itemCount();
        cart.addItem(product, 2);
        assertThat(cart.itemCount()).isEqualTo(before + 1);
        assertThat(cart.total()).isEqualTo(20.0); // 10 * 2
    }

    /** Post-condition: adding existing product merges quantities, size stays same */
    @Test
    void addItem_existingProduct_mergesQuantity() {
        cart.addItem(product, 2);
        cart.addItem(product, 3);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(50.0); // 10 * 5
    }

    /** Invariant: total is always >= 0 after addItem */
    @Test
    void addItem_invariant_totalIsNonNegative() {
        cart.addItem(product, 5);
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);
    }

    // =====================================================================
    // ShoppingCart.applyDiscount – pre-condition tests
    // =====================================================================

    /** Valid input: discount rate of 0 should not throw */
    @Test
    void applyDiscount_zeroRate_shouldNotThrow() {
        cart.addItem(product, 1);
        assertThatCode(() -> cart.applyDiscount(0)).doesNotThrowAnyException();
    }

    /** Valid input: typical discount rate should not throw */
    @Test
    void applyDiscount_typicalRate_shouldNotThrow() {
        cart.addItem(product, 1);
        assertThatCode(() -> cart.applyDiscount(25)).doesNotThrowAnyException();
    }

    /** Valid input: full discount (100%) should not throw */
    @Test
    void applyDiscount_fullRate_shouldNotThrow() {
        cart.addItem(product, 1);
        assertThatCode(() -> cart.applyDiscount(100)).doesNotThrowAnyException();
    }

    /** Pre-condition violation: negative discount rate should trigger AssertionError */
    @Test
    void applyDiscount_negativeRate_shouldViolatePreCondition() {
        cart.addItem(product, 1);
        assertThatThrownBy(() -> cart.applyDiscount(-10))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition violation: discount rate over 100 should trigger AssertionError */
    @Test
    void applyDiscount_overHundred_shouldViolatePreCondition() {
        cart.addItem(product, 1);
        assertThatThrownBy(() -> cart.applyDiscount(150))
                .isInstanceOf(AssertionError.class);
    }

    // =====================================================================
    // ShoppingCart.applyDiscount – post-condition tests
    // =====================================================================

    /** Post-condition: with positive discount and positive total, result < raw total */
    @Test
    void applyDiscount_positiveRate_resultLessThanTotal() {
        cart.addItem(product, 2); // total = 20
        double result = cart.applyDiscount(25);
        assertThat(result).isEqualTo(15.0); // 20 - 25%
    }

    /** Post-condition: 0% discount returns the raw total unchanged */
    @Test
    void applyDiscount_zeroRate_returnsRawTotal() {
        cart.addItem(product, 3); // total = 30
        double result = cart.applyDiscount(0);
        assertThat(result).isEqualTo(30.0);
    }

    /** Post-condition: 100% discount on empty cart returns 0 */
    @Test
    void applyDiscount_fullRateOnEmptyCart_returnsZero() {
        double result = cart.applyDiscount(100);
        assertThat(result).isEqualTo(0.0);
    }

    // =====================================================================
    // ShoppingCart invariant tests
    // =====================================================================

    /** Invariant: total >= 0 after any sequence of operations */
    @Test
    void invariant_totalNeverNegative_afterMixedOperations() {
        cart.addItem(product, 5);
        cart.removeItem("P001");
        assertThat(cart.total()).isGreaterThanOrEqualTo(0);
    }

    /** Invariant: total >= 0 after clear */
    @Test
    void invariant_totalNeverNegative_afterClear() {
        cart.addItem(product, 10);
        cart.clear();
        assertThat(cart.total()).isEqualTo(0.0);
    }

    // =====================================================================
    // PriceCalculator.calculate – pre-condition tests
    // =====================================================================

    /** Valid input: all parameters in valid range should not throw */
    @Test
    void calculate_validInputs_shouldNotThrow() {
        assertThatCode(() -> calculator.calculate(100, 10, 5))
                .doesNotThrowAnyException();
    }

    /** Pre-condition violation: negative base price should trigger AssertionError */
    @Test
    void calculate_negativeBasePrice_shouldViolatePreCondition() {
        assertThatThrownBy(() -> calculator.calculate(-10, 0, 0))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition violation: discount rate negative should trigger AssertionError */
    @Test
    void calculate_negativeDiscount_shouldViolatePreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100, -5, 0))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition violation: discount rate over 100 should trigger AssertionError */
    @Test
    void calculate_discountOverHundred_shouldViolatePreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100, 101, 0))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition violation: tax rate negative should trigger AssertionError */
    @Test
    void calculate_negativeTax_shouldViolatePreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100, 0, -1))
                .isInstanceOf(AssertionError.class);
    }

    /** Pre-condition violation: tax rate over 100 should trigger AssertionError */
    @Test
    void calculate_taxOverHundred_shouldViolatePreCondition() {
        assertThatThrownBy(() -> calculator.calculate(100, 0, 101))
                .isInstanceOf(AssertionError.class);
    }

    // =====================================================================
    // PriceCalculator.calculate – post-condition tests
    // =====================================================================

    /** Post-condition: result is always >= 0 for valid inputs */
    @Test
    void calculate_resultIsNonNegative() {
        double result = calculator.calculate(50, 50, 50);
        assertThat(result).isGreaterThanOrEqualTo(0);
    }

    /** Post-condition: zero base price gives zero result */
    @Test
    void calculate_zeroBasePrice_returnsZero() {
        double result = calculator.calculate(0, 50, 20);
        assertThat(result).isEqualTo(0.0);
    }

    /** Post-condition: full discount gives zero result regardless of tax */
    @Test
    void calculate_fullDiscount_returnsZero() {
        double result = calculator.calculate(100, 100, 50);
        assertThat(result).isEqualTo(0.0);
    }
}
