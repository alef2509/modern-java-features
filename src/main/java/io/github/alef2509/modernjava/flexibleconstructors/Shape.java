package io.github.alef2509.modernjava.flexibleconstructors;

/// Superclass whose constructor calls an overridable method: a classic trap,
/// because the subclass fields are not initialized yet at that point.
public abstract class Shape {

    private final String description;

    protected Shape() {
        this.description = describe();
    }

    protected abstract String describe();

    public String description() {
        return description;
    }
}
