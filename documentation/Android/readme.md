# Android Notes

### Restablishing Bluetooth Connection

I've been attempting to program the Android app such that it'll automatically reconnect to the bluetooth module when the connection is lost. I've spent so much time on this without making much progress that I'm giving up. My time is better spent actually buliding the interface or improving the Adruino code. This is a long term strecth goal now. If connection is lost between the tablet and the arduino... all hope is lost. Maybe a simple timer can be run that will check if the connection has been lost and automatically try to reconnect until it does.

Actually, that's an easy way to do it. Maybe I can just do that lol.

**Update:** After much annoyance with the Android platform, I created a background thread that continuously checks if the bluetooth socket is still active and if it isn't, attempts to reconnect. Since it can't actually directly read the status of the socket (socket.isConencted() returns true even if the device has been disconnected), other parts of the code must manually disconnect the socket when errors are encountered with reading it or a disconnect is detected. This reconnection can take up to ~7 seconds since a bluetooth socket can't have a custome timeout set and sometimes the read timeout takes a long time to fail. 

### Android writing to a file

I want to log data to a file so we can analyze it later. However, I'm having extreme difficulty doing this.

I've tried writing it to a file to the app's internal storage but I can't access that from a computer. I've tried writing it to the downloads folder and I can't get anything to work!

#### Saving file then adding to downloads with downloadManager.addCompletedDownload

```Java
DownloadManager downloadManager = (DownloadManager) this.getSystemService(DOWNLOAD_SERVICE);
downloadManager.addCompletedDownload(logFile.getName(), logFile.getName(), true, "text/plain",logFile.getAbsolutePath(),logFile.length(),true);
```

This added the file to the downloads folder but I cannot open the file in anything it all says the file does not exist. However, when reading the file in the app it reads it correctly.

#### Writing directly to the downloads folder
```Java
logFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
```

This isn't working either, I get ``open failed: EACCES (Permission denied)`` even though I have the following permissions set:

```xml
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
```

[A comment on stackoverflow](https://stackoverflow.com/a/28404125/5500690) says the following:
```
DIRECTORY_DOWNLOADS does not exist on my Android 6 (ASUS Nexus 7) and can not be created via mkdir() even with WRITE_EXTERNAL_STORAGE in the manifest
```

Well I'm running Anrdoid 6 on a Nexus 7. What now?

#### SUCCESS

I've done it. Following is my solution.

  * Include the following in ``AndroidManifest.xml``:

```XML
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />
```

  * Open/create folders and files in the following directory:

```Java
Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
```

  * Once the data needs to be saved (before app shutdown for example) and seen from a computer USB connection, do the following:

```Java
DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);

// add the parent folder to the downloads list
downloadManager.addCompletedDownload(
  addFile.getParentFile().getName(),
  addFile.getParentFile().getName(),
  true, // true = hide notifications
  "text/plain",
  addFile.getAbsolutePath(),
  addFile.length(),
  false);

// add the new file to the downloads list
downloadManager.addCompletedDownload(
  addFile.getName(),
  addFile.getName(),
  true,
  "text/plain",
  addFile.getAbsolutePath(),
  addFile.length(),
  false);
```

Note that if you do not set the hide notifications argumen to ``true`` then the tablet will be spammed with tons of notifications.