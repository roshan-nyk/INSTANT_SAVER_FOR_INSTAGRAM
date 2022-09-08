package instant.saver.for_instagram;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;



import java.util.Objects;

public class VideoActivity extends AppCompatActivity {

    private String videoUrl;
    private String pathName;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Activity activity = this;
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        activity.getWindow().setStatusBarColor(Color.BLACK);

        Objects.requireNonNull(getSupportActionBar()).hide();

        Intent intent = getIntent();
        videoUrl = intent.getStringExtra("Video_Url");
        pathName = intent.getStringExtra("PATH_NAME");

        videoView = findViewById(R.id.VideoView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView = null;
        videoUrl = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoView.setMediaController(new MediaController(this));
        if(videoUrl != null)
            videoView.setVideoURI(Uri.parse(videoUrl));
        else
            videoView.setVideoPath(pathName);
        videoView.start();
        videoView.setOnCompletionListener(mp -> onBackPressed());
    }
}