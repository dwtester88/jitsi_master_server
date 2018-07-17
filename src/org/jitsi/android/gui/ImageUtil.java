package org.jitsi.android.gui;


        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.graphics.Matrix;
        import android.media.ExifInterface;
        import android.os.Environment;
        import android.util.Base64;
        import android.util.Log;
        import org.jitsi.android.gui.chat.ChatSession;

        import java.io.*;

/**
 * Created by vinal on 02/05/18.
 */

public class ImageUtil {


    int maxWidth = 612;
    int maxHeight = 816;
    Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
    int quality = 80;
    String destinationDirectoryPath;


    static Bitmap decodeSampledBitmapFromFile(File imageFile, int reqWidth, int reqHeight) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap scaledBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        //check the rotation of the image and display it properly
        ExifInterface exif;
        exif = new ExifInterface(imageFile.getAbsolutePath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
        orientation = 0;
        Matrix matrix = new Matrix();
        if (orientation == 6) {
            matrix.postRotate(90);
        } else if (orientation == 3) {
            matrix.postRotate(180);
        } else if (orientation == 8) {
            matrix.postRotate(270);
        }
        scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        return scaledBitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void compressImageto(File file,String sourcerequest) {
        try {
            compressImages(file, 612, 816, Bitmap.CompressFormat.JPEG, 80);
            sendimage(sourcerequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static File compressImages(File imageFile, int reqWidth, int reqHeight, Bitmap.CompressFormat compressFormat, int quality) throws IOException {
        FileOutputStream fileOutputStream = null;
        File file = new File("test.jpg");

        if (!file.exists()) {
            file = new File(Environment.getExternalStorageDirectory(),"test.jpg");

        }
        try {
            fileOutputStream = new FileOutputStream(file);
            // write the compressed bitmap at the destination specified by destinationPath.
            decodeSampledBitmapFromFile(imageFile, reqWidth, reqHeight).compress(compressFormat, quality, fileOutputStream);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }
        Log.d("CameraPrieview","Picture is saved");


        return file;
    }

    public static void sendimage(String sourcerequest) {
        File imagefile = new File(Environment.getExternalStorageDirectory(),"test.jpg" );
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(imagefile);
            Bitmap bm = BitmapFactory.decodeStream(fis);
            ByteArrayOutputStream outstream = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 50, outstream);
            byte[] byteArr = outstream.toByteArray();
            String imagesend =Base64.encodeToString(byteArr, Base64.DEFAULT);

            ChatSession.sendMessage(imagesend,sourcerequest);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}

