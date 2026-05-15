# ShopEasy Test Suite

SE3004 Software Testing — Term Project

## Project Structure

```
shopeasy-tests/
├── pom.xml
├── src/
│   ├── main/java/shopeasy/
│   │   ├── CartItem.java              (provided)
│   │   ├── InventoryService.java      (provided)
│   │   ├── Order.java                 (provided)
│   │   ├── OrderProcessor.java        (provided)
│   │   ├── PaymentGateway.java        (provided)
│   │   ├── PriceCalculator.java       (modified for Task 3)
│   │   ├── Product.java               (provided)
│   │   └── ShoppingCart.java          (modified for Task 3)
│   └── test/java/shopeasy/
│       ├── PriceCalculatorSpecTest.java       (Task 1)
│       ├── ShoppingCartStructuralTest.java    (Task 2)
│       ├── ContractTest.java                  (Task 3)
│       ├── ShopEasyPropertyTest.java          (Task 4)
│       └── OrderProcessorMockTest.java        (Task 5)
└── report/
    ├── jacoco-screenshot.png          (Task 2 — add after running JaCoCo)
    └── reflection.pdf                 (Task 6 — write separately)
```

## How to Run

### Compile and run all tests
```bash
mvn test
```

### Generate JaCoCo coverage report (Task 2)
```bash
mvn test jacoco:report
# Open target/site/jacoco/index.html in your browser
```

### Run PIT mutation testing (Task 2 bonus)
```bash
mvn org.pitest:pitest-maven:mutationCoverage
# Open target/pit-reports/ in your browser
```

### Run a specific test class
```bash
mvn test -Dtest=PriceCalculatorSpecTest
mvn test -Dtest=ShoppingCartStructuralTest
mvn test -Dtest=ContractTest
mvn test -Dtest=ShopEasyPropertyTest
mvn test -Dtest=OrderProcessorMockTest
```

## Task Summary

| Task | File(s) | Technique | Status |
|------|---------|-----------|--------|
| 1 | `PriceCalculatorSpecTest.java` | Specification-Based Testing (partition + boundary) | ✅ |
| 2 | `ShoppingCartStructuralTest.java` | Structural testing + JaCoCo coverage | ✅ |
| 3 | Modified `ShoppingCart.java`, `PriceCalculator.java` + `ContractTest.java` | Design by Contract (assertions) | ✅ |
| 4 | `ShopEasyPropertyTest.java` | Property-Based Testing (jqwik) | ✅ |
| 5 | `OrderProcessorMockTest.java` | Mocks & Stubs (Mockito) | ✅ |
| 6 | `reflection.pdf` | Reflection report | 📝 (write manually) |

## Notes

- Assertions are enabled via `-ea` in Maven Surefire configuration (pom.xml)
- Do NOT modify production classes in `src/main/java` except for Task 3 assertions
- All tests should pass with `mvn test` before submission
- Remember to take the JaCoCo screenshot for Task 2
