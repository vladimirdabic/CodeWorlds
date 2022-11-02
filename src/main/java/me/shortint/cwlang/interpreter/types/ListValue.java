package me.shortint.cwlang.interpreter.types;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.parser.Statement;

import java.util.ArrayList;
import java.util.List;

public class ListValue implements Value, ValueIndexable, ValueProperties, ValueIterable {
    private final ArrayList<Value> values = new ArrayList<>();

    @Override
    public String getTypeName() {
        return "list";
    }

    @Override
    public String toStringValue() {
        return null;
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    @Override
    public Value getAtIndex(Value index) {
        if(!(index instanceof NumberValue))
            return null;

        NumberValue indexNumber = (NumberValue)index;

        if(indexNumber.getValue() % 1 != 0)
            return null;

        int idx = (int)indexNumber.getValue();

        if(idx < 0 || idx >= values.size()) return null;
        return values.get(idx);
    }

    @Override
    public Value setAtIndex(Value index, Value value) {
        if(!(index instanceof NumberValue))
            return null;

        NumberValue indexNumber = (NumberValue)index;

        if(indexNumber.getValue() % 1 != 0)
            return null;

        int idx = (int)indexNumber.getValue();

        if(idx < 0 || idx >= values.size()) return null;
        values.set(idx, value);
        return value;
    }

    @Override
    public Value getProperty(String propertyName) {
        ListValue self = this;

        switch (propertyName) {
            case "append":
                return new RunnableValue() {
                    @Override
                    public Value run(Interpreter interpreter, Value[] args, int line) {
                        if(args.length == 0)
                            throw interpreter.error(line, "List append function requires one argument");

                        self.values.add(args[0]);
                        return self;
                    }
                };

            case "clear":
                return new RunnableValue() {
                    @Override
                    public Value run(Interpreter interpreter, Value[] args, int line) {
                        self.values.clear();
                        return self;
                    }
                };

            case "pop":
                return new RunnableValue() {
                    @Override
                    public Value run(Interpreter interpreter, Value[] args, int line) {
                        int size = self.values.size();

                        if(args.length == 0) {
                            if (size == 0)
                                throw interpreter.error(line, "Index out of bounds for pop function");

                            return self.values.remove(size - 1);
                        }

                        if(!(args[0] instanceof NumberValue) || ((NumberValue)args[0]).getValue() % 1 != 0)
                            throw interpreter.error(line, "List pop function requires one integer argument");

                        int index = (int)((NumberValue)args[0]).getValue();

                        if(index < 0 || index >= size)
                            throw interpreter.error(line, "Index out of bounds for pop function");

                        return self.values.remove(index);
                    }
                };

            case "length":
            case "size":
                return new NumberValue(self.values.size());
        }

        return null;
    }

    @Override
    public Value setProperty(String propertyName, Value propertyValue) {
        return null;
    }


    @Override
    public void iterate(Interpreter interpreter, Statement body, String variableName) {
        for(Value value : values) {
            interpreter.environment.declare(variableName, value);
            interpreter.evaluate(body);
        }
    }

    @Override
    public Value copy() {
        return this;
    }

    public List<Value> getValues() {
        return values;
    }
}
