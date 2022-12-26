package me.yamayaki.musicbot.entities;

import org.javacord.api.entity.permission.Permissions;

import java.util.Arrays;
import java.util.Map;

public class ChannelInfo {
    public final long newChannel;
    public final String name;
    public final Integer userLimit;

    public final long[][] rolePermissions;
    public final long[][] userPermissions;

    public ChannelInfo(long newChannel, Map<Long, Permissions> rolePermissions, Map<Long, Permissions> userPermissions, String name, Integer userLimit) {
        this.newChannel = newChannel;
        this.rolePermissions = new long[rolePermissions.size()][3];
        this.userPermissions = new long[userPermissions.size()][3];
        this.name = name;
        this.userLimit = userLimit;

        int i = 0;
        for (Map.Entry<Long, Permissions> set : rolePermissions.entrySet()) {
            this.rolePermissions[i] = new long[]{set.getKey(), set.getValue().getAllowedBitmask(), set.getValue().getDeniedBitmask()};
            i++;
        }

        i = 0;
        for (Map.Entry<Long, Permissions> set : userPermissions.entrySet()) {
            this.userPermissions[i] = new long[]{set.getKey(), set.getValue().getAllowedBitmask(), set.getValue().getDeniedBitmask()};
            i++;
        }
    }

    @Override
    public String toString() {
        return "ChannelInfo{" +
                "newChannel=" + newChannel +
                ", name='" + name + '\'' +
                ", userLimit=" + userLimit +
                ", rolePermissions=" + Arrays.toString(rolePermissions) +
                ", userPermissions=" + Arrays.toString(userPermissions) +
                '}';
    }
}
