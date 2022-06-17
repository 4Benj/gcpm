package com.benj4.gcpm.handlers;

import com.benj4.gcpm.GCPMPlugin;
import com.benj4.gcpm.objects.GCPMGroup;
import emu.grasscutter.Configuration;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.command.PermissionHandler;
import emu.grasscutter.game.Account;
import emu.grasscutter.game.player.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class GCPMPermissionHandler implements PermissionHandler {
    private final String GCPM_GROUP_PREFIX = "gcpmGroup";

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
            if(requiredPermission.isEmpty() && hasPermission(account.getPermissions(), requiredPermission)) {
                return true;
            }

            // Check group permissions
            List<String> permissionGroups = account.getPermissions().stream().filter(x -> x.startsWith(GCPM_GROUP_PREFIX + "$")).toList();

            for (String permissionGroupPerm : permissionGroups) {
                String permissionGroupRawStr = permissionGroupPerm.substring((GCPM_GROUP_PREFIX + "$").length());
                GCPMGroup permissionGroup = getPermissionGroup(permissionGroupRawStr);
                if(permissionGroup != null && hasPermission(List.of(permissionGroup.permissions), requiredPermission)) {
                    return true;
                }
            }
        }

        CommandHandler.sendTranslatedMessage(sender, "commands.generic.permission_error", new Object[0]);
        return false;
    }

    public boolean hasPermission(List<String> permissionsList, String permission) {
        if (permissionsList.contains("*") && permissionsList.size() == 1) {
            return true;
        } else {
            List<String> permissions = Stream.of(permissionsList, Arrays.asList(Configuration.ACCOUNT.defaultPermissions)).flatMap(Collection::stream).distinct().toList();
            if (permissions.contains(permission)) {
                return true;
            } else {
                String[] permissionParts = permission.split("\\.");
                Iterator var4 = permissions.iterator();

                String p;
                do {
                    if (!var4.hasNext()) {
                        return permissions.contains("*");
                    }

                    p = (String)var4.next();
                    if (p.startsWith("-") && Account.permissionMatchesWildcard(p.substring(1), permissionParts)) {
                        return false;
                    }
                } while(!Account.permissionMatchesWildcard(p, permissionParts));

                return true;
            }
        }
    }

    public GCPMGroup getPermissionGroup(String name) {
        if(GCPMPlugin.getInstance().getGroups().containsKey(name)) {
            return GCPMPlugin.getInstance().getGroups().get(name);
        } else {
            return null;
        }
    }
}
