package me.shortint.cwlang.interpreter.types;

public interface ValueProperties {
    /**
     * Gets a property from a value
     * @param propertyName The name of the property
     * @return {@link Value} of the property
     */
    Value getProperty(String propertyName);

    /**
     * Sets a property in a value
     * @param propertyName The name of the property
     * @param propertyValue The new {@link Value} of the property
     * @return The same {@link Value} passed to this function
     */
    Value setProperty(String propertyName, Value propertyValue);
}
