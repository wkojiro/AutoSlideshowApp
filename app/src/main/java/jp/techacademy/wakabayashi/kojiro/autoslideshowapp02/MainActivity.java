package jp.techacademy.wakabayashi.kojiro.autoslideshowapp02;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/* contentProvider系　*/
import android.os.Build;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/* Timer系　*/
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    Button mPlayButton;
    Button mBackButton;
    Button mNextButton;
    ImageView imageView;
    int startPosition = 0;
    Boolean isStart = false;
    Timer mTimer;

    // タイマー用の時間のための変数
    double mTimerSec = 0.0;

    // handler (別スレッドを立てる）
    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* 定義エリア　*/
        mPlayButton = (Button) findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(this);

        mBackButton = (Button) findViewById(R.id.back_button);
        mBackButton.setOnClickListener(this);

        mNextButton = (Button) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);

        imageView = (ImageView)findViewById(R.id.imageView);

       // imageView.setImageResource(XXX);

        /* パーミッションエリア　*/
        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            /*
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                Long id = cursor.getLong(fieldIndex);
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                imageView.setImageURI(imageUri);
                Log.d("ANDROID", "URI : " + imageUri.toString());
            } while (cursor.moveToNext());
            */

            int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            Long id = cursor.getLong(fieldIndex);
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

            imageView.setImageURI(imageUri);
            startPosition = cursor.getPosition();
        }
        cursor.close();
    }



    @Override
    public void onClick(final View v) {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        final Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

       final int ccount = cursor.getCount();
       Log.d("Position0(タイマー起動前）", String.valueOf(startPosition));


        if (v.getId() == R.id.play_button) {
            Log.d("UI_PARTS", "ボタン1をタップしました");

            if(isStart == false) {
                isStart = true;
                mPlayButton.setText("停止");

                mBackButton.setEnabled(false);
                mNextButton.setEnabled(false);


                // タイマーの作成
                mTimer = new Timer();
                // タイマーの始動
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Log.d("Position1(タイマー起動直後）", String.valueOf(startPosition));
                        if (0 <= startPosition && startPosition <= ccount - 2) {
                            if (cursor.moveToPosition(startPosition)) {
                                cursor.moveToNext();
                                Log.d("Position2（Next後）", String.valueOf(startPosition));
                                int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                                Long id = cursor.getLong(fieldIndex);
                                final Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                                //imageView.setImageURI(imageUri);

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        imageView.setImageURI(imageUri);
                                        Log.d("Position3（描画したID）", String.valueOf(startPosition));
                                    }
                                });

                                //cursor.moveToPosition(startPosition);

                                startPosition = cursor.getPosition();

                                Log.d("Position4（終了後のID）", String.valueOf(startPosition));

                                if(startPosition == ccount -1){
                                    startPosition = 0;
                                }
                                // cursor.close();
                            }
                        }
                    }
                }, 1000, 2000);    // 最初に始動させるまで 1秒、ループの間隔を 2秒 に設定
            } else {
                isStart = false;
                mPlayButton.setText("再生");
                cursor.close();
                mTimer.cancel();

                mBackButton.setEnabled(true);
                mNextButton.setEnabled(true);

            }

        } else if (v.getId() == R.id.back_button) {
            Log.d("UI_PARTS", "ボタン2をタップしました");

             if(isStart == false) {

                Log.d("カーソルの位置がBeforeFirstかどうか", String.valueOf(cursor.isBeforeFirst()));

                if (startPosition >= 0) {

                    if(startPosition == 0){
                        cursor.moveToLast();
                        startPosition = cursor.getPosition();
                    }

                    if (cursor.moveToPosition(startPosition)) {
                        cursor.moveToPrevious();

                        Log.d("Position3", String.valueOf(startPosition));

                        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                        Long id = cursor.getLong(fieldIndex);
                        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                        imageView.setImageURI(imageUri);
                    }

                    //cursor.moveToPosition(startPosition);
                    startPosition = cursor.getPosition();
                    Log.d("カーソルの位置がBeforeFirstかどうか", String.valueOf(cursor.isBeforeFirst()));


                     startPosition = cursor.getPosition();
                     cursor.close();
                }
            }

        } else if (v.getId() == R.id.next_button){
            Log.d("UI_PARTS", "ボタン3をタップしました");

            if(isStart == false) {

                if (startPosition <= ccount - 2) {
                    if (cursor.moveToPosition(startPosition)) {
                        cursor.moveToNext();
                        Log.d("Position2", String.valueOf(startPosition));
                        Log.d("Carsor数", String.valueOf(cursor.getColumnCount()));
                        Log.d("Carsor数", String.valueOf(cursor.getCount()));
                        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                        Long id = cursor.getLong(fieldIndex);
                        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                        imageView.setImageURI(imageUri);
                    }

                /* 実験 後で消す　*/
                    // cursor.moveToLast();
                    // Log.d("LastのInt", String.valueOf(cursor.getPosition()));
                    // Log.d("LastのIntかどうか", String.valueOf(cursor.isAfterLast()));

                    //cursor.moveToPosition(startPosition);
                    startPosition = cursor.getPosition();

                    if(startPosition == ccount -1){
                        startPosition = 0;
                    }

                    cursor.close();
                }
            }
        }
    }
}
