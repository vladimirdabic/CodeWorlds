package me.shortint.cwlang.interpreter.types;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.parser.Statement;

import java.util.HashMap;
import java.util.Map;

public class ObjectValue implements Value, ValueProperties, ValueIterable, ValueIndexable {
    private final HashMap<Value, Value> properties = new HashMap<>();

    @Override
    public String getTypeName() {
        return "object";
    }

    @Override
    public String toStringValue() {
        return "<object{}>";
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    @Override
    public Value getProperty(String propertyName) {
        Value value = properties.getOrDefault(new StringValue(propertyName), Interpreter.nullValue);

        if(value instanceof FunctionValue)
            ((FunctionValue)value).selfReference = this;

        return value;
    }

    @Override
    public Value setProperty(String propertyName, Value propertyValue) {
        properties.put(new StringValue(propertyName), propertyValue);
        return propertyValue;
    }

    @Override
    public void iterate(Interpreter interpreter, Statement body, String variableName) {
        ObjectValue kvPair = new ObjectValue();
        for(Map.Entry<Value, Value> pair : properties.entrySet()) {
            kvPair.setProperty("key", pair.getKey().copy());
            kvPair.setProperty("value", pair.getValue());
            interpreter.environment.declare(variableName, kvPair);
            interpreter.evaluate(body);
        }
    }

    @Override
    public Value copy() {
        return this;
    }

    @Override
    public Value getAtIndex(Value index) {
        return properties.getOrDefault(index, Interpreter.nullValue);
    }

    @Override
    public Value setAtIndex(Value index, Value value) {
        properties.put(index.copy(), value);
        return value;
    }
}
