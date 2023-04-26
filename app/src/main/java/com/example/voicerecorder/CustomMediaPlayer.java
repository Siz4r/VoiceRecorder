package com.example.voicerecorder;

import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;

public class CustomMediaPlayer {
    private MediaPlayer mediaPlayer;
    public boolean isPlaying;
    private Handler seekBarHandler;
    private Runnable updateSeekBar;
    private final TextView editFileName;
    private final TextView playerHeader;
    private final SeekBar seekBar;
    private final ImageButton playBtn;
    private final BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior;
    private final Resources resources;

    public CustomMediaPlayer(TextView playerHeader,
                             SeekBar seekBar,
                             ImageButton playBtn,
                             BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior,
                             TextView editFileName, Resources resources) {
        this.playerHeader = playerHeader;
        this.seekBar = seekBar;
        this.playBtn = playBtn;
        this.bottomSheetBehavior = bottomSheetBehavior;
        this.editFileName = editFileName;
        this.resources = resources;
        initializeComponents();
    }

    private void initializeComponents() {
        this.bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    void stopAudio() {
        playBtn.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_play_arrow_black_20, null));
        playerHeader.setText("Stopped");
        isPlaying = false;
        mediaPlayer.pause();
    }

    void playAudio(File fileToPlay) {
        mediaPlayer = new MediaPlayer();
        isPlaying = true;
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        editFileName.setText(fileToPlay.getName());
        try {
            mediaPlayer.setDataSource(fileToPlay.getAbsolutePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        playBtn.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_pause_black_20, null));
        playerHeader.setText("Playing");
        editFileName.setText(fileToPlay.getName());
        mediaPlayer.start();
        System.out.println("cze");
        mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            stopAudio();
            playerHeader.setText("Finished");
        });

        seekBar.setMax(mediaPlayer.getDuration());
        seekBarHandler = new Handler();
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar, 0);
    }

    void pauseAudio() {
        mediaPlayer.pause();
        isPlaying = false;
        playBtn.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_play_arrow_black_20, null));
        playerHeader.setText("Stopped");
        seekBarHandler.removeCallbacks(updateSeekBar);
    }

    void resumeAudio() {
        mediaPlayer.start();
        isPlaying = true;
        playBtn.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.baseline_pause_black_20, null));
        playerHeader.setText("Playing");
        updateRunnable();
        seekBarHandler.postDelayed(updateSeekBar, 0);
    }

    private void updateRunnable() {
        updateSeekBar = new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                seekBarHandler.postDelayed(this, 500);
            }
        };
    }

    public void seekTo(int msec) {
        this.mediaPlayer.seekTo(msec);
    }
}
