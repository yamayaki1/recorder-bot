package me.yamayaki.musicbot.utils;

import org.javacord.api.entity.permission.Permissions;

import java.util.HashMap;
import java.util.Map;

public class ChannelInfo {
    public long newChannel;
    public Map<Long, Long[]> rolePermissions;
    public Map<Long, Long[]> userPermissions;
    public String name;
    public Integer userLimit;

    public ChannelInfo(long newChannel, Map<Long, Permissions> rolePermissions, Map<Long, Permissions> userPermissions, String name, Integer userLimit) {
        this.newChannel = newChannel;
        this.rolePermissions = new HashMap<>();
        this.userPermissions = new HashMap<>();
        this.name = name;
        this.userLimit = userLimit;

        rolePermissions.forEach((roleId, permissions) -> this.rolePermissions.put(roleId, new Long[]{permissions.getAllowedBitmask(), permissions.getDeniedBitmask()}));
        userPermissions.forEach((userId, permissions) -> this.userPermissions.put(userId, new Long[]{permissions.getAllowedBitmask(), permissions.getDeniedBitmask()}));
    }
}
