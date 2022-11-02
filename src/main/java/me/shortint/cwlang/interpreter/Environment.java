package me.shortint.cwlang.interpreter;

import me.shortint.cwlang.interpreter.types.StringValue;
import me.shortint.cwlang.interpreter.types.Value;

import java.util.HashMap;

public class Environment {
    public Environment parent;
    public Value thisValue;
    private final HashMap<String, Value> values;


    public Environment() {
        parent = null;
        values = new HashMap<>();
    }

    public Environment(Environment parent) {
        this.parent = parent;
        values = new HashMap<>();
    }

    public Environment(Environment parent, HashMap<String, Value> values) {
        this.parent = parent;
        this.values = values;
    }

    /**
     * Gets a variable from the environment
     * @param variable Name of the variable
     * @return {@link Value} or null if the variable was not declared
     */
    public Value get(String variable) {
        if(values.containsKey(variable)) return values.get(variable);
        if(parent != null) return parent.get(variable);

        return null;
    }

    public Value getNoParent(String variable) {
        return values.getOrDefault(variable, null);
    }

    /**
     * Assigns a value to a variable
     * @param variable Name of the variable
     * @param value {@link Value} to assign
     * @return The assigned value or null if the variable was not declared
     */
    public Value assign(String variable, Value value) {
        if(values.containsKey(variable)) {
            values.put(variable, value);
            return value;
        }
        if(parent != null) return parent.assign(variable, value);
        return null;
    }

    public Value assignNoParent(String variable, Value value) {
        if(values.containsKey(variable)) {
            values.put(variable, value);
            return value;
        }
        return null;
    }

    public Value getThisValue() {
        if(thisValue != null) return thisValue;
        if(parent != null) return parent.getThisValue();

        return null;
    }

    /**
     * Declares a variable in the environment
     * @param variable Name of the variable
     * @param value {@link Value} to assign
     */
    public void declare(String variable, Value value) {
        values.put(variable, value);
    }
}
