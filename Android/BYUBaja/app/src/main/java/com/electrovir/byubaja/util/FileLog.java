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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.DOWNLOAD_SERVICE;

public class FileLog {

    public static final String LOG_TAG = "FILE_LOGGER";
    private static boolean permissionAsked = false;


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

    private static File logFile;
    private static File dataFile;

    public static String d(String tag, String message) {
        Log.d(tag, message);
        return writeMessageToLog(tag, message, LogType.DEBUG);
    }
    public static String d(String tag, String message, Throwable exception) {
        Log.d(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.DEBUG);
    }
    public static String e(String tag, String message) {
        Log.e(tag, message);
        return writeMessageToLog(tag, message, LogType.ERROR);
    }
    public static String e(String tag, String message, Throwable exception) {
        Log.e(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.ERROR);
    }
    public static String i(String tag, String message) {
        Log.i(tag, message);
        return writeMessageToLog(tag, message, LogType.INFO);
    }
    public static String i(String tag, String message, Throwable exception) {
        Log.i(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.INFO);
    }
    public static String v(String tag, String message) {
        Log.v(tag, message);
        return writeMessageToLog(tag, message, LogType.VERBOSE);
    }
    public static String v(String tag, String message, Throwable exception) {
        Log.v(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.VERBOSE);
    }
    public static String w(String tag, Throwable exception) {
        Log.w(tag, exception);
        return writeMessageToLog(tag, exception.getMessage(), LogType.WARN);
    }
    public static String w(String tag, String message) {
        Log.w(tag, message);
        return writeMessageToLog(tag, message, LogType.WARN);
    }
    public static String w(String tag, String message, Throwable exception) {
        Log.w(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.WARN);
    }
    public static String wtf(String tag, Throwable exception) {
        Log.wtf(tag, exception);
        return writeMessageToLog(tag, exception.getMessage(), LogType.WTF);
    }
    public static String wtf(String tag, String message) {
        Log.wtf(tag, message);
        return writeMessageToLog(tag, message, LogType.WTF);
    }
    public static String wtf(String tag, String message, Throwable exception) {
        Log.wtf(tag, message, exception);
        return writeMessageToLog(tag, message + " exception: " + exception.getMessage(), LogType.WTF);
    }
    public static String data(String tag, String data) {
        writeData(data);
        return FileLog.i(tag, data);
    }

    private static String formatLogString(String tag, String message, LogType level) {
        StackTraceElement traceElement = new Throwable().getStackTrace()[3];
        int lineNumber = traceElement.getLineNumber();
        String fileName = traceElement.getFileName();

//        String formattedString = fileName + ":" + lineNumber + " " + logTypeStrings.get(level) + "/" + tag + ":" + message;
        String formattedString = getTimeStamp() + " "  + logTypeStrings.get(level) + "/" + tag +
                ":" +
                message;

        return formattedString;
    }

    // SETTERS

    private static File setupFile(Activity activity, File dir, String fileName) throws IOException {
        verifyWritePermissions(activity);

        String completeFileName;
        String date = getFormattedDate();
        int dotIndex = fileName.lastIndexOf(".");

        if (dotIndex != -1) {
            completeFileName = fileName.substring(0, dotIndex) + "_" + date + fileName.substring(dotIndex);
        }
        else if (fileName.length() < 1) {
            completeFileName = date + ".txt";
        }
        else {
            completeFileName = fileName + "_" + getFormattedDate() + ".txt";
        }

        File completeFile = new File(dir, completeFileName);

        completeFile.getParentFile().mkdirs();

        if (!completeFile.exists()) {
            completeFile.createNewFile();
        }

        return completeFile;
    }

    public static void setDataFile(Activity activity, File dir, String fileName) throws IOException {
        dataFile = setupFile(activity, dir, fileName);
    }

    public static void setLogFile(Activity activity, File dir, String fileName) throws IOException {
        logFile = setupFile(activity, dir, fileName);
    }

    public static File getDownloadsFolder() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    }

    public static void setDefaultFiles(Activity activity, String appName) throws IOException {
        final File parentDir = new File(getDownloadsFolder(), appName);

        File logDir = new File(parentDir, "logs");
        File dataDir = new File(parentDir, "data");

        logFile = setupFile(activity, logDir, "log");
        dataFile = setupFile(activity, dataDir, "data");
    }

    public static String getTimeStamp() {
        return Long.toString((new Date()).getTime() / 1000);
    }

    private static String getFormattedDate() {
        return new SimpleDateFormat("yyyy_MM_dd").format(new Date());
    }

    private static String writeMessageToLog(String tag, String message, LogType level) {
        String formattedText = formatLogString(tag, message, level);
        write(logFile, formattedText);
        return formattedText;
    }

    private static void writeData(String data) {
        write(dataFile, data);
    }

    private static void write(File file, String text) {
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(file, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            Log.e(LOG_TAG, "Error writing to file: " + e.getMessage());
        }
    }
    public static void saveFile(Context context) {
        //https://stackoverflow.com/a/46657146/5500690
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

        downloadManager.addCompletedDownload(logFile.getParentFile().getName(), logFile
                .getParentFile().getName(), true, "text/plain",logFile.getAbsolutePath(),logFile
                .length(),false);
        downloadManager.addCompletedDownload(dataFile.getParentFile().getName(), dataFile
                .getParentFile().getName(), true, "text/plain",dataFile.getAbsolutePath(),
                dataFile.length(),false);

        // I think this has to be done twice so that the media manager will actually pick it up
        downloadManager.addCompletedDownload(logFile.getName(), logFile.getName(), true,
                "text/plain",logFile.getAbsolutePath(),logFile.length(),false);
        downloadManager.addCompletedDownload(dataFile.getName(), dataFile.getName(), true,
                "text/plain",logFile.getAbsolutePath(),logFile.length(),false);
    }


    public static void verifyWritePermissions(Activity activity) {
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
