package me.shortint.cwlang.interpreter.types;

import me.shortint.cwlang.interpreter.Environment;
import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.parser.Expression;

import java.util.HashMap;
import java.util.Map;

public class ClassValue implements Value, ValueNewInstance, ValueClass, ValueProperties {
    private final String className;
    private final ClassValue parentClass;
    private final String context;
    private final int line;
    public final FunctionValue constructor;
    public final HashMap<String, Expression> properties;
    public boolean executeConstructor = true;

    public final Environment static_environment;

    public ClassValue(String className, String context, int line, FunctionValue constructor, HashMap<String, Expression> properties, Environment classEnvironment, HashMap<String, Value> static_properties, ClassValue parentClass) {
        this.className = className;
        this.context = context;
        this.line = line;
        this.constructor = constructor;
        this.properties = properties;
        this.parentClass = parentClass;
        this.static_environment = new Environment(classEnvironment);

        Value value;

        for(Map.Entry<String, Value> prop : static_properties.entrySet()) {
            value = prop.getValue();

            if(value instanceof FunctionValue) {
                ((FunctionValue) value).selfReference = this;
                ((FunctionValue) value).closure = static_environment;
            }

            static_environment.declare(prop.getKey(), value);
        }
    }

    @Override
    public String getTypeName() {
        return className;
    }

    @Override
    public String toStringValue() {
        return "<" + className + "@" + context + ":" + line + ">";
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    @Override
    public Value newInstance(Interpreter interpreter, Value[] args, int line) {
        ClassInstanceValue superInstance = null;

        if(parentClass != null) {
            parentClass.executeConstructor = false;
            Value instance = parentClass.newInstance(interpreter, args, line);

            if(!(instance instanceof ClassInstanceValue))
                throw interpreter.error(line, "Can only inherit from user defined classes");

            superInstance = (ClassInstanceValue)instance;
            static_environment.parent = superInstance.getEnvironment();
        }

        Environment instanceEnvironment = new Environment(static_environment);
        ClassInstanceValue instance = new ClassInstanceValue(this, instanceEnvironment, superInstance);

        // initialize instance
        for(Map.Entry<String, Expression> property : properties.entrySet()) {
            String key = property.getKey();
            Value value = property.getValue() == null ? Interpreter.nullValue : interpreter.evaluate(property.getValue());

            if(value instanceof FunctionValue) {
                ((FunctionValue) value).selfReference = instance;
                ((FunctionValue) value).closure = instanceEnvironment;
            }

            instanceEnvironment.declare(key, value);
        }

        if(executeConstructor && constructor != null) {
            constructor.selfReference = instance;
            constructor.closure = instanceEnvironment;
            constructor.run(interpreter, args, line);
        }

        executeConstructor = true;
        return instance;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public boolean valueIsInstance(Value value) {
        if(!(value instanceof ClassInstanceValue))
            return false;

        ClassInstanceValue classInstance = (ClassInstanceValue)value;
        return classInstance.getClassName().equals(className);
    }

    @Override
    public Value castValueToSelf(Interpreter interpreter, int line, Value value) {
        if(!(value instanceof ClassInstanceValue))
            throw interpreter.error(line, "Cannot cast a non user defined class to user defined class '" + className + "'");

        ClassInstanceValue instance = (ClassInstanceValue)value;
        return instance.getSuperInstance(this.className);
    }

    @Override
    public Value getProperty(String propertyName) {
        return static_environment.getNoParent(propertyName);
    }

    @Override
    public Value setProperty(String propertyName, Value propertyValue) {
        return static_environment.assignNoParent(propertyName, propertyValue);
    }

    @Override
    public Value copy() {
        return this;
    }
}
