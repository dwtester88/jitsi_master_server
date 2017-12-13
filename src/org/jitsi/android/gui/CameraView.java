package org.jitsi.android.gui;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.*;

import org.jitsi.*;
import org.jitsi.android.gui.chat.ChatSession;
import org.jitsi.android.gui.contactlist.ContactListFragment;

public class CameraView extends Activity implements SurfaceHolder.Callback, View.OnClickListener {
    private static final String TAG = "CameraTest";
    Camera mCamera = null;
    boolean mPreviewRunning,first = false;
    boolean flag_wait=true;



    @SuppressWarnings("deprecation")
    public void onCreate(Bundle icicle){
        super.onCreate(icicle);
        Log.e(TAG, "onCreate");

        first = getIntent().getBooleanExtra("First",false);
        flag_wait=getIntent().getBooleanExtra("wait_flag",true);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.cameraview);
        ImageView img = (ImageView) findViewById(R.id.blankImage);

        /*if(CaptureCameraImage.isBlack)
            img.setBackgroundResource(android.R.color.black);
        else
            img.setBackgroundResource(android.R.color.white);*/

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceView.setOnClickListener(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
    }


    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            if (data != null){
                //Intent mIntent = new Intent();
                //mIntent.putExtra("image",imageData);



                mCamera.stopPreview();
                mPreviewRunning = false;
                mCamera.release();

                //1536 x 2048 replace 200 x 200
                try{
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    Bitmap bitmap= BitmapFactory.decodeByteArray(data, 0, data.length,opts);

                    bitmap = Bitmap.createScaledBitmap(bitmap, 1536, 2048, false);
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int newWidth = 1536;
                    int newHeight = 2048;
                    File file = new File("test2" + ".jpg");
                    if (!file.exists()) {
                        file = new File(
                                Environment.getExternalStorageDirectory(),
                                "test2.jpg");
                        Log.e("file exist", "" + file + ",Bitmap= " + "test2");
                    }
                    try {
                        // make a new bitmap from your file
                        FileOutputStream outStream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outStream);






                        outStream.flush();
                        outStream.close();

                        //mychange
                        sendimage();



                    } catch (Exception e) {
                        Log.d("Error e ",e.getMessage());
                    }

                    /*// calculate the scale - in this case = 0.4f
                    float scaleWidth = ((float) newWidth) / width;
                    float scaleHeight = ((float) newHeight) / height;

                    // createa matrix for the manipulation
                    Matrix matrix = new Matrix();
                    // resize the bit map
                    matrix.postScale(scaleWidth, scaleHeight);
                    // rotate the Bitmap
                    matrix.postRotate(-90);
                    Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                            width, height, matrix, true);
                    // CaptureCameraImage.image.setImageBitmap(resizedBitmap);*/

                }catch(Exception e){
                    e.printStackTrace();
                }
                //StoreByteImage(mContext, imageData, 50,"ImageName");
                //setResult(FOTO_MODE, mIntent);

                Intent returnIntent = new Intent();

                returnIntent.putExtra("result",first);
                setResult(Activity.RESULT_OK,returnIntent);
                returnIntent.putExtra("wait_flag",flag_wait);
                finish();
                //setResult(585);
                //finish();
            }
        }
    };


    //mychange
    private void sendimage() {
        File imagefile = new File(Environment.getExternalStorageDirectory(),"test2.jpg" );
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imagefile);
            Bitmap bm = BitmapFactory.decodeStream(fis);
            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 50, outstream);
            byte[] byteArr = outstream.toByteArray();
            String imagesend =Base64.encodeToString(byteArr, Base64.DEFAULT);
            ChatSession.sendMessage(imagesend);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }




    protected void onResume(){
        Log.e(TAG, "onResume");
        super.onResume();
    }

    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
    }

    protected void onStop(){
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @TargetApi(9)
    public void surfaceCreated(SurfaceHolder holder){
        Log.e(TAG, "surfaceCreated");
        //prase 0 for backcamera parse 1 for frontcamera
        mCamera = Camera.open(0);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {

        Log.e(TAG, "surfaceChanged");

        // XXX stopPreview() will crash if preview is not running
        if (mPreviewRunning){
            mCamera.stopPreview();
        }

        //1536 x 2048 replace 200 x 200
        Camera.Parameters p = mCamera.getParameters();
        p.setPreviewSize(1536, 2048);


        /*if(CaptureCameraImage.cameraID == 0){
            String stringFlashMode = p.getFlashMode();
            if (stringFlashMode.equals("torch"))
                p.setFlashMode("on"); // Light is set off, flash is set to normal 'on' mode
            else
                p.setFlashMode("torch");
        }*/
        try {
            mCamera.setParameters(p);
        }catch (Exception e){
            e.printStackTrace();
            Log.d("Error ",e.getMessage());
        }

        try{
            mCamera.setPreviewDisplay(holder);
        }catch (Exception e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mCamera.startPreview();
        mPreviewRunning = true;
        mCamera.takePicture(null, mPictureCallback, mPictureCallback);
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surfaceDestroyed");
        //mCamera.stopPreview();
        //mPreviewRunning = false;
        //mCamera.release();
    }

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    public void onClick(View v) {
        // TODO Auto-generated method stub
      //  mCamera.setDisplayOrientation(90);

        mCamera.takePicture(null, mPictureCallback, mPictureCallback);
    }

}