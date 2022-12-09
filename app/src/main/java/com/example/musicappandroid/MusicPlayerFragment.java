package com.example.musicappandroid;

import static android.content.Context.BIND_AUTO_CREATE;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.musicappandroid.databinding.FragmentSecondBinding;

import java.util.ArrayList;

public class MusicPlayerFragment extends Fragment implements Runnable, ActionPlaying, ServiceConnection {
    private FragmentSecondBinding binding;

    private TextView title;
    private ImageButton playBtn;
    private SeekBar seekBar;
    private TextView musicDuration;
    private SwitchCompat repeatSwitch;

    private String musicInfo = "";
    private String path;

    private MediaSession ms;
    private MediaPlayer mp;
    private MediaSessionCompat mManagerCompat;
    private MusicService musicService;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private boolean m_playing;

    private MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
    private Bitmap songImage;
    private byte[] art;

    private int finalSec = -1;
    private int finalMin = -1;
    private int Sec = -1;
    private int Min = -1;

    private ArrayList<String> musicList;
    private int musicId;

    private float timerChangeMusic = 10f;
    private final float waitTime = 0.1f;

    public MusicPlayerFragment() {
        m_playing = true;
    }

    public MusicPlayerFragment(boolean a) {
        m_playing = a;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);

        MusicPlayerFragment fragment = null;

        if(getActivity() != null) {
            fragment = (MusicPlayerFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.listMusicPage);
        }

        if(fragment != null && fragment.getArguments() != null) {
            musicInfo = fragment.getArguments().getString("music_info");
            musicId = fragment.getArguments().getInt("posMusic");
            musicList = fragment.getArguments().getStringArrayList("listArr");
        }

        FragmentManager t = getActivity().getSupportFragmentManager();
        t.clearFragmentResult("music_info");
        path = musicInfo.split(": ")[1];
        musicInfo = musicInfo.split(": ")[0];

        mManagerCompat = new MediaSessionCompat(getActivity().getBaseContext(), "Compat_Sessions");
        ms = (MediaSession) mManagerCompat.getMediaSession();
        mp = new MediaPlayer();

        try{
            mp.setDataSource(path);
            mp.prepare();
        }catch(Exception e){e.printStackTrace();}

        if(m_playing){
            Intent pauseIntent = new Intent(getContext(), MusicService.class);
            getActivity().bindService(pauseIntent, MusicPlayerFragment.this, BIND_AUTO_CREATE);
        }

        CreateNotification();

        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

        Thread t = new Thread(this);

        if(getView() != null) {
            musicDuration = getView().findViewById(R.id.timeMusic);
        }

        finalMin = mp.getDuration() / 1000 / 60;
        finalSec = (mp.getDuration() / 1000) % 60;

        String info;

        if(finalSec < 10) {
             info = "0:00 / " + finalMin + ":0" + finalSec;
        } else {
            info = "0:00 / " + finalMin + ":" + finalSec;
        }

        musicDuration.setText(info);

        title = getView().findViewById(R.id.titleMusicTxt);
        title.setText(musicInfo);

        playBtn = getView().findViewById(R.id.playPauseBtn);

        metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(path);
        try {
            art = metaRetriever.getEmbeddedPicture();
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 2;
            songImage = BitmapFactory.decodeByteArray(art, 0, art.length,opt);
        }
        catch (Exception e)
        {
            songImage = null;
        }

        if(songImage != null) {
            BitmapDrawable ob = new BitmapDrawable(getResources(), songImage);
            getView().findViewById(R.id.albumSongImageView).setBackground(ob);
        }

