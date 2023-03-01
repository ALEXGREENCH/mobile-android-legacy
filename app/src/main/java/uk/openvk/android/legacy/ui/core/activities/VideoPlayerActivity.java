package uk.openvk.android.legacy.ui.core.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

import uk.openvk.android.legacy.R;
import uk.openvk.android.legacy.ui.OvkAlertDialog;
import wseemann.media.FFmpegMediaPlayer;
import uk.openvk.android.legacy.api.attachments.VideoAttachment;

/**
 * File created by Dmitry on 14.02.2023.
 */

@SuppressWarnings("deprecation")
public class VideoPlayerActivity extends Activity {
    private VideoAttachment video;
    private String url;
    private MediaController mediaCtrl;
    private VideoView video_view;
    private MediaPlayer mp;
    private FFmpegMediaPlayer fmp;
    private boolean ready;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        loadVideo();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().hide();
        }
    }

    private void loadVideo() {
        Bundle data = getIntent().getExtras();
        showPlayControls();
        findViewById(R.id.video_surface_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlayControls();
            }
        });
        if(data != null) {
            if(data.containsKey("attachment")) {
                video = data.getParcelable("attachment");
                assert video != null;
            } if(data.containsKey("files")) {
                video.files = data.getParcelable("files");
                assert video.files != null;
                if(video.files.ogv_480 != null && video.files.ogv_480.length() > 0) {
                    url = video.files.ogv_480;
                } if(video.files.mp4_144 != null && video.files.mp4_144.length() > 0) {
                    url = video.files.mp4_144;
                } if(video.files.mp4_240 != null && video.files.mp4_240.length() > 0) {
                    url = video.files.mp4_240;
                } if(video.files.mp4_360 != null && video.files.mp4_360.length() > 0) {
                    url = video.files.mp4_360;
                } if(video.files.mp4_480 != null && video.files.mp4_480.length() > 0) {
                    url = video.files.mp4_480;
                } if(video.files.mp4_720 != null && video.files.mp4_720.length() > 0) {
                    url = video.files.mp4_720;
                } if(video.files.mp4_1080 != null && video.files.mp4_1080.length() > 0) {
                    url = video.files.mp4_1080;
                }

                if(url == null) {
                    url = "";
                }

                Uri uri = Uri.parse(url);

                createMediaPlayer(uri);
                ((ImageButton) findViewById(R.id.video_btn)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playVideo();
                    }
                });
            }
        } else {
            finish();
        }
    }

    private void createMediaPlayer(Uri uri) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            fmp = new FFmpegMediaPlayer();
            fmp.setOnPreparedListener(new FFmpegMediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(FFmpegMediaPlayer mp) {
                    ready = true;
                    findViewById(R.id.video_progress_wrap).setVisibility(View.GONE);
                    SurfaceView vsv = findViewById(R.id.video_surface_view);
                    SurfaceHolder vsh = vsv.getHolder();
                    rescaleVideo(vsv, vsh);
                    vsh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    mp.setDisplay(vsh);
                    mp.start();
                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            updateControlPanel();
                            new Handler(Looper.myLooper()).postDelayed(this, 200);
                        }
                    });
                }
            });
            fmp.setOnErrorListener(new FFmpegMediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(FFmpegMediaPlayer mp, int what, int extra) {
                    fmp.release();
                    OvkAlertDialog err_dlg;
                    err_dlg = new OvkAlertDialog(VideoPlayerActivity.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayerActivity.this);
                    builder.setMessage(R.string.error);
                    builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    builder.setPositiveButton(R.string.retry_short, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent tp_player = new Intent(Intent.ACTION_VIEW);
                            tp_player.setDataAndType(Uri.parse(VideoPlayerActivity.this.url), "video/*");
                            startActivity(tp_player);
                            finish();
                        }
                    });
                    err_dlg.build(builder, getResources().getString(R.string.error), getResources().getString(R.string.video_err_decode), null);
                    err_dlg.show();
                    return false;
                }
            });

            try {
                fmp.setDataSource(this, uri);
                fmp.prepareAsync();
            } catch (IllegalArgumentException | IOException | IllegalStateException | SecurityException e) {
                e.printStackTrace();
            }
        } else {
            mp = new MediaPlayer();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    ready = true;
                    findViewById(R.id.video_progress_wrap).setVisibility(View.GONE);
                    SurfaceView vsv = VideoPlayerActivity.this.findViewById(R.id.video_surface_view);
                    SurfaceHolder vsh = vsv.getHolder();
                    rescaleVideo(vsv, vsh);
                    vsh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    mp.setDisplay(vsh);
                    mp.start();
                    new Handler(Looper.myLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            updateControlPanel();
                            new Handler(Looper.myLooper()).postDelayed(this, 200);
                        }
                    });
                }
            });
            mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mp.release();
                    OvkAlertDialog err_dlg;
                    err_dlg = new OvkAlertDialog(VideoPlayerActivity.this);
                    AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayerActivity.this);
                    builder.setMessage(R.string.error);
                    builder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    builder.setPositiveButton(R.string.retry_short, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent tp_player = new Intent(Intent.ACTION_VIEW);
                            tp_player.setDataAndType(Uri.parse(VideoPlayerActivity.this.url), "video/*");
                            startActivity(tp_player);
                            finish();
                        }
                    });
                    err_dlg.build(builder, getResources().getString(R.string.error), getResources().getString(R.string.video_err_decode), null);
                    err_dlg.show();
                    return false;
                }
            });

            try {
                mp.setDataSource(this, uri);
                mp.prepareAsync();
            } catch (IllegalArgumentException | IOException | IllegalStateException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void rescaleVideo(SurfaceView vsv, SurfaceHolder vsh) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            vsh.setFixedSize(fmp.getVideoWidth(), fmp.getVideoHeight());
            // Get the width of the frame
            int videoWidth = fmp.getVideoWidth();
            int videoHeight = fmp.getVideoHeight();
            float videoProportion = (float) videoWidth / (float) videoHeight;

            // Get the width of the screen
            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
            int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
            float screenProportion = (float) screenWidth / (float) screenHeight;

            android.view.ViewGroup.LayoutParams lp = vsv.getLayoutParams();
            if (videoProportion > screenProportion) {
                lp.width = screenWidth;
                lp.height = (int) ((float) screenWidth / videoProportion);
            } else {
                lp.width = (int) (videoProportion * (float) screenHeight);
                lp.height = screenHeight;
            }
            vsv.setLayoutParams(lp);
        } else {
            vsh.setFixedSize(mp.getVideoWidth(), mp.getVideoHeight());
            // Get the width of the frame
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();
            float videoProportion = (float) videoWidth / (float) videoHeight;

            // Get the width of the screen
            int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
            int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
            float screenProportion = (float) screenWidth / (float) screenHeight;

            android.view.ViewGroup.LayoutParams lp = vsv.getLayoutParams();
            if (videoProportion > screenProportion) {
                lp.width = screenWidth;
                lp.height = (int) ((float) screenWidth / videoProportion);
            } else {
                lp.width = (int) (videoProportion * (float) screenHeight);
                lp.height = screenHeight;
            }
            vsv.setLayoutParams(lp);
        }
    }

    public void showPlayControls() {
        findViewById(R.id.video_bottombar).setVisibility(View.VISIBLE);
        new Handler(Looper.myLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.video_bottombar).setVisibility(View.GONE);
            }
        }, 5000);
    }

    private void playVideo() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                if (isPlaying()) {
                    fmp.pause();
                } else {
                    fmp.start();
                }
            } else {
                if (isPlaying()) {
                    mp.pause();
                } else {
                    mp.start();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isPlaying() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                return fmp.isPlaying();
            } else {
                return mp.isPlaying();
            }
        } catch (Exception ex) {
            return false;
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateControlPanel() {
        if(ready) {
            int pos = 0;
            int duration = 0;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    pos = fmp.getCurrentPosition() / 1000;
                    duration = fmp.getDuration() / 1000;
                } else {
                    pos = mp.getCurrentPosition() / 1000;
                    duration = mp.getDuration() / 1000;
                }
            } catch (Exception ignored) {
            }
            if (isPlaying()) {
                ((TextView) findViewById(R.id.video_time1)).setText(String.format("%d:%02d", pos / 60, pos % 60));
                ((TextView) findViewById(R.id.video_time2)).setText(String.format("%d:%02d", duration / 60, duration % 60));
                if(!((SeekBar) findViewById(R.id.video_seekbar)).isFocused()) {
                    ((SeekBar) findViewById(R.id.video_seekbar)).setProgress(pos);
                    ((SeekBar) findViewById(R.id.video_seekbar)).setMax(duration);
                }
                ((ImageButton) findViewById(R.id.video_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_video_pause));
            } else {
                ((ImageButton) findViewById(R.id.video_btn)).setImageDrawable(getResources().getDrawable(R.drawable.ic_video_play));
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SurfaceView vsv = VideoPlayerActivity.this.findViewById(R.id.video_surface_view);
        SurfaceHolder vsh = vsv.getHolder();
        rescaleVideo(vsv, vsh);
    }
}