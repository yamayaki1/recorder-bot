package me.yamayaki.musicbot.audio.equalizer;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;

public class DefaultEqualizers {
    public static EQFilter NONE;
    public static EQFilter BASS_BOOST;
    public static EQFilter EAR_RAPE;

    static {
        NONE = new NoBoost();
        BASS_BOOST = new BassBoost();
        EAR_RAPE = new XTremeBassEarrapeBoost();
    }

    public interface EQFilter {
        void applyBands(EqualizerFactory equalizerFactory);
    }
}
