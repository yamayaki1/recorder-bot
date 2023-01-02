package me.yamayaki.musicbot.audio.equalizer;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;

public class BassBoost implements DefaultEqualizers.EQFilter {
    public static final float[] BASS_BOOST = {
            0.2f,
            0.15f,
            0.1f,
            0.05f,
            0.0f,
            -0.05f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f,
            -0.1f
    };

    @Override
    public void applyBands(EqualizerFactory equalizerFactory) {
        for (int i = 0; i < BASS_BOOST.length; i++) {
            equalizerFactory.setGain(i, BASS_BOOST[i] * 0.75f);
        }
    }
}
