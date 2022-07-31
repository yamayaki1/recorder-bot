package me.jerriidesu.musicbot.audio;

import me.jerriidesu.musicbot.MusicBot;
import org.javacord.api.entity.server.Server;

import java.util.HashMap;
import java.util.List;

public class ServerManager {
    private final MusicBot bot;
    private final HashMap<Server, TrackManager> serverMap = new HashMap<>();

    public ServerManager(MusicBot bot) {
        this.bot = bot;
    }

    public TrackManager getTrackManager(Server server) {
        return this.serverMap.computeIfAbsent(server, TrackManager::new);
    }

    public void removeTrackManager(Server server) {
        this.serverMap.get(server).close();
        this.serverMap.remove(server);
    }

    public List<TrackManager> getAll() {
        return this.serverMap.values().stream().toList();
    }
}
