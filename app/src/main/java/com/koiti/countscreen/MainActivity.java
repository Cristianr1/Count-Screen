package com.koiti.countscreen;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView textCount;
    TextView textIp;
    ServerSocket serverSocket;
    ConnectionParameters socketHandle;
    Videos videos;
    VideoView videoView;
    int video_index = 0;

    ArrayList<Uri> videosArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        videoView = findViewById(R.id.video_view);
        textIp = findViewById(R.id.textview_ip);
        textCount = findViewById(R.id.textview_count);

        socketHandle = new ConnectionParameters(this);
        ContentResolver contentResolver = getContentResolver();
        videos = new Videos(contentResolver);
        videos.getVideos();
        videosArrayList = videos.getVideosArrayList();

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                video_index++;
                if (video_index < (videosArrayList.size())) {
                    videoView.setVideoURI(videosArrayList.get(video_index));
                    videoView.start();
                } else {
                    video_index = 0;
                    videoView.setVideoURI(videosArrayList.get(video_index));
                    videoView.start();
                }
            }
        });

        if(videosArrayList.size()>0) {
            videoView.setVideoURI(videosArrayList.get(0));
            videoView.start();
        }
//        if (videosArrayList.size() <= 1)
//            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//                    mp.setLooping(true);
//                }
//            });

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        new Thread(new ThreadMessageReceived()).start();
    }

    class ThreadMessageReceived implements Runnable {
        private volatile boolean exit = false;
        private BufferedReader input;

        @Override
        public void run() {
            Socket socket;
            try {
                serverSocket = new ServerSocket(ConnectionParameters.SERVER_PORT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textIp.setVisibility(View.VISIBLE);
                        textIp.setText(socketHandle.getServerIp());
                    }
                });

                try {
                    socket = serverSocket.accept();
                    input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textIp.setVisibility(View.INVISIBLE);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                while (!exit) {
                    final StringBuilder sbMessage = new StringBuilder("Cupo Disponible: ");
                    try {
                        final String message = input.readLine();
                        if (message != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textCount.setText(sbMessage.append(message));
                                }
                            });
                        } else {
                            exit = true;
                            try {
                                serverSocket.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            new Thread(new ThreadMessageReceived()).start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
