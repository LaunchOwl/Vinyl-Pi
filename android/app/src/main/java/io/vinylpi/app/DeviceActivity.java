package io.vinylpi.app;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.Log;
import android.util.MalformedJsonException;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;

public class DeviceActivity extends AppCompatActivity {
    private static final String TAG = "DeviceActivity";
    final static String EXTRA_PI_DEVICE = "io.vinylpi.app.PI_DEVICE";
    final static String PI_AUDIO_PORT = "8000";
    final static String PI_STREAM_PATH = "pi.ogg";
    private MediaPlayer mediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Intent intent = getIntent();
        final PiDevice piDevice = intent.getParcelableExtra(EXTRA_PI_DEVICE);
        if (toolbar != null) {
            toolbar.setTitle(piDevice.getDeviceName());
        }
        setSupportActionBar(toolbar);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final ImageView playButton = (ImageView) findViewById(R.id.iv_play);
        final ImageView pauseButton = (ImageView) findViewById(R.id.iv_pause);
        final String ipAddress = piDevice.getIpAddress();
        final ProgressBar connecting = (ProgressBar) findViewById(R.id.pb_connecting);

        final SocketHelper socketHelper = new SocketHelper();
        socketHelper.setOnDataReceivedListener(new SocketHelper.OnDataReceivedListener() {
            @Override
            public void onDataReceived(final String message) {
                Log.d(TAG, message);

                try {
                    int eventId = 0;
                    int connections = 0;

                    JsonReader reader = new JsonReader(new StringReader(message));
                    reader.beginObject();
                    while (reader.hasNext()) {

                        String name = reader.nextName();
                        if (name.equals("eventId")) {
                            eventId = reader.nextInt();
                        } else if (name.equals("connections")) {
                            connections = reader.nextInt();
                        }
                    }
                    reader.endObject();

                    switch (eventId) {
                        case 0:
                            // Wait for socket connection before starting playback
                            mediaPlayer.start();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    connecting.setVisibility(View.GONE);
                                    //playButton.setImageResource(R.drawable.ic_pause_black_24dp);
                                    playButton.setVisibility(View.GONE);
                                    pauseButton.setVisibility(View.VISIBLE);
                                    //playButton.setTag("pause");
                                }
                            });
                            updateConnectionCount(connections);
                            break;
                        case 1:
                            updateConnectionCount(connections);
                    }



                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if (playButton != null && ipAddress != null) {
            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        playButton.setVisibility(View.GONE);
                        pauseButton.setVisibility(View.GONE);
                        connecting.setVisibility(View.VISIBLE);

                        String url = "http://" + ipAddress + ":" + PI_AUDIO_PORT + "/" + PI_STREAM_PATH;
                        Log.d(TAG, url);
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        //mediaPlayer.setVolume(1, 1);
                        mediaPlayer.setDataSource(url);

                        //AudioManager audioManager =(AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
                        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                            @Override
                            public void onPrepared(MediaPlayer mp) {
                                socketHelper.connect(ipAddress);
                            }
                        });
                        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                Log.d(TAG, "MediaPlayer Error " + what + " : " + extra);

                                connecting.setVisibility(View.GONE);
                                //playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                                pauseButton.setVisibility(View.GONE);
                                playButton.setVisibility(View.VISIBLE);
                                destroyMediaPlayer();

                                return false;
                            }
                        });
                        mediaPlayer.prepareAsync();

                    } catch (IllegalArgumentException e) {
                        connecting.setVisibility(View.GONE);
                        //playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        pauseButton.setVisibility(View.GONE);
                        playButton.setVisibility(View.VISIBLE);
                        destroyMediaPlayer();
                    } catch (IOException e) {
                        connecting.setVisibility(View.GONE);
                        //playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
                        pauseButton.setVisibility(View.GONE);
                        playButton.setVisibility(View.VISIBLE);
                        destroyMediaPlayer();
                    }
                }
            });
        }
    }

    private void destroyMediaPlayer () {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        final ImageView playButton = (ImageView) findViewById(R.id.iv_play);
        final ImageView pauseButton = (ImageView) findViewById(R.id.iv_pause);
        final ProgressBar connecting = (ProgressBar) findViewById(R.id.pb_connecting);
        connecting.setVisibility(View.GONE);
        // playButton.setImageResource(R.drawable.ic_play_arrow_black_24dp);
        pauseButton.setVisibility(View.GONE);
        playButton.setVisibility(View.VISIBLE);
    }

    private void updateConnectionCount(final int connectionCount) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView deviceCount = (TextView) findViewById(R.id.tv_device_count);
                deviceCount.setText(String.valueOf(connectionCount));
            }
        });
    }

    public void resetUI() {

    }

    @Override
    protected void onPause() {
        super.onPause();
        destroyMediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyMediaPlayer();
    }

}
