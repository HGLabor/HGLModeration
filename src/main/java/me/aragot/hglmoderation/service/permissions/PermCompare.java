package me.aragot.hglmoderation.service.permissions;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class PermCompare {
    public static final int EQUAL = 0;
    public static final int SMALLER_THAN = 1;
    public static final int GREATER_THAN = 2;

    public static CompletableFuture<Integer> comparePermissionOf(UUID base, UUID toCompare) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User baseUser;
        User toCompareUser;
        try {
            baseUser = luckPerms.getUserManager().loadUser(base).get();
            toCompareUser = luckPerms.getUserManager().loadUser(toCompare).get();
        } catch (InterruptedException | ExecutionException x) {
            return CompletableFuture.completedFuture(SMALLER_THAN);
        }

        int baseUserWeight = getHighestWeightOfUser(baseUser);
        int toCompareWeight = getHighestWeightOfUser(toCompareUser);

        if (baseUserWeight > toCompareWeight)
            return CompletableFuture.completedFuture(GREATER_THAN);
        else if (baseUserWeight < toCompareWeight)
            return CompletableFuture.completedFuture(SMALLER_THAN);

        return CompletableFuture.completedFuture(EQUAL);
    }

    private static int getHighestWeightOfUser(User user) {
        int highestWeight = Integer.MIN_VALUE;
        for (Group group : user.getInheritedGroups(user.getQueryOptions())) {
            int groupWeight = group.getWeight().orElse(0);
            highestWeight = Math.max(highestWeight, groupWeight);
        }
        return highestWeight;
    }
}
