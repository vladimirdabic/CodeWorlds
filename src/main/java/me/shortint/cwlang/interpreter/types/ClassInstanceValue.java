package me.shortint.cwlang.interpreter.types;

import me.shortint.cwlang.interpreter.Environment;

public class ClassInstanceValue implements Value, ValueProperties {
    private final ClassValue classType;
    private final Environment environment;
    private final Environment static_environment;
    private final ClassInstanceValue superInstance;

    public ClassInstanceValue(ClassValue classType, Environment environment, ClassInstanceValue superInstance) {
        this.classType = classType;
        this.environment = environment;
        this.static_environment = environment.parent;
        this.superInstance = superInstance;
    }

    @Override
    public String getTypeName() {
        return classType.getClassName();
    }

    @Override
    public String toStringValue() {
        return "<" + classType.getClassName() + "_instance>";
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    @Override
    public Value getProperty(String propertyName) {
        if(environment.getNoParent(propertyName) != null)
            return environment.getNoParent(propertyName);

        if(static_environment.getNoParent(propertyName) != null)
            return static_environment.getNoParent(propertyName);

        if(superInstance != null)
            return superInstance.getProperty(propertyName);

        return null;
    }

    @Override
    public Value setProperty(String propertyName, Value propertyValue) {
        if(static_environment.assignNoParent(propertyName, propertyValue) != null)
            return propertyValue;

        if(environment.assignNoParent(propertyName, propertyValue) != null)
            return propertyValue;

        if(superInstance != null)
            return superInstance.setProperty(propertyName, propertyValue);

        return null;
    }

    public String getClassName() {
        return classType.getClassName();
    }

    public Environment getEnvironment() {
        return environment;
    }

    public ClassInstanceValue getSuperInstance(String superClassName) {
        if(classType.getClassName().equals(superClassName))
            return this;

        if(superInstance != null)
            return superInstance.getSuperInstance(superClassName);

        return null;
    }

    public ClassInstanceValue getSuperInstance() {
        return superInstance;
    }

    @Override
    public Value copy() {
        return this;
    }
}
