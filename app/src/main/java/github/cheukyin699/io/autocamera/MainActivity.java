package github.cheukyin699.io.autocamera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    SurfaceView surface;
    Button lapseToggle;
    EditText lapseText;
    EditText filePrefix;

    boolean isLapsing;
    long lapseNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surface = findViewById(R.id.surfaceView);
        lapseToggle = findViewById(R.id.lapseBt);
        lapseText = findViewById(R.id.lapseDelta);
        filePrefix = findViewById(R.id.filePrefix);
        isLapsing = false;
        lapseNumber = 0;

        lapseToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final long delay = Long.valueOf(lapseText.getText().toString());
                isLapsing = !isLapsing;

                if (isLapsing) {
                    // Set up threading
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            super.run();

                            try {
                                Camera c = Camera.open();
                                c.setPreviewDisplay(surface.getHolder());
                                c.startPreview();

                                // Takes lots of pics!
                                while (isLapsing) {
                                    String prefix = filePrefix.getText().toString();
                                    c.takePicture(
                                            null,
                                            null,
                                            new SavePictureCallback(prefix, lapseNumber++)
                                    );
                                    c.startPreview();
                                    sleep(delay);
                                }

                                c.stopPreview();
                                c.release();
                            } catch (Exception e) {
                                Log.e("CAMERA", e.getMessage());
                            }
                        }
                    };
                    t.start();
                }
                lapseText.setEnabled(!isLapsing);
                filePrefix.setEnabled(!isLapsing);
                lapseToggle.setText(isLapsing ? R.string.stop : R.string.start);
            }
        });
    }

    private class SavePictureCallback implements Camera.PictureCallback {
        private String prefix;
        private long number;

        SavePictureCallback(String p, long n) {
            prefix = p;
            number = n;
        }

        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File img = new File(dir, prefix + number + ".jpg");

            try {
                FileOutputStream of = new FileOutputStream(img);
                of.write(bytes);
                of.close();
            } catch (Exception e) {
                Log.e("FILE", e.getMessage());
            }
        }
    }
}
