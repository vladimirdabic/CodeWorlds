package me.shortint.cwlang.interpreter.types;

import me.shortint.cwlang.interpreter.Environment;
import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.ReturnError;
import me.shortint.cwlang.parser.Statement;

import java.util.Arrays;

public class FunctionValue implements Value, ValueCallable {
    public final Statement body;
    public final String[] argument_names;
    public final String function_name;
    public final boolean varargs;
    public Environment closure;
    public Value selfReference = null;

    public FunctionValue(String function_name, Statement body, String[] argument_names, Environment closure, boolean varargs) {
        this.function_name = function_name;
        this.body = body;
        this.argument_names = argument_names;
        this.closure = closure;
        this.varargs = varargs;
    }

    @Override
    public Value run(Interpreter interpreter, Value[] args, int line) {
        Environment environment = new Environment(closure);
        Environment originalEnvironment = interpreter.environment;

        if(selfReference != null) environment.thisValue = selfReference;

        int min = Math.min(args.length, varargs ? argument_names.length - 1 : argument_names.length);
        for(int i = 0; i < min; ++i) {
            environment.declare(argument_names[i], args[i]);
        }

        if(varargs && args.length >= argument_names.length) {
            ListValue varargsList = new ListValue();
            varargsList.getValues().addAll(Arrays.asList(args).subList(argument_names.length - 1, args.length));
            environment.declare(argument_names[argument_names.length - 1], varargsList);
        }

        Value return_value = Interpreter.nullValue;
        interpreter.environment = environment;

        try {
            interpreter.evaluate(body);
        } catch (ReturnError e) {
            return_value = e.value;
        }
        interpreter.environment = originalEnvironment;

        return return_value;
    }

    @Override
    public String getTypeName() {
        return "function";
    }

    @Override
    public String toStringValue() {
        return "<function '" + function_name + "'>";
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    @Override
    public Value copy() {
        return this;
    }
}
