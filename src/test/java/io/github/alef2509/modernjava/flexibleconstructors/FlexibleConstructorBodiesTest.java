package io.github.alef2509.modernjava.flexibleconstructors;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlexibleConstructorBodiesTest {

    @Test
    void superclassConstructorSeesFieldsAssignedInThePrologue() {
        // Shape() calls describe(); without JEP 513 it would print "0.0 x 0.0"
        assertThat(new Rectangle(3, 4).description()).isEqualTo("rectangle 3.0 x 4.0");
    }

    @Test
    void argumentsAreValidatedBeforeSuperRuns() {
        assertThatThrownBy(() -> new Rectangle(-1, 4)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void codeCanRunBeforeDelegatingToAnotherConstructor() {
        assertThat(new Rectangle(" 2 X 5 ").area()).isEqualTo(10.0);
        assertThatThrownBy(() -> new Rectangle("2x3x4")).hasMessageContaining("WIDTHxHEIGHT");
    }
}
