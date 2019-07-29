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
import android.webkit.MimeTypeMap;

import androidx.exifinterface.media.ExifInterface;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Core implementation of methods related to File manipulation
 */
class FileUtils {

    private static final String TAG = "FileUtils";
    private static final double SCALE_FACTOR = 50.0;

    private static final int DEGREES_90 = 90;
    private static final int DEGREES_180 = 180;
    private static final int DEGREES_270 = 270;

    static SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    static String insertImage(ContentResolver cr, String path) throws IOException {

        Log.d(getTime(), "start");
        File file = new File(path);
        Log.d(getTime(), "created file");
        String mimeType = MimeTypeMap.getFileExtensionFromUrl(file.toString());
        byte[] source = readBytesFromFile(file);
        Log.d(getTime(), "read bytes");

        byte[] rotatedBytes = getRotatedBytes(source, path);
        if (rotatedBytes != null) {
            source = rotatedBytes;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, file.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
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
                Log.d(getTime(), "started write");
                try {
                    if (imageOut != null) {
                        imageOut.write(source);
                    }
                    Log.d(getTime(), "finished write");
                } finally {
                    if (imageOut != null) {
                        imageOut.close();
                    }
                }

                long id = ContentUris.parseId(url);
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(
                        cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                storeThumbnail(cr, miniThumb, id);
                Log.d(getTime(), "created thumbnail");
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

    static String insertVideo(ContentResolver cr, String path) {

        File file = new File(path);
        String mimeType = MimeTypeMap.getFileExtensionFromUrl(file.toString());
        byte[] source = readBytesFromFile(file);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, file.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
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

        Log.d(getTime(), "started rotation");

        Bitmap bitmap = BitmapFactory.decodeByteArray(source, 0, source.length);
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationInDegrees);
        Bitmap adjustedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        Log.d(getTime(), "finished rotation");

        Log.d(getTime(), "started bitmap to array");
        byte[] rotatedBytes = bitmapToArray(adjustedBitmap);
        Log.d(getTime(), "finished bitmap to array");

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
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bmp.recycle();
        return byteArray;
    }

    private static byte[] readBytesFromFile(File file) {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private static String getTime() {
        return "IMAGE " + dateFormat.format(new Date());
    }

}

