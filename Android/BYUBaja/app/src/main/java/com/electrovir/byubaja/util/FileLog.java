package com.electrovir.byubaja.util;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.content.Context.DOWNLOAD_SERVICE;

public class FileLog {

    private static final String LOG_TAG = "FILE_LOGGER";
    private boolean permissionAsked = false;


    private enum LogType {
        DEBUG,
        ERROR,
        INFO,
        VERBOSE,
        WARN,
        WTF
    }
    private static final Map<LogType, String> logTypeStrings;

    static {
        logTypeStrings = new HashMap<>();
        logTypeStrings.put(LogType.DEBUG, "D");
        logTypeStrings.put(LogType.ERROR, "E");
        logTypeStrings.put(LogType.INFO, "I");
        logTypeStrings.put(LogType.VERBOSE, "V");
        logTypeStrings.put(LogType.WARN, "W");
        logTypeStrings.put(LogType.WTF, "WTF");
    }

    public FileLog() {}

    public FileLog(Activity activity, String appName) {
        try {
            setDefaultFiles(activity, appName);
        }
        catch (IOException e) {
            Log.e(LOG_TAG, "Failed to initiae log file.");
        }
    }

    public File logFile;
    public File dataFile;

    public String d(String tag, String message) {
        Log.d(tag, message);
        return writeMessageToLog(tag, message, LogType.DEBUG);
    }
    public String d(String tag, String message, Throwable exception) {
        Log.d(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.DEBUG);
    }
    public String e(String tag, String message) {
        Log.e(tag, message);
        return writeMessageToLog(tag, message, LogType.ERROR);
    }
    public String e(String tag, String message, Throwable exception) {
        Log.e(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.ERROR);
    }
    public String i(String tag, String message) {
        Log.i(tag, message);
        return writeMessageToLog(tag, message, LogType.INFO);
    }
    public String i(String tag, String message, Throwable exception) {
        Log.i(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.INFO);
    }
    public String v(String tag, String message) {
        Log.v(tag, message);
        return writeMessageToLog(tag, message, LogType.VERBOSE);
    }
    public String v(String tag, String message, Throwable exception) {
        Log.v(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.VERBOSE);
    }
    public String w(String tag, Throwable exception) {
        Log.w(tag, exception);
        return writeMessageToLog(tag, exception.getMessage(), LogType.WARN);
    }
    public String w(String tag, String message) {
        Log.w(tag, message);
        return writeMessageToLog(tag, message, LogType.WARN);
    }
    public String w(String tag, String message, Throwable exception) {
        Log.w(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.WARN);
    }
    public String wtf(String tag, Throwable exception) {
        Log.wtf(tag, exception);
        return writeMessageToLog(tag, exception.getMessage(), LogType.WTF);
    }
    public String wtf(String tag, String message) {
        Log.wtf(tag, message);
        return writeMessageToLog(tag, message, LogType.WTF);
    }
    public String wtf(String tag, String message, Throwable exception) {
        Log.wtf(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.WTF);
    }
    public String data(String tag, String data) {
        return writeData(data);
    }

    private static String formatLogString(String tag, String message, LogType level) {
        StackTraceElement traceElement = new Throwable().getStackTrace()[3];
        int lineNumber = traceElement.getLineNumber();
        String fileName = traceElement.getFileName();

//        String formattedString = fileName + ":" + lineNumber + " " + logTypeStrings.get(level) + "/" + tag + ":" + message;
        return getTimeStamp() + " "  + logTypeStrings.get(level) + "/" + tag +
                ":" +
                message;
    }

    // SETTERS

    private File setupFile(Activity activity, File dir, String fileName)
            throws IOException {
        verifyWritePermissions(activity);

        String date = getFormattedDate();
        int dotIndex = fileName.lastIndexOf(".");

        if (fileName.length() == 0) {
            throw new IllegalArgumentException("Passed file name is empty and no date was " +
                    "expected to replace it.");
        }
        else if (dotIndex == -1) {
            fileName = fileName + ".txt";
        }

        File completeFile = new File(dir, fileName);

        completeFile.getParentFile().mkdirs();

        if (!completeFile.exists()) {
            completeFile.createNewFile();
        }

        return completeFile;
    }

    public void setDataFile(Activity activity, File dir, String fileName) throws IOException {
        this.dataFile = setupFile(activity, dir, fileName);
    }

    public void setDataFile(Activity activity, String appName, String fileName) throws IOException {
        setDataFile(activity, getParentDir(appName), fileName);
    }

    public void setLogFile(Activity activity, File dir, String fileName) throws IOException {
        this.logFile = setupFile(activity, dir, fileName);
    }

    public void setLogFile(Activity activity, String appName, String fileName) throws IOException {
        setLogFile(activity, getParentDir(appName), fileName);
    }

    public static File getDownloadsFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static File getParentDir(String appName) {
        return new File(getDownloadsFolder(), appName + File.separator + getFormattedDate());
    }

    public void setDefaultFiles(Activity activity, String appName) throws IOException {
        final File parentDir = getParentDir(appName);

        this.logFile = setupFile(activity, parentDir, "log");
        this.dataFile = setupFile(activity, parentDir, "data");
    }

    public static String getTimeStamp() {
        return Long.toString((new Date()).getTime() / 1000);
    }

    private static String getFormattedDate() {
        return new SimpleDateFormat("yyyy_MM_dd").format(new Date());
    }

    private String writeMessageToLog(String tag, String message, LogType level) {
        String formattedText = formatLogString(tag, message, level);
        write(this.logFile, formattedText);
        return formattedText;
    }

    private String writeData(String data) {
        return write(this.dataFile, data);
    }

    private static String write(File file, String text) {
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append(text);
            buf.newLine();
            buf.close();
            return text;
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "Error writing to file: " + e.getMessage());
            return "";
        }
    }

    public static void addFileToDownloads(File addFile, Context context) {
        //https://stackoverflow.com/a/46657146/5500690
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

        // add the parent folder to the downloads list
        downloadManager.addCompletedDownload(addFile.getParentFile().getName(), addFile
                .getParentFile().getName(), true, "text/plain",addFile.getAbsolutePath(),addFile
                .length(),false);

        // add the new file to the downloads list
        downloadManager.addCompletedDownload(addFile.getName(), addFile.getName(), true,
                "text/plain",addFile.getAbsolutePath(),addFile.length(),false);
    }

    public void saveFiles(Context context) {
        if (logFile != null) {
            addFileToDownloads(logFile, context);
        }
        if (dataFile != null) {
            addFileToDownloads(dataFile, context);
        }
    }

    public void verifyWritePermissions(Activity activity) {
        final int REQUEST_EXTERNAL_STORAGE = 1;
        final String[] PERMISSIONS_STORAGE = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        // Check if we have write permission
        int permission = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED && !permissionAsked) {
            permissionAsked = true;
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }
}
