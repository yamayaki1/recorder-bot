package me.yamayaki.musicbot.entities;

import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.ServerVoiceChannelBuilder;
import org.javacord.api.entity.channel.ServerVoiceChannelUpdater;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChannelCopy {
    private String name;
    private Integer userLimit;

    private long categoryId = 0;
    private int position;

    private long[][] rolePermissions;
    private long[][] userPermissions;

    private long associatedChannel = 0L;

    public static ChannelCopy of(ServerVoiceChannel voiceChannel) {
        ChannelCopy cInfo = new ChannelCopy();

        cInfo.name = voiceChannel.getName();
        cInfo.userLimit = voiceChannel.getUserLimit().orElse(0);
        cInfo.position = voiceChannel.getRawPosition();

        cInfo.rolePermissions = copyPermissions(voiceChannel.getOverwrittenRolePermissions());
        cInfo.userPermissions = copyPermissions(voiceChannel.getOverwrittenRolePermissions());

        voiceChannel.getCategory().ifPresent(channelCategory -> {
            cInfo.categoryId = channelCategory.getId();
        });

        return cInfo;
    }

    private static long[][] copyPermissions(Map<Long, Permissions> permissionsMap) {
        long[][] permissions = new long[permissionsMap.size()][3];

        int index = 0;
        for (Map.Entry<Long, Permissions> mapEntry : permissionsMap.entrySet()) {
            permissions[index] = new long[]{mapEntry.getKey(), mapEntry.getValue().getAllowedBitmask(), mapEntry.getValue().getDeniedBitmask()};
            index++;
        }

        return permissions;
    }

    public CompletableFuture<Void> into(ServerVoiceChannel voiceChannel, boolean removeExistingPerms) {
        ServerVoiceChannelUpdater voiceUpdater = voiceChannel.createUpdater();
        Server server = voiceChannel.getServer();

        voiceUpdater.setName(this.name);
        voiceUpdater.setUserLimit(this.userLimit);

        if (removeExistingPerms) {
            voiceChannel.getOverwrittenUserPermissions().forEach((userId, permissions) -> {
                User user = server.getMemberById(userId).orElse(null);
                voiceUpdater.removePermissionOverwrite(user);
            });
        }

        for (long[] data : this.rolePermissions) {
            voiceUpdater.addPermissionOverwrite(server.getRoleById(data[0]).orElse(null), Permissions.fromBitmask(data[1], data[2]));
        }

        for (long[] data : this.userPermissions) {
            voiceUpdater.addPermissionOverwrite(server.getMemberById(data[0]).orElse(null), Permissions.fromBitmask(data[1], data[2]));
        }

        return voiceUpdater.update();
    }

    public ServerVoiceChannel newFrom(Server server) {
        ServerVoiceChannelBuilder voiceBuilder = server.createVoiceChannelBuilder()
                .setName(this.name)
                .setUserlimit(this.userLimit)
                .setCategory(server.getChannelCategoryById(this.categoryId).orElse(null));

        for (long[] data : this.rolePermissions) {
            voiceBuilder.addPermissionOverwrite(server.getRoleById(data[0]).orElse(null), Permissions.fromBitmask(data[1], data[2]));
        }

        for (long[] data : this.userPermissions) {
            voiceBuilder.addPermissionOverwrite(server.getMemberById(data[0]).orElse(null), Permissions.fromBitmask(data[1], data[2]));
        }

        ServerVoiceChannel voiceChannel = voiceBuilder.create().join();
        voiceChannel.createUpdater().setRawPosition(this.position).update().join();

        return voiceChannel;
    }

    public long getAssociatedChannel() {
        return this.associatedChannel;
    }

    public void setAssociatedChannel(long channelId) {
        this.associatedChannel = channelId;
    }
}
