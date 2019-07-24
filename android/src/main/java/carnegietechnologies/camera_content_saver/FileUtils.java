package carnegietechnologies.camera_content_saver;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

/**
 * Core implementation of methods related to File manipulation
 */
class FileUtils {

    private static final String TAG = "FileUtils";
    private static final double SCALE_FACTOR = 50.0;

    private static final int DEGREES_90 = 90;
    private static final int DEGREES_180 = 180;
    private static final int DEGREES_270 = 270;

    static String insertImage(ContentResolver cr,
                              byte[] source,
                              String title,
                              String description, String path) throws IOException {


        InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(source));
        String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
        inputStream.close();

        byte[] rotatedBytes = getRotatedBytes(source, path);
        if (rotatedBytes != null) {
            source = rotatedBytes;
            Log.d("BYTES ROTATED", "BYTES ROTATED");

        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = "";

        try {
            url = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream imageOut = null;
                if (url != null) {
                    imageOut = cr.openOutputStream(url);
                }
                try {
                    if (imageOut != null) {
                        imageOut.write(source);
                    }
                } finally {
                    if (imageOut != null) {
                        imageOut.close();
                    }
                }

                long id = ContentUris.parseId(url);
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(
                        cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                storeThumbnail(cr, miniThumb, id
                );
            } else {
                if (url != null) {
                    cr.delete(url, null, null);
                }
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = getFilePathFromContentUri(url, cr);
        }


        return stringUrl;
    }

    static String insertVideo(ContentResolver cr,
                              byte[] source,
                              String title,
                              String description) throws IOException {


        InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(source));
        String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
        inputStream.close();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, description);
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri url = null;
        String stringUrl = "";

        try {
            url = cr.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream videoOutStream = null;
                if (url != null) {
                    videoOutStream = cr.openOutputStream(url);
                }
                try {
                    if (videoOutStream != null) {
                        videoOutStream.write(source);
                    }
                } finally {
                    if (videoOutStream != null) {
                        videoOutStream.close();
                    }
                }
            } else {
                if (url != null) {
                    cr.delete(url, null, null);
                }
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                cr.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = getFilePathFromContentUri(url, cr);
        }


        return stringUrl;
    }

    private static byte[] getRotatedBytes(byte[] source, String path)
            throws IOException {
        int rotationInDegrees = exifToDegrees(getRotation(path));

        if (rotationInDegrees == 0) {
            return null;
        }

        Log.d("orientation", Integer.toString(rotationInDegrees));

        Bitmap bitmap = BitmapFactory.decodeByteArray(source, 0, source.length);
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationInDegrees);
        Bitmap adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();

        byte[] rotatedBytes = bitmapToArray(adjustedBitmap);
        adjustedBitmap.recycle();

        return rotatedBytes;
    }

    private static void storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = (float) SCALE_FACTOR / source.getWidth();
        float scaleY = (float) SCALE_FACTOR / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Thumbnails.KIND, MediaStore.Images.Thumbnails.MICRO_KIND);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID, (int) id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream thumbOut = null;
            if (url != null) {
                thumbOut = cr.openOutputStream(url);
            }
            if (thumbOut != null) {
                thumbOut.close();
            }
        } catch (FileNotFoundException ex) {
            Log.d(TAG, ex.toString());
        } catch (IOException ex) {
            Log.d(TAG, ex.toString());
        }
    }

    private static String getFilePathFromContentUri(Uri selectedVideoUri,
                                                    ContentResolver contentResolver) {
        String filePath = null;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(selectedVideoUri, filePathColumn, null, null, null);

        int columnIndex;

        if (cursor != null) {
            cursor.moveToFirst();
            columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }

        return filePath;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return DEGREES_90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return DEGREES_180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return DEGREES_270;
        }
        return 0;
    }

    private static int getRotation(String path) throws IOException {
        ExifInterface exif = new ExifInterface(path);
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
    }

    private static byte[] bitmapToArray(Bitmap bmp) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] byteArray = stream.toByteArray();
        bmp.recycle();
        return byteArray;
    }
}

