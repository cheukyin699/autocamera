package github.cheukyin699.io.autocamera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends Activity {

    SurfaceView surface;
    Button lapseToggle;
    EditText lapseText;

    boolean isLapsing;
    long delay;
    long lapseNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surface = findViewById(R.id.surfaceView);
        lapseToggle = findViewById(R.id.lapseBt);
        lapseText = findViewById(R.id.lapseDelta);
        isLapsing = false;
        delay = 1000;
        lapseNumber = 0;

        lapseText.setText(Double.toString(delay));

        lapseText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    delay = Long.valueOf(editable.toString());
                } catch (Exception e) {
                    Log.d("LONG", e.getMessage());
                }
            }
        });

        lapseToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                                    Camera.PictureCallback callback = new Camera.PictureCallback() {
                                        @Override
                                        public void onPictureTaken(byte[] bytes, Camera camera) {
                                            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                                            File img = new File(dir, lapseNumber + ".jpg");

                                            try {
                                                FileOutputStream of = new FileOutputStream(img);
                                                of.write(bytes);
                                                of.close();

                                                Log.d("FILENAME", img.getAbsolutePath());
                                            } catch (Exception e) {
                                                Log.e("FILE", e.getMessage());
                                            }
                                        }
                                    };
                                    c.takePicture(null, null, callback);
                                    lapseNumber++;
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
                lapseToggle.setText(isLapsing ? R.string.stop : R.string.start);
            }
        });
    }
}