        if(m_playing) {
            mp.start();
            playBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_button, null));
        }

        playBtn.setOnClickListener(view1 -> {
            timerChangeMusic = 10f;
            if(m_playing) {
                m_playing = false;
                mp.pause();
                playBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.play_button, null));

            } else {
                m_playing = true;
                mp.start();
                playBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_button, null));
            }
        });

        seekBar = getView().findViewById(R.id.Duration_SseekBar);
        seekBar.setMax(mp.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                Min = seekBar.getProgress() / 1000 / 60;
                Sec = (seekBar.getProgress() / 1000) % 60;
                String info;

                if(Sec < 10) {
                    if(finalSec < 10) {
                        info = "" + Min + ":0" + Sec + " / " + finalMin + ":0" + finalSec;
                    } else {
                        info = "" + Min + ":0" + Sec + " / " + finalMin + ":" + finalSec;
                    }
                }
                else {
                    if(finalSec < 10) {
                        info = "" + Min + ":" + Sec + " / " + finalMin + ":0" + finalSec;
                    } else {
                        info = "" + Min + ":" + Sec + " / " + finalMin + ":" + finalSec;
                    }
                }

                musicDuration.setText(info);
                timerChangeMusic = 10f;

                if(fromUser) {
                    mp.seekTo(seekBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        seekBar.setOnTouchListener((v, event) -> {
            Min = seekBar.getProgress() / 1000 / 60;
            Sec = (seekBar.getProgress() / 1000) % 60;

            String infoTime;

            if(Sec < 10) {
                if(finalSec < 10) {
                    infoTime = "" + Min + ":0" + Sec + " / " + finalMin + ":0" + finalSec;
                } else {
                    infoTime = "" + Min + ":0" + Sec + " / " + finalMin + ":" + finalSec;
                }
            }
            else {
                if(finalSec < 10) {
                    infoTime = "" + Min + ":" + Sec + " / " + finalMin + ":0" + finalSec;
                } else {
                    infoTime = "" + Min + ":" + Sec + " / " + finalMin + ":" + finalSec;
                }
            }

            timerChangeMusic = 10f;
            musicDuration.setText(infoTime);
            mp.seekTo(seekBar.getProgress());

            return view.performClick();
        });

        repeatSwitch = getView().findViewById(R.id.repeatSwitchCompat);
        repeatSwitch.setOnClickListener(v -> {
            mp.setLooping(repeatSwitch.isChecked());

            if(repeatSwitch.isChecked() && mp.getCurrentPosition() == mp.getDuration()) {
                mp.seekTo(0);
                seekBar.setProgress(0);

                String infoMusic;

                if(finalSec < 10) {
                    infoMusic = "0:00 / " + finalMin + ":0" + finalSec;
                } else {
                    infoMusic = "0:00 / " + finalMin + ":" + finalSec;
                }

                musicDuration.setText(infoMusic);
                timerChangeMusic = 10f;

                mp.start();
                playBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_button, null));
            }
        });

        ImageButton previousBtn = getView().findViewById(R.id.backBtnImage);
        previousBtn.setOnClickListener(v -> previousAction());

        ImageButton nextBtn = getView().findViewById(R.id.nextBtnImage);
        nextBtn.setOnClickListener(v -> nextAction());

        t.start();

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();

        if(mBuilder == null) {
            CreateNotification();
        }
    }

    private void CreateNotification() {
        Intent pauseIntent = new Intent(getContext(), MusicService.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(getContext(), 0, pauseIntent, PendingIntent.FLAG_MUTABLE);
        pauseIntent.setAction("PAUSE");
        pauseIntent.putExtra("ACTION", "PAUSE_ACTION");
        pauseIntent.setAction("PAUSE");

        Intent previousBtnIntent = new Intent(getContext(), NotificationReceiver.class).setAction("PREVIOUS");
        previousBtnIntent.putExtra("ACTION", "PREVIOUS_ACTION");
        PendingIntent.getBroadcast(getContext(), 0, previousBtnIntent, PendingIntent.FLAG_MUTABLE);

        Intent pauseBtnIntent = new Intent(getContext(), NotificationReceiver.class).setAction("PAUSE");
        pauseBtnIntent.putExtra("ACTION", "PAUSE_ACTION");
        PendingIntent.getBroadcast(getContext(), 0, pauseBtnIntent, PendingIntent.FLAG_MUTABLE);

        Intent playBtnIntent = new Intent(getContext(), NotificationReceiver.class).setAction("PLAY");
        playBtnIntent.putExtra("ACTION", "PLAY_ACTION");
        PendingIntent.getBroadcast(getContext(), 0, playBtnIntent, PendingIntent.FLAG_MUTABLE);

        Intent nextBtnIntent = new Intent(getContext(), NotificationReceiver.class).setAction("NEXT");
        nextBtnIntent.putExtra("ACTION", "NEXT_ACTION");
        PendingIntent.getBroadcast(getContext(), 0, nextBtnIntent, PendingIntent.FLAG_MUTABLE);

        PendingIntent ButtonPreviousScreen = PendingIntent.getBroadcast(getContext(), 12345, previousBtnIntent, PendingIntent.FLAG_MUTABLE);
        PendingIntent ButtonPauseScreen = PendingIntent.getBroadcast(getContext(), 12345, pauseBtnIntent, PendingIntent.FLAG_MUTABLE);
        PendingIntent ButtonPlayScreen = PendingIntent.getBroadcast(getContext(), 12345, playBtnIntent, PendingIntent.FLAG_MUTABLE);
        PendingIntent ButtonNextScreen = PendingIntent.getBroadcast(getContext(), 12345, nextBtnIntent, PendingIntent.FLAG_MUTABLE);

        ms.setPlaybackState(new PlaybackState.Builder()
                .setState(PlaybackState.STATE_PLAYING, mp.getCurrentPosition(), 1)
                .setActions(PlaybackState.ACTION_SEEK_TO)
                .build()
        );

        if(getContext() != null) {
            mBuilder = new NotificationCompat.Builder(getContext(), "i.apps.notifications");
            mBuilder.setSmallIcon(R.drawable.icon);
            mBuilder
                    .addAction(android.R.drawable.ic_media_previous, "<<", ButtonPreviousScreen)
                    .addAction(android.R.drawable.ic_media_pause, "Pause", ButtonPauseScreen)
                    .addAction(android.R.drawable.ic_media_play, "Play", ButtonPlayScreen)
                    .addAction(android.R.drawable.ic_media_next, ">>", ButtonNextScreen)
                    .setContentTitle("Music App")
                    .setContentText("Playing : " + musicInfo)
                    .setCategory(NotificationCompat.CATEGORY_EVENT)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setMediaSession(mManagerCompat.getSessionToken())
                            .setShowActionsInCompactView(0, 1, 3))
                    .setAutoCancel(false)
                    .setOnlyAlertOnce(false)
                    .setSilent(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setContentIntent(resultPendingIntent);

            if (songImage != null) {
                mBuilder.setLargeIcon(songImage);
            } else {
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.temp_bg));
            }

            mNotificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            assert mNotificationManager != null;
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            mNotificationManager.createNotificationChannel(notificationChannel);

            assert mNotificationManager != null;
            mNotificationManager.notify(0, mBuilder.build());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if(mp != null) {
            mp.pause();
            mp.stop();
            mp = null;
        }

        if(binding != null) {
            binding = null;
        }
    }

    @Override
    public void run() {
        int currentPosition = mp.getCurrentPosition();

        Min = currentPosition / 1000 / 60;
        Sec = (currentPosition / 1000) % 60;

        while (mp != null) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            timerChangeMusic += 0.1f;

            if(mp != null)
                currentPosition = mp.getCurrentPosition();
            else {
                mNotificationManager.cancel(0);
                return;
            }

            Min = currentPosition / 1000 / 60;
            Sec = (currentPosition / 1000) % 60;

            String infoMusic;

            if(Sec < 10) {
                if(finalSec < 10) {
                    infoMusic = "" + Min + ":0" + Sec + " / " + finalMin + ":0" + finalSec;
                } else {
                    infoMusic = "" + Min + ":0" + Sec + " / " + finalMin + ":" + finalSec;
                }
            }
            else {
                if(finalSec < 10) {
                    infoMusic = "" + Min + ":" + Sec + " / " + finalMin + ":0" + finalSec;
                } else {
                    infoMusic = "" + Min + ":" + Sec + " / " + finalMin + ":" + finalSec;
                }
            }

            musicDuration.postDelayed(() -> musicDuration.setText(infoMusic), 10);
            seekBar.setProgress(currentPosition);

            if(!repeatSwitch.isChecked() && mp.getCurrentPosition() >= mp.getDuration()) {
                Log.d("MusicId", "S " + musicId);
                mp.stop();
                musicId++;
                seekBar.setProgress(0);

                if (musicId == musicList.size()) {
                    musicId = 0;
                }

                path = musicList.get(musicId).split(": ")[1];
                musicInfo = musicList.get(musicId).split(": ")[0];
                mp = new MediaPlayer();
                try {
                    mp.setDataSource(path);
                    mp.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                title.postDelayed(() -> title.setText(musicInfo), 10);

                if(getView()!= null) {
                    playBtn.postDelayed(() -> playBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_button, null)), 10);
                }

                metaRetriever.setDataSource(path);
                try {
                    art = metaRetriever.getEmbeddedPicture();
                    BitmapFactory.Options opt = new BitmapFactory.Options();
                    opt.inSampleSize = 2;
                    songImage = BitmapFactory.decodeByteArray(art, 0, art.length, opt);
                } catch (Exception e) {
                    songImage = null;
                }

                if (songImage != null) {
                    BitmapDrawable ob = new BitmapDrawable(getResources(), songImage);
                    getView().findViewById(R.id.albumSongImageView).postDelayed(() ->  getView().findViewById(R.id.albumSongImageView).setBackground(ob), 10);
                } else {
                    getView().findViewById(R.id.albumSongImageView).postDelayed(() ->
                            getView().findViewById(R.id.albumSongImageView).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.temp_bg, null)), 10);
                }
                seekBar.setMax(mp.getDuration());
                finalMin = mp.getDuration() / 1000 / 60;
                finalSec = (mp.getDuration() / 1000) % 60;
                String info;

                if (finalSec < 10) {
                    info = "0:00 / " + finalMin + ":0" + finalSec;
                } else {
                    info = "0:00 / " + finalMin + ":" + finalSec;
                }

                if(mBuilder != null && mNotificationManager != null) {
                    mBuilder.setContentText("Playing : " + musicInfo);
                    if(songImage != null) {
                        mBuilder.setLargeIcon(songImage);
                    } else {
                        mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.temp_bg));
                    }
                    mNotificationManager.notify(0, mBuilder.build());
                }

                musicDuration.postDelayed(() -> musicDuration.setText(info), 10);
                Log.d("MusicId", "E " + musicId);
                mp.start();
            }
        }

        timerChangeMusic = 10f;
    }

    @Override
    public void playAction() {
        m_playing = true;
        mp.start();

        if(getView()!= null) {
            playBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_button, null));
        }
    }

    @Override
    public void pauseAction() {
        m_playing = false;
        mp.pause();

        if(getView()!= null) {
            playBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.play_button, null));
        }
    }

    @Override
    public void nextAction() {
        if(timerChangeMusic < waitTime) {
            return;
        }

        timerChangeMusic = 0;

        musicId++;
        ChangeMusic();
    }

    @Override
    public void previousAction() {
        if(timerChangeMusic < waitTime) {
            return;
        }

        timerChangeMusic = 0;

        musicId--;
        ChangeMusic();
    }

    private void ChangeMusic() {
        mp.stop();
        seekBar.setProgress(0);

        if(musicId == musicList.size()) {
            musicId = 0;
        }

        if(musicId == -1) {
            musicId = musicList.size()-1;
        }

        path = musicList.get(musicId).split(": ")[1];
        musicInfo = musicList.get(musicId).split(": ")[0];
        mp = new MediaPlayer();

        try{
            mp.setDataSource(path);
            mp.prepare();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        seekBar.setMax(mp.getDuration());

        title.setText(musicInfo);
        mp.start();
        playBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.pause_button, null));
        metaRetriever.setDataSource(path);

        try {
            art = metaRetriever.getEmbeddedPicture();
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 2;
            songImage = BitmapFactory.decodeByteArray(art, 0, art.length,opt);
        }
        catch (Exception e)
        {
            songImage = null;
        }

        if(songImage != null && getView() != null) {
            BitmapDrawable ob = new BitmapDrawable(getResources(), songImage);
            getView().findViewById(R.id.albumSongImageView).setBackground(ob);
        }else if(getView() != null) {
            getView().findViewById(R.id.albumSongImageView).setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.temp_bg, null));
        }

        finalMin = mp.getDuration() / 1000 / 60;
        finalSec = (mp.getDuration() / 1000) % 60;

        String infoMusic;

        if(finalSec < 10) {
            infoMusic = "0:00 / " + finalMin + ":0" + finalSec;
        } else {
            infoMusic = "0:00 / " + finalMin + ":" + finalSec;
        }

        musicDuration.setText(infoMusic);

        if(mBuilder != null && mNotificationManager != null) {
            mBuilder.setContentText("Playing : " + musicInfo);
            if(songImage != null) {
                mBuilder.setLargeIcon(songImage);
            } else {
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.temp_bg));
            }
            mNotificationManager.notify(0, mBuilder.build());
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder) service;
        musicService = binder.getService();
        musicService.setCallback(MusicPlayerFragment.this);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        ServiceConnection.super.onBindingDied(name);
    }

    @Override
    public void onNullBinding(ComponentName name) {
        ServiceConnection.super.onNullBinding(name);
    }
}