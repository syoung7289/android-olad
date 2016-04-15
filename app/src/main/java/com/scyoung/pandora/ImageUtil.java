package com.scyoung.pandora;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.InputStream;

/**
 * Created by scyoung on 3/9/16.
 */
public class ImageUtil {

    public static Bitmap getScaledBitmap(int resourceId, int reqWidth, int reqHeight, Context context) {
        Bitmap ret;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        Log.d("ImageUtil", "getScaledBitmap original image height is: " + options.outHeight);
        Log.d("ImageUtil", "getScaledBitmap original image width is: " + options.outWidth);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        ret = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        Log.d("ImageUtil", "getScaledBitmap scaled image height is: " + ret.getHeight());
        Log.d("ImageUtil", "getScaledBitmap scaled image width is: " + ret.getWidth());
        Log.d("ImageUtil", "image in bytes: " + ret.getByteCount());

        return ret;
    }

    public static Bitmap getScaledBitmap(Uri uri, int maxSide, Context context) {
        return getScaledBitmap(uri, maxSide, maxSide, context);
    }

    public static Bitmap getScaledBitmap(Uri uri, int reqWidth, int reqHeight, Context context) {
        Bitmap ret;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            InputStream imageStream = contentResolver.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);
            Log.d("ImageUtil", "getScaledBitmap original image height is: " + options.outHeight);
            Log.d("ImageUtil", "getScaledBitmap original image width is: " + options.outWidth);
            imageStream.close();

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            imageStream = contentResolver.openInputStream(uri);
            ret = BitmapFactory.decodeStream(imageStream, null, options);
            Log.d("ImageUtil", "getScaledBitmap scaled image height is: " + ret.getHeight());
            Log.d("ImageUtil", "getScaledBitmap scaled image width is: " + ret.getWidth());
            Log.d("ImageUtil", "image in bytes: " + ret.getByteCount());
            imageStream.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            ret = null;
        }
        return ret;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap getBitmap(Uri imageUri) {
        Bitmap ret = null;
        try {
            File f = new File(imageUri.getPath());
            if (f.exists()) {
                Log.d("decodeUri", f.getAbsolutePath());
                ret = BitmapFactory.decodeFile(f.getAbsolutePath());
            }
            else {
                Log.d("CA:decodeUri", "File not found: " + imageUri.getPath());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
