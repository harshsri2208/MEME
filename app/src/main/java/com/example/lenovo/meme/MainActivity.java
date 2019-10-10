package com.example.lenovo.meme;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_REQUEST = 1;
    private static final int RESULT_LOAD_IMAGE = 2;

    Button btnLoad, btnSave, btnShare, btnGo, btnClear;
    TextView prevTextTop, preTextBottom;
    EditText inputTextTop, inputTextBottom;
    ImageView ivMemePreview;
    String currentImage = "";
    boolean imageLoaded = false, textAdded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        }

        ivMemePreview = findViewById(R.id.imageView);

        prevTextTop = findViewById(R.id.textView1);
        preTextBottom = findViewById(R.id.textView2);

        inputTextTop = findViewById(R.id.editText1);
        inputTextBottom = findViewById(R.id.editText2);

        btnGo = findViewById(R.id.go);

        btnLoad = findViewById(R.id.load);
        btnSave = findViewById(R.id.save);
        btnShare = findViewById(R.id.share);
        btnClear = findViewById(R.id.btnClear);

        btnSave.setEnabled(false);
        btnShare.setEnabled(false);
        btnGo.setEnabled(false);
        btnGo.setText("Upload an image first");

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View content = findViewById(R.id.lay);
                Bitmap bitmap = getScreenShot(content);
                currentImage = "meme" + System.currentTimeMillis() + ".png";
                store(bitmap, currentImage);
                btnShare.setEnabled(true);

            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareImage();
            }
        });

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prevTextTop.setText(inputTextTop.getText().toString());
                preTextBottom.setText(inputTextBottom.getText().toString());

                //Forces user to enter at least one line of text
                if (!inputTextTop.getText().toString().equals("") || !inputTextBottom.getText().toString().equals("")) {
                    textAdded = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Enter some text first!",
                            Toast.LENGTH_SHORT).show();
                    textAdded = false;
                    btnShare.setEnabled(false);
                    btnSave.setEnabled(false);
                }
                if (imageLoaded && textAdded) {
                    btnShare.setEnabled(true);
                    btnSave.setEnabled(true);
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputTextTop.setText("");
                inputTextBottom.setText("");
                btnShare.setEnabled(false);
                btnSave.setEnabled(false);
            }
        });
    }

    public static Bitmap getScreenShot(View view) {
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public void store(Bitmap bm, String fileName) {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MEME";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(dirPath, fileName);

        Log.d("PATH", file.getAbsolutePath());
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareImage() {
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MEME";
        String imageName = "meme" + System.currentTimeMillis() + ".png";
        View content = findViewById(R.id.lay);
        Bitmap bitmap = getScreenShot(content);
        File imageFile = new File(dirPath, imageName);
        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Uri uri = FileProvider.getUriForFile(MainActivity.this, "com.example.lenovo.meme.provider", imageFile);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.setType("image/*");

        try {
            startActivity(Intent.createChooser(intent, "Share Via"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No sharing app found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = null;
            if (selectedImage != null) {
                cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            }
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                ivMemePreview.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                //Ensures image can't be saved/shared until text has been added
                imageLoaded = true;
                btnGo.setEnabled(true);
                btnGo.setText("TRY");
                if (textAdded) {
                    btnShare.setEnabled(true);
                    btnSave.setEnabled(true);
                    btnShare.setEnabled(true);
                }
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);//do nothing
            } else {
                Toast.makeText(this, "No Permisssion Granted!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}