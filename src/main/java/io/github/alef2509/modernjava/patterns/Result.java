package io.github.alef2509.modernjava.patterns;

import java.util.function.Function;

/// A tiny `Result` type: sealed interfaces + generic records give you algebraic
/// data types in plain Java, with no library.
public sealed interface Result<T> {

    record Success<T>(T value) implements Result<T> {
    }

    record Failure<T>(String error) implements Result<T> {
    }

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(String error) {
        return new Failure<>(error);
    }

    static Result<Integer> parseInt(String text) {
        try {
            return success(Integer.parseInt(text));
        } catch (NumberFormatException _) { // unnamed variable (JEP 456)
            return failure("not a number: " + text);
        }
    }

    default <R> Result<R> map(Function<? super T, ? extends R> mapper) {
        return switch (this) {
            case Success<T>(var value) -> success(mapper.apply(value));
            case Failure<T>(var error) -> failure(error);
        };
    }

    default <R> Result<R> flatMap(Function<? super T, Result<R>> mapper) {
        return switch (this) {
            case Success<T>(var value) -> mapper.apply(value);
            case Failure<T>(var error) -> failure(error);
        };
    }

    default T orElse(T fallback) {
        return switch (this) {
            case Success<T>(var value) -> value;
            case Failure<T> _ -> fallback;
        };
    }
}
