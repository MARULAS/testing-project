package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Task 5 – Mocks & Stubs (Chapter 6)
 *
 * <p>Target class: {@link OrderProcessor}
 *
 * <p>Uses Mockito to mock {@link InventoryService} and {@link PaymentGateway},
 * then tests {@link OrderProcessor#process(String, ShoppingCart)} in isolation.
 */
@ExtendWith(MockitoExtension.class)
class OrderProcessorMockTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private ShoppingCart cart;
    private Product widget;
    private Product gadget;

    @BeforeEach
    void setUp() {
        cart   = new ShoppingCart();
        widget = new Product("P001", "Widget", 25.0, 100);
        gadget = new Product("P002", "Gadget", 50.0, 50);
    }

    // =====================================================================
    // Scenario 1: Happy path — inventory OK, payment succeeds
    // =====================================================================

    /**
     * Happy path: all items in stock, payment goes through.
     * Should return a non-null Order with correct customerId and total.
     */
    @Test
    void process_inventoryOkAndPaymentOk_returnsOrder() {
        cart.addItem(widget, 2); // total = 50.0

        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(paymentGateway.charge("customer-1", 50.0)).thenReturn(true);

        Order order = orderProcessor.process("customer-1", cart);

        assertThat(order).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-1");
        assertThat(order.getTotal()).isEqualTo(50.0);
        assertThat(order.getItems()).hasSize(1);

        // Verify that payment was actually attempted
        verify(paymentGateway).charge("customer-1", 50.0);
    }

    /**
     * Happy path with multiple items: verifies inventory is checked for ALL items.
     */
    @Test
    void process_multipleItems_inventoryCheckedForAll() {
        cart.addItem(widget, 1); // 25.0
        cart.addItem(gadget, 1); // 50.0, total = 75.0

        when(inventoryService.isAvailable(widget, 1)).thenReturn(true);
        when(inventoryService.isAvailable(gadget, 1)).thenReturn(true);
        when(paymentGateway.charge("customer-2", 75.0)).thenReturn(true);

        Order order = orderProcessor.process("customer-2", cart);

        assertThat(order).isNotNull();
        assertThat(order.getTotal()).isEqualTo(75.0);

        // Verify inventory was checked for both products
        verify(inventoryService).isAvailable(widget, 1);
        verify(inventoryService).isAvailable(gadget, 1);
        verify(paymentGateway).charge("customer-2", 75.0);
    }

    // =====================================================================
    // Scenario 2: Inventory failure — at least one item unavailable
    // =====================================================================

    /**
     * Inventory failure: one item is out of stock.
     * Should return null and NEVER attempt payment.
     */
    @Test
    void process_oneItemUnavailable_returnsNullAndNeverCharges() {
        cart.addItem(widget, 2);
        cart.addItem(gadget, 1);

        when(inventoryService.isAvailable(widget, 2)).thenReturn(true);
        when(inventoryService.isAvailable(gadget, 1)).thenReturn(false);

        Order order = orderProcessor.process("customer-3", cart);

        assertThat(order).isNull();

        // CRITICAL: payment must never be attempted when inventory fails
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    /**
     * Inventory failure: first item checked is already unavailable.
     * Should return null immediately without checking remaining items.
     */
    @Test
    void process_firstItemUnavailable_returnsNullImmediately() {
        cart.addItem(widget, 5);
        cart.addItem(gadget, 2);

        when(inventoryService.isAvailable(widget, 5)).thenReturn(false);

        Order order = orderProcessor.process("customer-4", cart);

        assertThat(order).isNull();
        // Payment should never be called
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    // =====================================================================
    // Scenario 3: Payment failure — inventory OK but charge declined
    // =====================================================================

    /**
     * Payment failure: inventory passes but payment gateway returns false.
     * Should return null.
     */
    @Test
    void process_inventoryOkButPaymentFails_returnsNull() {
        cart.addItem(widget, 4); // total = 100.0

        when(inventoryService.isAvailable(widget, 4)).thenReturn(true);
        when(paymentGateway.charge("customer-5", 100.0)).thenReturn(false);

        Order order = orderProcessor.process("customer-5", cart);

        assertThat(order).isNull();
        // Verify payment was attempted (it just failed)
        verify(paymentGateway).charge("customer-5", 100.0);
    }

    // =====================================================================
    // Scenario 4: Partial quantity — only some quantity available
    // =====================================================================

    /**
     * Partial quantity: customer wants 10 widgets but only 5 are in stock.
     * Since isAvailable(10) returns false, the order should be rejected.
     *
     * <p>Note: The current implementation treats partial availability as a full
     * failure — it does not support backorders or partial fulfillment.
     */
    @Test
    void process_partialQuantityAvailable_returnsNull() {
        cart.addItem(widget, 10); // wants 10

        // Only 5 available, so isAvailable(10) returns false
        when(inventoryService.isAvailable(widget, 10)).thenReturn(false);

        Order order = orderProcessor.process("customer-6", cart);

        assertThat(order).isNull();
        verify(paymentGateway, never()).charge(anyString(), anyDouble());
    }

    /**
     * Partial quantity edge case: exactly enough stock (boundary).
     * isAvailable returns true when quantity == available stock.
     */
    @Test
    void process_exactQuantityAvailable_orderSucceeds() {
        cart.addItem(widget, 100); // exactly all stock

        when(inventoryService.isAvailable(widget, 100)).thenReturn(true);
        when(paymentGateway.charge("customer-7", 2500.0)).thenReturn(true);

        Order order = orderProcessor.process("customer-7", cart);

        assertThat(order).isNotNull();
        assertThat(order.getTotal()).isEqualTo(2500.0);
    }

    // =====================================================================
    // Input validation tests (no mocks needed for these, but good to verify)
    // =====================================================================

    /** Null customerId should throw IllegalArgumentException */
    @Test
    void process_nullCustomerId_throwsException() {
        cart.addItem(widget, 1);
        assertThatThrownBy(() -> orderProcessor.process(null, cart))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customerId");
    }

    /** Blank customerId should throw IllegalArgumentException */
    @Test
    void process_blankCustomerId_throwsException() {
        cart.addItem(widget, 1);
        assertThatThrownBy(() -> orderProcessor.process("   ", cart))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customerId");
    }

    /** Null cart should throw IllegalArgumentException */
    @Test
    void process_nullCart_throwsException() {
        assertThatThrownBy(() -> orderProcessor.process("customer-8", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cart");
    }

    /** Empty cart should throw IllegalArgumentException */
    @Test
    void process_emptyCart_throwsException() {
        assertThatThrownBy(() -> orderProcessor.process("customer-9", cart))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    // =====================================================================
    // Dependency injection validation
    // =====================================================================

    /** Null inventoryService in constructor should throw IllegalArgumentException */
    @Test
    void constructor_nullInventoryService_throwsException() {
        assertThatThrownBy(() -> new OrderProcessor(null, paymentGateway))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inventoryService");
    }

    /** Null paymentGateway in constructor should throw IllegalArgumentException */
    @Test
    void constructor_nullPaymentGateway_throwsException() {
        assertThatThrownBy(() -> new OrderProcessor(inventoryService, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paymentGateway");
    }
}
