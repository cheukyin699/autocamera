package github.cheukyin699.io.autocamera;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    private static final String APP_FOLDER = "/AutoCamera";
    private static final int PERMISSIONS_REQUEST_CODE = 12;

    SurfaceView surface;
    Button lapseToggle;
    EditText lapseText;
    EditText filePrefix;

    boolean isLapsing;
    boolean useCamera;
    boolean extStorage;
    long lapseNumber;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surface = findViewById(R.id.surfaceView);
        lapseToggle = findViewById(R.id.lapseBt);
        lapseText = findViewById(R.id.lapseDelta);
        filePrefix = findViewById(R.id.filePrefix);

        isLapsing = false;
        useCamera = false;
        extStorage = false;
        lapseNumber = 0;

        setPermissions();

        lapseToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!(useCamera && extStorage)) {
                    Toast.makeText(getApplicationContext(),
                            "Have you enabled the appropriate permissions yet?",
                            Toast.LENGTH_LONG).show();

                    return;
                }

                final long delay = Long.valueOf(lapseText.getText().toString());
                isLapsing = !isLapsing;

                if (isLapsing) {
                    // Set up threading
                    Thread t = new CameraThread(delay);
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
            File root = Environment.getExternalStorageDirectory();
            File dir = new File(root.getAbsolutePath() + APP_FOLDER);
            dir.mkdirs();

            File img = new File(dir, prefix + number + ".jpg");

            try {
                FileOutputStream of = new FileOutputStream(img);
                of.write(bytes);
                of.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Do something about this exception
                e.printStackTrace();
            }
        }
    }

    private void setPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[] {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                },
                PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    useCamera = true;
                    extStorage = true;
                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "Cannot use permissions.",
                            Toast.LENGTH_LONG
                    ).show();
                }
                break;
        }
    }

    private class CameraThread extends Thread {
        private final long delay;

        public CameraThread(long delay) {
            this.delay = delay;
        }

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
                // TODO Do something about this exception.
                Log.e("CAMERA", e.getMessage());
            }
        }
    }
}
