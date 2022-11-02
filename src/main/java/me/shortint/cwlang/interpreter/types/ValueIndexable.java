package me.shortint.cwlang.interpreter.types;

public interface ValueIndexable {
    /**
     * Gets a value at an index in a value
     * @param index The index
     * @return {@link Value} at the index or null if the index is invalid
     */
    Value getAtIndex(Value index);

    /**
     * Sets a value at an index in a value
     * @param index The index
     * @param value {@link Value} to set
     * @return {@link Value} passed to the function or null if the index is invalid
     */
    Value setAtIndex(Value index, Value value);
}
