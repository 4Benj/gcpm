package com.benj4.gcpm.handlers;

import com.benj4.gcpm.GCPMPlugin;
import com.benj4.gcpm.objects.GCPMGroup;
import emu.grasscutter.Configuration;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.command.PermissionHandler;
import emu.grasscutter.game.Account;
import emu.grasscutter.game.player.Player;

import java.util.*;
import java.util.stream.Stream;

public class GCPMPermissionHandler implements PermissionHandler {
    public static final String GCPM_GROUP_PREFIX = "gcpmGroup";

    @Override
    public boolean EnablePermissionCommand() {
        return false;
    }

    @Override
    public boolean checkPermission(Player sender, Player targetPlayer, String permissionNode, String permissionNodeTargeted) {
        if(sender == null) {
            return true; // Most likely sent from console
        } else {
            Account account = sender.getAccount();
            String requiredPermission = sender == targetPlayer ? permissionNode : permissionNodeTargeted;

            // Check individual player permissions
            if(!requiredPermission.isEmpty() && hasPermission(account.getPermissions(), requiredPermission)) {
                return true;
            } else if (!requiredPermission.isEmpty() && permissionTaken(account.getPermissions(), requiredPermission)) {
                sendPermissionError(sender);
                return false;
            }

            // Check group permissions
            List<String> permissionGroupStrs = account.getPermissions().stream().filter(x -> x.startsWith(GCPM_GROUP_PREFIX + "$")).toList();
            List<GCPMGroup> permissionGroups =
                    GCPMPlugin.getInstance().getGroups().values().stream().filter(x -> permissionGroupStrs.contains(GCPM_GROUP_PREFIX + "$" + x.getName())).sorted(Comparator.comparingInt(GCPMGroup::getWeight).reversed()).toList();

            for (GCPMGroup permissionGroup : permissionGroups) {
                if(permissionGroup != null && hasPermission(permissionGroup.getPermissions(), requiredPermission)) {
                    return true;
                } else if(permissionGroup != null && permissionTaken(permissionGroup.getPermissions(), requiredPermission)){
                    sendPermissionError(sender);
                    return false;
                }
            }
        }

        sendPermissionError(sender);
        return false;
    }

    public static void sendPermissionError(Player sender) {
        CommandHandler.sendTranslatedMessage(sender, "commands.generic.permission_error", new Object[0]);
    }

    /**
     * Check if player has a permission
     * @param permissions The permission list (from the player or permission group)
     * @param permission The permission node to check
     * @return If the player has the permission
     */
    public static boolean hasPermission(List<String> permissions, String permission) {
        if(permissions.contains("*") && permissions.size() == 1) return true;

        if (permissions.contains(permission)) return true;

        String[] permissionParts = permission.split("\\.");
        for (String p : permissions) {
            if (p.startsWith("-") && Account.permissionMatchesWildcard(p.substring(1), permissionParts)) return false;
            if (Account.permissionMatchesWildcard(p, permissionParts)) return true;
        }

        return permissions.contains("*");
    }

    /**
     * Check if permission has the - prefix.
     * @param permissions The permission list (from the player or permission group)
     * @param permission The permission node to check
     * @return If the permission has the - prefix
     */
    public static boolean permissionTaken(List<String> permissions, String permission) {
        if (permissions.contains("-" + permission)) return true;

        String[] permissionParts = permission.split("\\.");
        for (String p : permissions) {
            if (p.startsWith("-") && Account.permissionMatchesWildcard(p.substring(1), permissionParts)) return true;
        }

        return false;
    }
}
