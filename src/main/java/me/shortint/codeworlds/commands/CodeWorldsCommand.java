package me.shortint.codeworlds.commands;

import me.shortint.codeworlds.CodeWorldsPlugin;
import me.shortint.codeworlds.listeners.EventData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.help.HelpTopic;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

public class CodeWorldsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!command.testPermission(sender))
            return true;

        if(args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Invalid usage");
            return true;
        }


        if(args[0].equals("execute")) {
            if(args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /" + label + " execute <script_name>");
                return true;
            }
            String scriptName = args[1];
            File file = Paths.get(CodeWorldsPlugin.getInstance().getDataFolder().getAbsolutePath() + "/scripts/" + scriptName + ".cw").toFile();

            if(!file.exists()) {
                sender.sendMessage(ChatColor.RED + "Script not found");
                return true;
            }

            CodeWorldsPlugin.executeFile(file, sender);
        }

        if(args[0].equals("reload")) {
            CodeWorldsPlugin.getInstance().reload(sender);
            sender.sendMessage(ChatColor.GREEN + "Scripts have been reloaded");
        }

        if(args[0].equals("unregister")) {
            String cmdName = args[1];
            CodeWorldsRegisteredCommand foundCommand = null;

            for(CodeWorldsRegisteredCommand codeWorldsCommand : CodeWorldsPlugin.getRegisteredCommands()) {
                if(codeWorldsCommand.getName().equals(cmdName)) {
                    HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(codeWorldsCommand.commandCategory);
                    if(topic != null) Bukkit.getServer().getHelpMap().getHelpTopics().remove(topic);
                    codeWorldsCommand.unregister();
                    foundCommand = codeWorldsCommand;
                }
            }

            CodeWorldsPlugin.getRegisteredCommands().remove(foundCommand);
            CodeWorldsPlugin.syncCommands();
            sender.sendMessage(ChatColor.GREEN + "Command unregistered");
        }

        if(args[0].equals("terminate")) {
            String contextName = args[1];

            for(Map.Entry<String, Set<EventData>> listenerData : CodeWorldsPlugin.getEventDistributor().eventListeners.entrySet())
                listenerData.getValue().removeIf(eventData -> eventData.eventContext.equals(contextName));

            for(CodeWorldsRegisteredCommand codeWorldsCommand : CodeWorldsPlugin.getRegisteredCommands()) {
                HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(codeWorldsCommand.commandCategory);

                // remove category
                if(topic != null)
                    Bukkit.getServer().getHelpMap().getHelpTopics().remove(topic);

                codeWorldsCommand.unregister();
            }

            CodeWorldsPlugin.syncCommands();
            sender.sendMessage(ChatColor.GREEN + "Script '" + contextName + "' was terminated");
        }

        return true;
    }
}
