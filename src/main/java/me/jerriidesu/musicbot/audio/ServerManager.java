package me.jerriidesu.musicbot.audio;

import org.javacord.api.entity.server.Server;

import java.util.HashMap;
import java.util.List;

public class ServerManager {
    private final HashMap<Server, TrackManager> serverMap;

    public ServerManager() {
        this.serverMap = new HashMap<>();
    }

    public TrackManager getTrackManager(Server server) {
        return this.serverMap.computeIfAbsent(server, TrackManager::new);
    }

    public void removeTrackManager(Server server) {
        this.serverMap.get(server).shutdown();
        this.serverMap.remove(server);
    }

    public List<TrackManager> getAll() {
        return this.serverMap.values().stream().toList();
    }

    public void shutdown() {
        for (TrackManager trackManager : serverMap.values()) {
            trackManager.shutdown();
        }
    }
}
