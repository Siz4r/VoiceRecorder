package com.example.voicerecorder;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voicerecorder.databinding.AudiosListBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListFragment extends Fragment implements AudioListAdapter.OnItemListClick {
    private AudiosListBinding binding;
    private List<File> records;
    private CustomMediaPlayer customMediaPlayer;
    private File fileToPlay;
    private TextView editFileName;
    private SeekBar seekBar;
    private Integer position;
    private String editedFileName;
    private ImageButton playBtn;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = AudiosListBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.audioListView.setOnClickListener(view1 -> NavHostFragment.findNavController(ListFragment.this)
                .navigate(R.id.action_ListFragment_to_RecordFragment));

        ConstraintLayout playerSheet = binding.getRoot().findViewById(R.id.player_sheet);

        this.editFileName = playerSheet.findViewById(R.id.editFileName);
        this.playBtn = playerSheet.findViewById(R.id.header_player_btn);
        this.seekBar = playerSheet.findViewById(R.id.seek_bar);

        TextView playerHeader = playerSheet.findViewById(R.id.header_title);
        BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior = BottomSheetBehavior.from(playerSheet);

        readRecordsDir();
        initializeListOfRecords();

        this.customMediaPlayer = new CustomMediaPlayer(playerHeader,
                seekBar,
                playBtn,
                bottomSheetBehavior,
                editFileName, getResources());

        initializePlayerSheetComponents(playerSheet);
    }

    private void readRecordsDir() {
        String path = Objects.requireNonNull(getActivity())
                .getExternalFilesDir("/")
                .getAbsolutePath();

        File directory = new File(path);
        this.records = new ArrayList<>(Arrays.asList(Objects.requireNonNull(directory.listFiles())));
    }

    private void initializePlayerSheetComponents(ConstraintLayout playerSheet) {
        editFileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                editedFileName = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                File editedFile = new File(fileToPlay.getParent() + "/" + editedFileName);
                if (!editedFile.exists() && fileToPlay.renameTo(editedFile)) {
                    fileToPlay = editedFile;
                    records.set(position, editedFile);
                    initializeListOfRecords();
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (fileToPlay != null) {
                    customMediaPlayer.pauseAudio();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (fileToPlay != null) {
                    int progress = seekBar.getProgress();
                    customMediaPlayer.seekTo(progress);
                    customMediaPlayer.resumeAudio();
                }
            }
        });

        playerSheet.findViewById(R.id.forward_btn).setOnClickListener(view1 -> {
            if (fileToPlay != null && position < records.size() - 1) {
                onClickListener(records.get(position + 1), position + 1);
            }
        });

        playerSheet.findViewById(R.id.backward_btn).setOnClickListener(view1 -> {
            if (fileToPlay != null && position > 0) {
                onClickListener(records.get(position - 1), position - 1);
            }
        });

        playerSheet.findViewById(R.id.delete_btn).setOnClickListener(view1 -> {
            customMediaPlayer.stopAudio();
            if (fileToPlay != null && fileToPlay.delete()) {
                    this.records = this.records
                            .stream()
                            .filter(File::exists)
                            .collect(Collectors.toList());

                    initializeListOfRecords();
                    if (position < records.size()) onClickListener(records.get(position), position);

            }


        });

        playBtn.setOnClickListener(view1 -> {
            if (customMediaPlayer.isPlaying) {
                customMediaPlayer.pauseAudio();
            } else {
                if (fileToPlay != null) {
                    customMediaPlayer.resumeAudio();
                }
            }
        });
    }

    private void initializeListOfRecords() {
        RecyclerView audioList = binding.audioListView;
        AudioListAdapter audioListAdapter = new AudioListAdapter(records, this);
        audioList.setHasFixedSize(true);
        audioList.setLayoutManager(new LinearLayoutManager(getContext()));
        audioList.setAdapter(audioListAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClickListener(File file, int position) {
        Log.d("PLAY LOG", "File playing " + file.getName());
        this.position = position;
        if (customMediaPlayer.isPlaying) {
            customMediaPlayer.stopAudio();
            fileToPlay = file;
            customMediaPlayer.playAudio(fileToPlay);
        } else {
            fileToPlay = file;
            customMediaPlayer.playAudio(fileToPlay);
        }
    }



    @Override
    public void onStop() {
        super.onStop();
        if (customMediaPlayer.isPlaying) {
            customMediaPlayer.stopAudio();
        }
    }
}