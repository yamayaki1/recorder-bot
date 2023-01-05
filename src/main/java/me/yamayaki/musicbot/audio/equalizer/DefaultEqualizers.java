package me.yamayaki.musicbot.audio.equalizer;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;

public class DefaultEqualizers {
    public static EQFilter NONE;
    public static EQFilter BASS_BOOST;

    static {
        NONE = new NoBoost();
        BASS_BOOST = new BassBoost();
    }

    public interface EQFilter {
        void applyBands(EqualizerFactory equalizerFactory);
    }
}
