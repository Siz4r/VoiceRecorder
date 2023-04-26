package com.example.voicerecorder;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.voicerecorder.databinding.RecorderBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class RecordFragment extends Fragment {
    private static final int PERMISSION_CODE = 21;
    private RecorderBinding binding;

    private boolean isRecording;
    private String recordPermission = Manifest.permission.RECORD_AUDIO;
    private MediaRecorder mediaRecorder;
    private Chronometer timer;
    private String recordFile;
    private TextView fileNameText;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = RecorderBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.listBtn.setOnClickListener(view1 -> {
            if (isRecording) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavHostFragment.findNavController(RecordFragment.this)
                                .navigate(R.id.action_RecordFragment_to_ListFragment);
                    }
                });
                alertDialog.setTitle("Audio Still Recording");
                alertDialog.setMessage("Are you sure you want to stop recording?");
                alertDialog.create().show();
            } else {
                NavHostFragment.findNavController(RecordFragment.this)
                        .navigate(R.id.action_RecordFragment_to_ListFragment);
            }
        });

        binding.recordBtn.setOnClickListener(view1 -> {
            if (isRecording) {
                stopRecording();
                binding.recordBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.microphone_icon, null));
            } else {
                if (checkPermissions()) {
                    startRecording();
                    binding.recordBtn.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.microphone_icon_stopped, null));
                }
            }
            isRecording = !isRecording;
        });

        this.timer = binding.recordTimer;
        this.fileNameText = binding.textView2;
    }

    private void stopRecording() {
        fileNameText.setText("Recording stopped, file saved: " + recordFile);
        mediaRecorder.stop();
        mediaRecorder.release();
        timer.stop();
    }

    private void startRecording() {
        timer.setBase(SystemClock.elapsedRealtime());
        timer.start();
        String recordPath = Objects.requireNonNull(getActivity()).
                getExternalFilesDir("/").getAbsolutePath();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss", Locale.GERMAN);
        recordFile = "filename" + formatter.format(new Date()) + ".3gp";

        fileNameText.setText("Recording, file name: " + recordFile);

        this.mediaRecorder = new MediaRecorder();
        this.mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(recordPath + "/" + recordFile);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }


    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(Objects.requireNonNull(getContext()),
                recordPermission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{recordPermission}, PERMISSION_CODE);
            return false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRecording) {
            stopRecording();
            isRecording = false;
        }
    }
}