package com.benj4.gcpm.commands;

import com.benj4.gcpm.GCPMPlugin;
import com.benj4.gcpm.handlers.GCPMPermissionHandler;
import com.benj4.gcpm.objects.GCPMGroup;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.Account;
import emu.grasscutter.game.player.Player;
import nosqlite.Database;

import java.io.FileNotFoundException;
import java.util.List;

import static com.benj4.gcpm.handlers.GCPMPermissionHandler.sendPermissionError;

@Command(label = "gcpm",
        usage = "gcpm <reload|player|group|help>",
        description = "gcpm.command",
        targetRequirement = Command.TargetRequirement.NONE
)
public class GCPMCommand implements CommandHandler {
    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if(args.size() == 0) {
            // Send help.
            return;
        }

        switch(args.get(0)) {
            case "help" -> {
                // Send help.
                if(!Grasscutter.getPermissionHandler().checkPermission(sender, targetPlayer, "gcpm.command.help", null)) {
                    sendPermissionError(sender);
                    break;
                }

                break;
            }
            case "player" -> {
                if(!Grasscutter.getPermissionHandler().checkPermission(sender, targetPlayer, "gcpm.command.player", null)) {
                    sendPermissionError(sender);
                    break;
                }

                playerCommand(sender, targetPlayer, args.stream().skip(1).toList());
                break;
            }
            case "group" -> {
                if(!Grasscutter.getPermissionHandler().checkPermission(sender, targetPlayer, "gcpm.command.group", null)) {
                    sendPermissionError(sender);
                    break;
                }

                break;
            }
            case "reload" -> {
                if(!Grasscutter.getPermissionHandler().checkPermission(sender, targetPlayer, "gcpm.command.reload", null)) {
                    sendPermissionError(sender);
                    break;
                }

                try {
                    if(GCPMPlugin.getInstance().loadGroups()) {
                        CommandHandler.sendMessage(sender, String.format("GCPM groups reloaded."));
                    } else {
                        CommandHandler.sendMessage(sender, String.format("Unable to reload GCPM groups. No groups available."));
                    }
                } catch (FileNotFoundException e) {
                    CommandHandler.sendMessage(sender, String.format("Unable to reload GCPM groups. Check the console."));
                    Grasscutter.getLogger().error("Unable to load GCPMGroups file. ", e);
                }

                try {
                    GCPMPlugin.getInstance().loadConfig();
                    CommandHandler.sendMessage(sender, String.format("GCPM Config reloaded."));
                } catch (FileNotFoundException e) {
                    CommandHandler.sendMessage(sender, String.format("Unable to reload GCPM config. Check the console."));
                    Grasscutter.getLogger().error("Unable to load GCPMConfig file. ", e);
                }

                break;
            }
            default -> {
                CommandHandler.sendMessage(sender, String.format("Invalid subcommand '{}'. Use '/gcpm help' to list all available commands.", args.get(0)));
                break;
            }
        }
    }

    // /gcpm player <username|uid> <permission|perm|p|group|g> <add|remove> <permission|group>
    // /gcpm @uid <permission|perm|p|group|g> <add|remove> <permission|group>
    private void playerCommand(Player sender, Player targetPlayer, List<String> args) {
        Account player; // <@uid||username||uid>
        String func; // <permission|perm|p|group|g>
        String op; // <add|remove>
        String var; // <permission|group>

        if(targetPlayer == sender) {
            if(args.size() != 4) {
                CommandHandler.sendMessage(sender, "Invalid argument size for subcommand 'player'.");
                return;
            }

            // Get style of username param (username or uid)
            if(args.get(0).matches("-\\d+"))  {
                // is uid
                player = DatabaseHelper.getAccountByPlayerId(Integer.parseInt(args.get(0)));
            } else {
                // is username
                player = DatabaseHelper.getAccountByName(args.get(0));
            }

            func = args.get(1);
            op = args.get(2);
            var = args.get(3);
        } else {
            if(args.size() != 3) {
                CommandHandler.sendMessage(sender, "Invalid argument size for subcommand 'player'.");
                return;
            }

            player = targetPlayer.getAccount();
            func = determinePlayerFunc(args.get(0));
            op = args.get(1);
            var = args.get(2);
        }

        if(player == null) {
            CommandHandler.sendMessage(sender, "Player not found.");
            return;
        }

        String permissionString;
        if(!func.isEmpty() && func == "permission") {
            permissionString = var;
        } else if(!func.isEmpty() && func == "group") {
            permissionString = GCPMPermissionHandler.GCPM_GROUP_PREFIX + "$" + var;
        } else {
            CommandHandler.sendMessage(sender, "Unknown function for subcommand 'player'.");
            return;
        }


        switch(op) {
            case "add" -> {
                player.addPermission(permissionString);
                if(func == "permission") {
                    CommandHandler.sendMessage(sender, String.format("Successfully added {0} permission to player {1}", var, player.toString()));
                } else if(func == "group") {
                    CommandHandler.sendMessage(sender, String.format("Successfully added {0} group to player {1}", var, player.toString()));
                }
            }
            case "remove" -> {
                player.removePermission(permissionString);
            }
            default -> {
                CommandHandler.sendMessage(sender, "Unknown operation for subcommand 'player'.");
                return;
            }
        }
    }

    private String determinePlayerFunc(String rawFunc) {
        switch(rawFunc) {
            case "permission", "perm", "p" -> {
                return "permission";
            }
            case "group", "g" -> {
                return "group";
            }
            default -> {
                return null;
            }
        }
    }
}
