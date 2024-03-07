package me.aragot.hglmoderation.tools.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.group.GroupManager;
import net.luckperms.api.model.user.User;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PermCompare {
    public static final int EQUAL = 0;
    public static final int SMALLER_THAN = 1;
    public static final int GREATER_THAN = 2;

    public CompletableFuture<Integer> comparePermissionOf(UUID base, UUID toCompare){
        LuckPerms luckPerms = LuckPermsProvider.get();
        User baseUser;
        User toCompareUser;
        try {
            baseUser = luckPerms.getUserManager().loadUser(base).get();
            toCompareUser = luckPerms.getUserManager().loadUser(toCompare).get();
        } catch (InterruptedException | ExecutionException x) {
            return CompletableFuture.completedFuture(SMALLER_THAN);
        }

        GroupManager groupManager = luckPerms.getGroupManager();

        Group baseGroup = groupManager.getGroup(baseUser.getPrimaryGroup());
        Group toCompareGroup = groupManager.getGroup(toCompareUser.getPrimaryGroup());

        if(baseGroup == null)
            return CompletableFuture.completedFuture(SMALLER_THAN);

        if(toCompareGroup == null)
            return CompletableFuture.completedFuture(GREATER_THAN);

        int baseUserWeight = baseGroup.getWeight().orElse(Integer.MIN_VALUE);
        int toCompareWeight = toCompareGroup.getWeight().orElse(Integer.MIN_VALUE);

        if(baseUserWeight > toCompareWeight)
            return CompletableFuture.completedFuture(GREATER_THAN);
        else if(baseUserWeight < toCompareWeight)
            return CompletableFuture.completedFuture(SMALLER_THAN);

        return CompletableFuture.completedFuture(EQUAL);
    }
}
