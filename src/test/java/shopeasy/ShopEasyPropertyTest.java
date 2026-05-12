package shopeasy;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 4 – Property-Based Testing (Chapter 5)
 *
 * <p>Target classes: {@link PriceCalculator}, {@link ShoppingCart}
 *
 * <p>Using jqwik to define properties that should hold for ALL valid inputs,
 * not just specific examples.
 */
class ShopEasyPropertyTest {

    // =====================================================================
    // PriceCalculator properties
    // =====================================================================

    /**
     * Property: The final price is always non-negative.
     *
     * <p>What it means: No matter what valid base price, discount, and tax we use,
     * the calculator should never return a negative number.
     *
     * <p>Bug class caught: Any implementation path that produces a negative result
     * (e.g., arithmetic overflow, incorrect formula, or invalid parameter handling).
     */
    @Property
    void finalPriceIsNeverNegative(
            @ForAll @DoubleRange(min = 0, max = 10_000) double base,
            @ForAll @DoubleRange(min = 0, max = 100)   double discount,
            @ForAll @DoubleRange(min = 0, max = 100)   double tax) {

        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(base, discount, tax);
        assertThat(result).isGreaterThanOrEqualTo(0.0);
    }

    /**
     * Property: Monotonicity — for fixed base and tax, increasing the discount
     * rate never increases the final price.
     *
     * <p>What it means: If discount A > discount B, then price with A <= price with B.
     * More discount should mean less (or equal) price.
     *
     * <p>Bug class caught: A formula that adds discount instead of subtracting it,
     * or any non-monotonic behavior in the discount application.
     */
    @Property
    void higherDiscountNeverIncreasesPrice(
            @ForAll @DoubleRange(min = 0, max = 10_000) double base,
            @ForAll @DoubleRange(min = 0, max = 100)   double tax,
            @ForAll @DoubleRange(min = 0, max = 100)   double discountA,
            @ForAll @DoubleRange(min = 0, max = 100)   double discountB) {

        PriceCalculator calc = new PriceCalculator();
        double priceA = calc.calculate(base, discountA, tax);
        double priceB = calc.calculate(base, discountB, tax);

        if (discountA > discountB) {
            assertThat(priceA).isLessThanOrEqualTo(priceB);
        } else if (discountA < discountB) {
            assertThat(priceA).isGreaterThanOrEqualTo(priceB);
        } else {
            assertThat(priceA).isEqualTo(priceB);
        }
    }

    /**
     * Property: Identity — 0% discount and 0% tax returns exactly the base price.
     *
     * <p>What it means: Applying no discount and no tax should be a no-op.
     *
     * <p>Bug class caught: Any extra fees, rounding errors, or formula mistakes
     * that change the price even when no adjustments are applied.
     */
    @Property
    void zeroDiscountAndZeroTaxReturnsBasePrice(
            @ForAll @DoubleRange(min = 0, max = 10_000) double base) {

        PriceCalculator calc = new PriceCalculator();
        double result = calc.calculate(base, 0, 0);
        assertThat(result).isEqualTo(base);
    }

    // =====================================================================
    // ShoppingCart properties
    // =====================================================================

    /**
     * Property: Cart commutativity — adding product A then B yields the same total
     * as adding B then A.
     *
     * <p>What it means: The order in which items are added to the cart should not
     * affect the final total price.
     *
     * <p>Bug class caught: Any stateful bug where the order of operations matters
     * (e.g., incorrect merging logic, side effects during addItem).
     */
    @Property
    void addingItemsIsCommutativeForTotal(
            @ForAll @DoubleRange(min = 0.01, max = 500) double priceA,
            @ForAll @DoubleRange(min = 0.01, max = 500) double priceB,
            @ForAll @IntRange(min = 1, max = 50) int qtyA,
            @ForAll @IntRange(min = 1, max = 50) int qtyB) {

        Product productA = new Product("PA", "Product A", priceA, 100);
        Product productB = new Product("PB", "Product B", priceB, 100);

        ShoppingCart cart1 = new ShoppingCart();
        cart1.addItem(productA, qtyA);
        cart1.addItem(productB, qtyB);

        ShoppingCart cart2 = new ShoppingCart();
        cart2.addItem(productB, qtyB);
        cart2.addItem(productA, qtyA);

        assertThat(cart1.total()).isCloseTo(cart2.total(), within(0.001));
        assertThat(cart1.itemCount()).isEqualTo(cart2.itemCount());
    }

    /**
     * Property: Adding then removing a product returns total to 0.
     *
     * <p>What it means: If you add an item and then immediately remove it,
     * the cart should be back to its empty state.
     *
     * <p>Bug class caught: Memory leaks, incorrect removal logic, or state
     * that persists after removal.
     */
    @Property
    void addThenRemoveReturnsEmptyCart(
            @ForAll("validProducts") Product product,
            @ForAll @IntRange(min = 1, max = 100) int quantity) {

        ShoppingCart cart = new ShoppingCart();
        cart.addItem(product, quantity);
        cart.removeItem(product.getId());

        assertThat(cart.total()).isEqualTo(0.0);
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    /**
     * Property: Total is always >= 0 regardless of cart operations.
     *
     * <p>What it means: The cart total should never become negative,
     * even after arbitrary sequences of add/remove/update operations.
     *
     * <p>Bug class caught: Arithmetic underflow, incorrect quantity handling,
     * or negative price bugs.
     */
    @Property
    void cartTotalNeverNegative(
            @ForAll("validProducts") Product product,
            @ForAll @IntRange(min = 1, max = 50) int qty1,
            @ForAll @IntRange(min = 1, max = 50) int qty2) {

        ShoppingCart cart = new ShoppingCart();
        cart.addItem(product, qty1);
        cart.addItem(product, qty2); // merges
        cart.removeItem(product.getId());

        assertThat(cart.total()).isGreaterThanOrEqualTo(0.0);
    }

    // =====================================================================
    // Custom providers
    // =====================================================================

    /**
     * Provides valid Product instances for property tests.
     * Uses alpha-numeric IDs and positive prices to ensure valid products.
     */
    @Provide
    Arbitrary<Product> validProducts() {
        return Combinators.combine(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(5),
                Arbitraries.doubles().between(0.01, 500.0)
        ).as((name, price) -> new Product("P-" + name, name, price, 100));
    }
}
