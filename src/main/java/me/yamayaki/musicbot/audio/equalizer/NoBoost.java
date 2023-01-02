package me.yamayaki.musicbot.audio.equalizer;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;

public class NoBoost implements DefaultEqualizers.EQFilter {
    @Override
    public void applyBands(EqualizerFactory equalizerFactory) {
        for (int i = 0; i < 15; i++) {
            equalizerFactory.setGain(i, 0);
        }
    }
}
