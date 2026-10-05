package io.github.alef2509.modernjava.flexibleconstructors;

import java.util.Locale;

/// **Flexible Constructor Bodies** (JEP 513, final in Java 25).
///
/// Statements may now appear *before* `super(...)` / `this(...)`, as long as they
/// do not use the instance being built. Two practical wins:
/// 1. **Fail fast**: validate and normalize arguments before the superclass
///    constructor runs, instead of squeezing the logic into a static helper call
///    inside the `super(...)` arguments.
/// 2. **Safer initialization**: fields can be assigned before `super()`, so a
///    superclass constructor that calls an overridden method sees them initialized
///    (`describe()` below would otherwise read `width = 0`, `height = 0`).
public final class Rectangle extends Shape {

    private final double width;
    private final double height;

    public Rectangle(double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("sides must be positive");
        }
        this.width = width;
        this.height = height;
        super();
    }

    /// Parses `"3x4"`. Parsing and validation happen *before* delegating with
    /// `this(...)`, which used to require a static helper method.
    public Rectangle(String spec) {
        String[] sides = spec.toLowerCase(Locale.ROOT).split("x");
        if (sides.length != 2) {
            throw new IllegalArgumentException("expected WIDTHxHEIGHT but was: " + spec);
        }
        this(Double.parseDouble(sides[0].strip()), Double.parseDouble(sides[1].strip()));
    }

    @Override
    protected String describe() {
        return "rectangle %.1f x %.1f".formatted(width, height);
    }

    public double area() {
        return width * height;
    }
}
