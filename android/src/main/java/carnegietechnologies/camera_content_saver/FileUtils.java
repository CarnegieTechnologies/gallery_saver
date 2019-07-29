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

/**
 * Core implementation of methods related to File manipulation
 */
class FileUtils {

    private static final String TAG = "FileUtils";
    private static final double SCALE_FACTOR = 50.0;

    private static final int DEGREES_90 = 90;
    private static final int DEGREES_180 = 180;
    private static final int DEGREES_270 = 270;

    /**
     * Inserts image into external storage
     *
     * @param contentResolver - content resolver
     * @param path            - path to temp file that needs to be stored
     * @return path to newly created file
     */
    static String insertImage(ContentResolver contentResolver, String path) {

        File file = new File(path);
        String mimeType = MimeTypeMap.getFileExtensionFromUrl(file.toString());
        byte[] source = getBytesFromFile(file);

        byte[] rotatedBytes = getRotatedBytesIfNecessary(source, path);

        if (rotatedBytes != null) {
            source = rotatedBytes;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, file.getName());
        values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        Uri imageUri = null;
        String stringUrl = "";

        try {
            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream outputStream = null;
                if (imageUri != null) {
                    outputStream = contentResolver.openOutputStream(imageUri);
                }
                try {
                    if (outputStream != null) {
                        outputStream.write(source);
                    }
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }

                long pathId = ContentUris.parseId(imageUri);
                Bitmap miniThumb = MediaStore.Images.Thumbnails.getThumbnail(
                        contentResolver, pathId, MediaStore.Images.Thumbnails.MINI_KIND, null);
                storeThumbnail(contentResolver, miniThumb, pathId);
            } else {
                if (imageUri != null) {
                    contentResolver.delete(imageUri, null, null);
                }
                imageUri = null;
            }
        } catch (IOException e) {
            contentResolver.delete(imageUri, null, null);
            imageUri = null;
        }

        if (imageUri != null) {
            stringUrl = getFilePathFromContentUri(imageUri, contentResolver);
        }

        return stringUrl;
    }

    /**
     * @param contentResolver - content resolver
     * @param path            - path to temp file that needs to be stored
     * @return path to newly created file
     */
    static String insertVideo(ContentResolver contentResolver, String path) {

        File file = new File(path);
        String mimeType = MimeTypeMap.getFileExtensionFromUrl(file.toString());
        byte[] source = getBytesFromFile(file);

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
            url = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

            if (source != null) {
                OutputStream videoOutStream = null;
                if (url != null) {
                    videoOutStream = contentResolver.openOutputStream(url);
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
                    contentResolver.delete(url, null, null);
                }
                url = null;
            }
        } catch (Exception e) {
            if (url != null) {
                contentResolver.delete(url, null, null);
                url = null;
            }
        }

        if (url != null) {
            stringUrl = getFilePathFromContentUri(url, contentResolver);
        }


        return stringUrl;
    }

    /**
     * @param source -  array of bytes that will maybe be rotated
     * @param path   - path to image that needs to be checked for rotation
     * @return - array of bytes from rotated image, if rotation needs to be performed
     */
    private static byte[] getRotatedBytesIfNecessary(byte[] source, String path) {
        int rotationInDegrees = 0;

        try {
            rotationInDegrees = exifToDegrees(getRotation(path));
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }

        if (rotationInDegrees == 0) {
            return null;
        }

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

    /**
     * @param contentResolver - content resolver
     * @param source          - bitmap source image
     * @param id              - path id
     */
    private static void storeThumbnail(
            ContentResolver contentResolver,
            Bitmap source,
            long id) {

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

        Uri thumbUri = contentResolver.insert(
                MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            OutputStream outputStream = null;
            if (thumbUri != null) {
                outputStream = contentResolver.openOutputStream(thumbUri);
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (FileNotFoundException ex) {
            Log.d(TAG, ex.toString());
        } catch (IOException ex) {
            Log.d(TAG, ex.toString());
        }
    }

    /**
     * @param uri             - provided file uri
     * @param contentResolver - content resolver
     * @return path from provided Uri
     */
    private static String getFilePathFromContentUri(Uri uri,
                                                    ContentResolver contentResolver) {
        String filePath = null;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA};

        Cursor cursor = contentResolver.query(uri, filePathColumn,
                null, null, null);

        int columnIndex;

        if (cursor != null) {
            cursor.moveToFirst();
            columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            filePath = cursor.getString(columnIndex);
            cursor.close();
        }

        return filePath;
    }

    /**
     * @param orientation - exif orientation
     * @return how many degrees is file rotated
     */
    private static int exifToDegrees(int orientation) {
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return DEGREES_90;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return DEGREES_180;
        } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return DEGREES_270;
        }
        return 0;
    }

    /**
     * @param path - path to bitmap that needs to be checked for orientation
     * @return exif orientation
     * @throws IOException - can happen while creating {@link ExifInterface} object for
     *                     provided path
     */
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

    private static byte[] getBytesFromFile(File file) {
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
}

