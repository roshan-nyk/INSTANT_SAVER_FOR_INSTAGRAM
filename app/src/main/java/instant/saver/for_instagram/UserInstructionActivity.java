package instant.saver.for_instagram;

import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.insta_saver.R;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

public class UserInstructionActivity extends IntroActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setFullscreen(true);
        super.onCreate(savedInstanceState);
        // Add slides, edit configuration...
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ) {
            getWindow().setFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS, WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS);
//            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
        }

        autoplay(4000, INFINITE);


        addSlide(new SimpleSlide.Builder()
                .title("User Instruction\n  Option 1")
                .description("Open Instagram. First click on the three dots at the top right corner,then select \"Copy Link\" option and Open \"Instant Saver\" app")
                .image(R.drawable.usertutorial_copyto_ii)
                .background(R.color.black)
                .backgroundDark(R.color.white)
                .scrollable(false)
//                .permission(Manifest.permission.CAMERA)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Option 2\n  Step 1")
                .description("Open Instagram. First click on the three dots at the top right corner and then click on the \"Share To\" option.")
                .image(R.drawable.usertutorial_shareto_v)
                .background(R.color.black)
                .backgroundDark(R.color.white)
                .scrollable(false)
//                .permission(Manifest.permission.CAMERA)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Step 2\n")
                .description("Now click on the \"Instant Saver\" logo\n\n")
                .image(R.drawable.usertutorial_shareto_vi)
                .background(R.color.black)
                .backgroundDark(R.color.white)
                .scrollable(false)
//                .permission(Manifest.permission.CAMERA)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Option 3\n  Step 1")
                .description("Open \"Instant Saver\" app and  click on the Add User button\n")
                .image(R.drawable.usertutorial_bookmark_i)
                .background(R.color.black)
                .backgroundDark(R.color.white)
                .scrollable(false)
//                .permission(Manifest.permission.CAMERA)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("\n")
                .description("Now your screen should look like this,\n\n")
                .image(R.drawable.usertutorial_bookmark_ii)
                .background(R.color.black)
                .backgroundDark(R.color.white)
                .scrollable(false)
//                .permission(Manifest.permission.CAMERA)
                .build());

        addSlide(new SimpleSlide.Builder()
                .title("Step 2")
                .description("Open Instagram. First open the User Profile then click on the three dots at the top right corner,then select \"Copy Profile Url\" option and open \"Instant Saver\" app")
                .image(R.drawable.usertutorial_bookmark_iii)
                .background(R.color.black)
                .backgroundDark(R.color.white)
                .scrollable(false)
//                .permission(Manifest.permission.CAMERA)
                .build());
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().clearFlags(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS);
        }
        super.onBackPressed();
    }
}