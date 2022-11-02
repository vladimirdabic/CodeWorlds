package me.shortint.cwlang.interpreter.types;

public interface Value {
    /**
     * Gets the type name of the value
     * <table>
     *     <thead>
     *         <tr>
     *             <th colspan="2">Default types</th>
     *         </tr>
     *     </thead>
     *     <tbody>
     *         <tr><td>number</td></tr>
     *         <tr><td>string</td></tr>
     *         <tr><td>object</td></tr>
     *         <tr><td>list</td></tr>
     *         <tr><td>function</td></tr>
     *         <tr><td>boolean</td></tr>
     *         <tr><td>null</td></tr>
     *     </tbody>
     * </table>
     * @return The type name as a string
     */
    String getTypeName();

    /**
     * Gets the string representation of the value
     */
    String toStringValue();

    /**
     * Checks if the value is equal to an another value
     * @param other {@link Value} to compare to
     */
    boolean equals(Value other);

    /**
     * Creates a copy of the value
     * @return Copied value
     */
    Value copy();
}
