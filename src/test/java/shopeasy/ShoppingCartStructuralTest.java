package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Task 2 – Structural Testing & Code Coverage (Chapter 3)
 *
 * <p>Target class: {@link ShoppingCart}
 *
 * <p>Goal: achieve >= 80% branch coverage on ShoppingCart.
 * Start with specification-based tests, then add tests targeting uncovered branches
 * identified via JaCoCo report.
 */
class ShoppingCartStructuralTest {

    private ShoppingCart cart;
    private Product apple;
    private Product banana;
    private Product orange;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        apple  = new Product("P001", "Apple",  1.50, 100);
        banana = new Product("P002", "Banana", 0.80, 50);
        orange = new Product("P003", "Orange", 2.00, 30);
    }

    // =====================================================================
    // Initial specification-based tests (happy path + basic branches)
    // =====================================================================

    /** Empty cart should have total 0 and item count 0 */
    @Test
    void emptyCart_hasZeroTotalAndZeroItems() {
        assertThat(cart.total()).isEqualTo(0.0);
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    /** addItem with new product increases item count */
    @Test
    void addItem_newProduct_increasesItemCount() {
        cart.addItem(apple, 3);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(4.50);
    }

    /** addItem with existing product merges quantity, keeps item count */
    @Test
    void addItem_existingProduct_mergesQuantity() {
        cart.addItem(apple, 2);
        cart.addItem(apple, 3);
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(7.50); // 1.50 * 5
    }

    /** addItem with different products increases item count for each */
    @Test
    void addItem_multipleProducts_tracksEachSeparately() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 5);
        assertThat(cart.itemCount()).isEqualTo(2);
        assertThat(cart.total()).isEqualTo(7.00); // 3.00 + 4.00
    }

    /** removeItem removes existing product */
    @Test
    void removeItem_existingProduct_removesIt() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 3);
        cart.removeItem("P001");
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(2.40);
    }

    /** removeItem with non-existent product does nothing (silent) */
    @Test
    void removeItem_nonExistentProduct_doesNothing() {
        cart.addItem(apple, 2);
        cart.removeItem("P999"); // doesn't exist
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(3.00);
    }

    /** updateQuantity changes quantity of existing product */
    @Test
    void updateQuantity_existingProduct_updatesQuantity() {
        cart.addItem(apple, 2);
        cart.updateQuantity("P001", 5);
        assertThat(cart.total()).isEqualTo(7.50);
    }

    /** updateQuantity with non-existent product throws IllegalArgumentException */
    @Test
    void updateQuantity_nonExistentProduct_throwsException() {
        cart.addItem(apple, 2);
        assertThatThrownBy(() -> cart.updateQuantity("P999", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    /** updateQuantity with invalid quantity throws IllegalArgumentException */
    @Test
    void updateQuantity_invalidQuantity_throwsException() {
        cart.addItem(apple, 2);
        assertThatThrownBy(() -> cart.updateQuantity("P001", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Quantity must be > 0");
    }

    /** applyDiscount with 0% returns raw total */
    @Test
    void applyDiscount_zeroPercent_returnsRawTotal() {
        cart.addItem(apple, 2); // total = 3.00
        double result = cart.applyDiscount(0);
        assertThat(result).isEqualTo(3.00);
    }

    /** applyDiscount with positive rate reduces total */
    @Test
    void applyDiscount_positiveRate_reducesTotal() {
        cart.addItem(apple, 2); // total = 3.00
        double result = cart.applyDiscount(50);
        assertThat(result).isEqualTo(1.50);
    }

    /** applyDiscount on empty cart returns 0 */
    @Test
    void applyDiscount_emptyCart_returnsZero() {
        double result = cart.applyDiscount(25);
        assertThat(result).isEqualTo(0.0);
    }

    /** getItems returns unmodifiable list */
    @Test
    void getItems_returnsUnmodifiableList() {
        cart.addItem(apple, 1);
        assertThatThrownBy(() -> cart.getItems().add(new CartItem(banana, 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    /** clear removes all items */
    @Test
    void clear_removesAllItems() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 3);
        cart.clear();
        assertThat(cart.itemCount()).isEqualTo(0);
        assertThat(cart.total()).isEqualTo(0.0);
    }

    // =====================================================================
    // Additional branch-targeting tests (to push coverage >= 80%)
    // =====================================================================

    /** Branch: addItem loop — product not found, falls through to add new */
    @Test
    void addItem_loopProductNotFound_addsNewItem() {
        cart.addItem(apple, 1);
        cart.addItem(banana, 1); // different ID, loop won't match apple
        assertThat(cart.itemCount()).isEqualTo(2);
    }

    /** Branch: addItem loop — product found early, returns without adding new */
    @Test
    void addItem_loopProductFoundEarly_mergesAndReturns() {
        cart.addItem(apple, 1);
        cart.addItem(banana, 1);
        cart.addItem(apple, 1); // found at index 0, returns immediately
        assertThat(cart.itemCount()).isEqualTo(2);
        assertThat(cart.total()).isEqualTo(3.80); // 3.00 + 0.80
    }

    /** Branch: total() with empty cart (sum starts at 0, loop doesn't execute) */
    @Test
    void total_emptyCart_loopNeverExecutes() {
        assertThat(cart.total()).isEqualTo(0.0);
    }

    /** Branch: total() with multiple items (loop executes multiple times) */
    @Test
    void total_multipleItems_loopExecutesMultipleTimes() {
        cart.addItem(apple, 1);   // 1.50
        cart.addItem(banana, 1);  // 0.80
        cart.addItem(orange, 1);  // 2.00
        assertThat(cart.total()).isEqualTo(4.30);
    }

    /** Branch: removeItem on empty cart (removeIf predicate never matches) */
    @Test
    void removeItem_emptyCart_nothingHappens() {
        cart.removeItem("P001");
        assertThat(cart.itemCount()).isEqualTo(0);
    }

    /** Branch: toString on empty cart */
    @Test
    void toString_emptyCart() {
        assertThat(cart.toString()).contains("items=0").contains("total=0.00");
    }

    /** Branch: toString on non-empty cart */
    @Test
    void toString_nonEmptyCart() {
        cart.addItem(apple, 2);
        assertThat(cart.toString()).contains("items=1").contains("total=3.00");
    }

    /** Edge case: applyDiscount with 100% on non-empty cart */
    @Test
    void applyDiscount_fullDiscountOnNonEmptyCart() {
        cart.addItem(apple, 10); // total = 15.00
        double result = cart.applyDiscount(100);
        assertThat(result).isEqualTo(0.0);
    }

    /** Edge case: multiple discounts are not compounded (each applies to raw total) */
    @Test
    void applyDiscount_notCompounded() {
        cart.addItem(apple, 10); // total = 15.00
        double first = cart.applyDiscount(50);  // 7.50
        double second = cart.applyDiscount(50); // still 7.50 (applies to raw total again)
        assertThat(second).isEqualTo(7.50);
    }

    /** Branch coverage: updateQuantity with quantity = 1 (minimum valid) */
    @Test
    void updateQuantity_minimumValidQuantity() {
        cart.addItem(apple, 5);
        cart.updateQuantity("P001", 1);
        assertThat(cart.total()).isEqualTo(1.50);
    }

    /** Mixed operations to exercise multiple branches in sequence */
    @Test
    void mixedOperations_exerciseMultipleBranches() {
        cart.addItem(apple, 2);
        cart.addItem(banana, 3);
        cart.addItem(apple, 1); // merge branch
        cart.removeItem("P002"); // remove existing
        cart.removeItem("P999"); // remove non-existing
        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.total()).isEqualTo(4.50); // 1.50 * 3
    }
}
