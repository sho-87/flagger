package ca.simonho.flagger;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

public class MediaScanner {
    private final String TAG = "MediaScanner";

    protected void scanFile(final Context context, String[] files, String[] mimeTypes) {
        MediaScannerConnection.scanFile(context, files, mimeTypes,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {

                        if (uri == null) {
                            Log.e(TAG, "Media scan failed");
                            Log.e(TAG, "Failed path: " + path);
                        } else {
                            Log.d(TAG, "Scan successful: " + path);
                            Log.d(TAG, "Succeed uri: " + uri);
                        }
                    }
                }
        );
    }
}