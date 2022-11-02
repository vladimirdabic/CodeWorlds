package me.shortint.codeworlds.utils;

import me.shortint.codeworlds.CodeWorldsPlugin;
import me.shortint.codeworlds.commands.CodeWorldsRegisteredCommand;
import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.ValueCallable;
import me.shortint.cwlang.parser.Statement;

import java.util.List;

public class RegisterInterface implements Interpreter.CodeWorldsInterface {
    @Override
    public void registerEvent(Interpreter interpreter, Statement.EventDeclaration declaration) {
        CodeWorldsPlugin.getEventDistributor().registerEventListener(declaration.eventName.lexeme, declaration.args, declaration.body);
    }

    @Override
    public void registerCommand(Interpreter interpreter, ValueCallable executor, String name, String description, String usageMessage, String category, List<String> aliases, int line) {
        CodeWorldsRegisteredCommand command = new CodeWorldsRegisteredCommand(name, description, usageMessage, category, aliases, executor, interpreter.context, line, interpreter.environment);
        command.register();
        CodeWorldsPlugin.getRegisteredCommands().add(command);
    }
}
