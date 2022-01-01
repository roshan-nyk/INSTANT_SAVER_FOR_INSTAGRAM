package instant.saver.for_instagram.util;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import instant.saver.for_instagram.InstagramActivity;
import instant.saver.for_instagram.api.GetDataFromServer;
import instant.saver.for_instagram.model.album_gallery.Album_Data;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class Utils {

    private final String SHAREDPREFERENCEFILENAME = "INSTANT-SAVER-SHARED-PREFERENCE";
    private final Context context;
    private final SharedPreferences preferences;
    private final Activity activity;
    private List<Long> downloadIds;
    private String []tempCookies = new String[] {
            //            valid upto 15 november 2022
            "mid=YZgLUQALAAHXrIofzMJWKh1-wmYv; ig_did=D07E6975-09A2-49AF-8E2D-2C3751109942; ig_nrcb=1; csrftoken=hpWtiqFwRPRCnuL9wBj2rNEAbqB4wTdO",
            "mid=YZnYEwALAAGjyDyN07FbKknN-_2Z; ig_did=01D2C3F5-8E84-42DE-A831-2BF85304DE07; ig_nrcb=1; csrftoken=B6l22B9O2n621OUqI5KYGPMN7qHhOiYY",
            "mid=YZntHQALAAGfJuZEYPBJbMNzkm0K; ig_did=AA5FBEBD-F620-4F3A-B602-8FA0C5947AD0; ig_nrcb=1; csrftoken=mMWiKpj7Du0El2nqlGH521oNRhPw1wX9",
            "mid=YZz8LgALAAG4OqfQMDL871LVqWYe; ig_did=2878DDDD-3C7B-4308-8756-EB0224888E04; ig_nrcb=1; csrftoken=ZUNhkPsv5J6v4Eh5fQgbm88GuoRyynT7",
            "mid=YZz9zQALAAHHRxRktIfO8mo-9Vdc; ig_did=77F5E071-887D-432E-9504-0D82E1F283DD; ig_nrcb=1; csrftoken=fBXIrb8XrtiJGPbVp0diaEPZPIZumh27",
            "mid=YZz-rQALAAEJ8ZqTIx_XiytKJKpK; ig_did=7DC2F398-B491-4589-B290-3B0CBF965D31; ig_nrcb=1; csrftoken=5OIIIrdeeJXmSnFE5loQ4JYPTGXQoZYi",
            "mid=YZz_eAALAAHSqtMEG5XgeEcZNKsz; ig_did=A178DA4A-ADCA-4BED-8489-CAC4BE90D5E9; ig_nrcb=1; csrftoken=dvmtdFnUrJMfy9sQP1CJNDioDOm3LO8n",
            "mid=YZ0A6gALAAG4gTD-hZDIpoDG_HoL; ig_did=EB2C4407-4EAB-45C1-B1CD-09AC01A8DAF0; ig_nrcb=1; csrftoken=gdcz0TusaVfpao4MdDgFWQBfaVFzc6dn"

    };

    public Utils(Activity activity) {
        this.activity = activity;
        context = activity;
        preferences = context.getSharedPreferences(SHAREDPREFERENCEFILENAME, Context.MODE_PRIVATE);
    }

    public String getTempCookies() {
        int random = new Random().nextInt(8);
        return  tempCookies[random];
    }

    public String getDESTINATIONPATH() {
        return "/InstantSaver/";
    }

    public String getCookies() {
        return preferences.getString("COOKIES", null);
    }

    public void setCookies(String Cookies) {
        preferences.edit().putString("COOKIES", Cookies).apply();
    }

    public String getUserId() {
        return preferences.getString("USER_ID", null);
    }

    public void setUserId(String userId) {
        preferences.edit().putString("USER_ID", userId).apply();
    }

    public String getClipBoardClip() {
        return preferences.getString("CLIP_BOARD_DATA", null);
    }

    public void setClipBoardClip(String clipBoardData) {
        preferences.edit().putString("CLIP_BOARD_DATA", clipBoardData).apply();
    }

    public boolean isUserInstructionActivityShown() {
        return preferences.getBoolean("USER_INSTRUCTION_SHOWN", false);
    }

    public void setUserInstructionActivityShown(boolean userInstructionActivityShown) {
        preferences.edit().putBoolean("USER_INSTRUCTION_SHOWN", userInstructionActivityShown).apply();
    }

    //    deprecated
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void startDownload(String downloadPath, String destinationPath, Context context, String FileName, String type, int count) {
        if (count == 0) {
            if (activity instanceof InstagramActivity)
                ((InstagramActivity) activity).hideProgressBar();
            Toast.makeText(context, "Download Started", Toast.LENGTH_SHORT).show();
            downloadIds = new ArrayList<>();
        }
        Uri uri = Uri.parse(downloadPath); // Path from where you want to download file.
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);  // Tell on which network you want to download file.

//            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);  // This will show notification on top when downloading the file.
//            request.setTitle(FileName + ""); // Title for notification.
        request.setVisibleInDownloadsUi(true);    // necessary otherwise all downloads will be deleted in v11

        if (type.equals("photo"))
            request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, destinationPath + "InstantPicture/" + FileName);  // Storage directory path
        else
            request.setDestinationInExternalPublicDir(DIRECTORY_DOWNLOADS, destinationPath + "InstantVideos /" + FileName);

        long ID = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).enqueue(request); // This will start downloading
        downloadIds.add(ID);

        try {
            if (type.equals("photo"))
                MediaScannerConnection.scanFile(context, new String[]{new File(DIRECTORY_DOWNLOADS + "/" + destinationPath + "InstantPicture/" + FileName).getAbsolutePath()}, null,
                        (path, uri1) -> {
                        });
            else
                MediaScannerConnection.scanFile(context, new String[]{new File(DIRECTORY_DOWNLOADS + "/" + destinationPath + "InstantVideos /" + FileName).getAbsolutePath()}, null,
                        (path, uri1) -> {
                        });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Long> getDownloadIds() {
        return downloadIds;
    }

    public void hideSoftKeyboard(View view) {
        InputMethodManager imm = view.getContext().getSystemService(InputMethodManager.class);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public String getFilenameFromURL(String url) {
        try {
            return new File(new URI(url).getPath()).getName();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            if(url.contains(".jpg"))
                return System.currentTimeMillis() + ".jpg";
            else  return System.currentTimeMillis() + ".mp4";
        }
    }

    public String checkForAlreadyExistedFile(String str, List<Album_Data> albumData) {
        for (Album_Data singleAlbum : albumData) {
            if (singleAlbum.getMedia().contains(str))
                str = getNewUrl(str, albumData);
        }
        return str.substring(29);
    }

    private String getNewUrl(String str, List<Album_Data> albumData) {
        String temp = str.substring(0, str.length() - 4);
        String mediaType = str.substring(str.length() - 4);
        int count = 1;
        String rand = "-" + count++;
        for (Album_Data singleAlbumData : albumData) {
            if (singleAlbumData.getMedia().contains(temp + rand + mediaType))
                rand = "-" + count++;
        }
        return temp + rand + mediaType;
    }

    //    don't invert method
    public boolean checkForAlreadyExistedFile(ArrayList<String> strings, String s, List<Album_Data> albumData) {
        boolean isContain = false;
        if (strings.size() > 0) {         //            checking if all files are present inside the album or not even if a single file out of the all files is not part of album then we will download all files
//            and the files which are part of the album previously will be saved with different names. To do that another function will be called
            for (int i = 0; i < strings.size(); i++) {
                String temp = getFilenameFromURL(strings.get(i));
                String mediaType;
                if (temp.endsWith(".mp4"))
                    mediaType = "InstantVideos /";
                else mediaType = "InstantPicture/";
                temp = temp.substring(0, temp.length() - 4);
                for (Album_Data singleAlbum : albumData) {
                    String finalTemp = temp;
                    if (singleAlbum.getMedia().stream().anyMatch(p -> p.contains(getDESTINATIONPATH() + mediaType + finalTemp))) {
                        isContain = true;
                        break;
                    }
                }
                if (isContain) isContain = false;
                else return false;
            }
            return true;
        } else {
            String temp = getFilenameFromURL(s);
            String mediaType;
            if (temp.endsWith(".mp4"))
                mediaType = "InstantVideos /";
            else mediaType = "InstantPicture/";
            temp = temp.substring(0, temp.length() - 4);
            for (Album_Data singleAlbum : albumData) {
                String finalTemp = temp;
                if (singleAlbum.getMedia().stream().anyMatch(p -> p.contains(getDESTINATIONPATH() + mediaType + finalTemp)))
                    return true;
            }
        }
        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean checkAvailableInternalStorage() {
//        checking if at least 5mb storage available in internal storage
        try {
            final long NUM_BYTES_NEEDED_FOR_MY_APP = 1024 * 1024 * 5L;
            StorageManager storageManager = context.getApplicationContext().getSystemService(StorageManager.class);
            UUID appSpecificInternalDirUuid = storageManager.getUuidForPath(context.getFilesDir());
            long availableBytes = storageManager.getAllocatableBytes(appSpecificInternalDirUuid);
            Log.d("TAG", "checkAvailableInternalStorage: " + availableBytes);
            if (availableBytes >= NUM_BYTES_NEEDED_FOR_MY_APP) {
//            NOT ALLOCATING 5MB STORAGE BUT JUST CHECKING IF THAT MUCH SPACE IS AVAILABLE OR NOT
//            storageManager.allocateBytes(appSpecificInternalDirUuid, NUM_BYTES_NEEDED_FOR_MY_APP);
                return true;
            } else {
                // To request that the user remove all app cache files instead, set
                // "action" to ACTION_CLEAR_APP_CACHE.
                Intent storageIntent = new Intent();
                storageIntent.setAction(StorageManager.ACTION_MANAGE_STORAGE);
//                storageIntent.setAction(StorageManager.ACTION_CLEAR_APP_CACHE);             for this startActivityForResult  required
                context.startActivity(storageIntent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    public void checkAvailableExternalStorage(ArrayList<String> stringUrlToDownload, ArrayList<String> userDetails, List<Album_Data> albumData, ArrayList<Boolean> isVideo) {
        String[] mediaName = new String[stringUrlToDownload.size()];
        AtomicLong file_size1 = new AtomicLong();
        long externalStorageVolumes = new StatFs(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath()).getAvailableBytes();
        AtomicInteger currentCount = new AtomicInteger();
        try {
            for (int i = 0; i < stringUrlToDownload.size(); i++) {
                URL url = new URL(stringUrlToDownload.get(i));
                URLConnection urlConnection = url.openConnection();
                int currentIndex = i;
                GetDataFromServer.getInstance().getDataBaseWriteExecutor().execute(() ->
                {
                    try {
                        urlConnection.connect();
                        file_size1.addAndGet(urlConnection.getContentLengthLong());
                        Log.d("TAG", "checkAvailableExternalStorage: " + file_size1);
//                long file_size2 = Long.parseLong(urlConnection.getHeaderField("content-length"));
                        if (externalStorageVolumes - (1024 * 1024 * 1000L) > file_size1.get()) {
                            activity.runOnUiThread(() -> {
                                makeDecisionOnDownloading(stringUrlToDownload.get(currentIndex), userDetails, albumData, isVideo.get(currentIndex), mediaName, currentIndex, currentCount.getAndIncrement(), stringUrlToDownload.size());
                            });
                        } else
                            activity.runOnUiThread(() -> {
                                Toast.makeText(context, "Storage Availability is very less.\nKindly delete your cache data or some files.", Toast.LENGTH_LONG).show();
                            });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
//                keeping 1.5gb space available for phone use
                if (file_size1.get() > externalStorageVolumes - (1024 * 1024 * 1000L))
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void makeDecisionOnDownloading(String str, ArrayList<String> userDetails, List<Album_Data> albumData, boolean isVideo, String[] mediaName, int currentIndex, int currentCount, int totalDownloads) {
//        check for original file present or not
        if (totalDownloads != 1 || !checkForAlreadyExistedFile(new ArrayList<>(), str, albumData)) {
            String temp = getFilenameFromURL(str);
            String fileNameUrl;
//            check for duplicate file present or not
            if (isVideo) {
                temp = checkForAlreadyExistedFile(getDESTINATIONPATH() + "InstantVideos /" + temp, albumData);
                startDownload(str, getDESTINATIONPATH(), activity, temp, "video", currentCount);
                fileNameUrl = getDESTINATIONPATH() + "InstantVideos /" + temp;
            } else {
                temp = checkForAlreadyExistedFile(getDESTINATIONPATH() + "InstantPicture/" + temp, albumData);
                startDownload(str, getDESTINATIONPATH(), activity, temp, "photo", currentCount);
                fileNameUrl = getDESTINATIONPATH() + "InstantPicture/" + temp;
            }
            mediaName[currentIndex] = fileNameUrl;
        } else {
            Toast.makeText(activity, "File Already Exist", Toast.LENGTH_LONG).show();
            if (activity instanceof InstagramActivity)
                ((InstagramActivity) activity).hideProgressBar();
        }
        if (mediaName.length > 0 && currentCount + 1 == totalDownloads) {
            ArrayList<String> newMediaName = new ArrayList<>(Arrays.asList(mediaName));
            Album_Data albumDataToStoreInGallery = new Album_Data(newMediaName);
            albumDataToStoreInGallery.setUserName(userDetails.get(0));
            albumDataToStoreInGallery.setProfilePicUrl(userDetails.get(1));
            albumDataToStoreInGallery.setShortcode(userDetails.get(2));
            albumDataToStoreInGallery.setMediaCaption(userDetails.get(3));
            albumDataToStoreInGallery.setProduct_type(userDetails.get(4));
//            Storing values in hashmap which will be checked with download IDs after download is completed
            GetDataFromServer.getInstance().getStoreDataAfterDownloadCompletion().put(albumDataToStoreInGallery, getDownloadIds());
        }
    }

    public boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public boolean isExternalStorageReadOnly() {
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(Environment.getExternalStorageState());
    }

    public boolean checkPermission() {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {*/
        int readCheck = ContextCompat.checkSelfPermission(activity.getApplicationContext(), READ_EXTERNAL_STORAGE);
        int writeCheck = ContextCompat.checkSelfPermission(activity.getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q)
            return readCheck == PackageManager.PERMISSION_GRANTED;
        return readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED;
        //        }
    }
}
