package pl.pollub.android.app4;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ProgressInfo implements Parcelable {
    public static final int DOWNLOAD_START = 0;
    public static final int DOWNLOAD_STOP = 1;
    public static final int DOWNLOAD_ERROR = 2;
    private int fileSize;
    private int downloadBytes;
    private int downloadStatus;

    public ProgressInfo(int fileSize){
        this.downloadBytes=0;
        this.fileSize=0;
        this.downloadStatus=DOWNLOAD_START;
    }
    public ProgressInfo(int fileSize, int downloadBytes, int downloadStatus){
        this.downloadBytes=downloadBytes;
        this.fileSize=fileSize;
        this.downloadStatus=downloadStatus;
    }

    protected ProgressInfo(Parcel in) {
        fileSize = in.readInt();
        downloadBytes = in.readInt();
        downloadStatus = in.readInt();
    }

    public static final Creator<ProgressInfo> CREATOR = new Creator<ProgressInfo>() {
        @Override
        public ProgressInfo createFromParcel(Parcel in) {
            return new ProgressInfo(in);
        }

        @Override
        public ProgressInfo[] newArray(int size) {
            return new ProgressInfo[size];
        }
    };

    public boolean isDownloadFinished(){
        return this.downloadBytes == this.fileSize;
    }
    public int getProgressValue() {
        return (int)(((double)this.downloadBytes/(double) this.fileSize)*100);
    }



    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getDownloadBytes() {
        return downloadBytes;
    }

    public void setDownloadBytes(int downloadBytes) {
        this.downloadBytes = downloadBytes;
    }

    public int getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(fileSize);
        parcel.writeInt(downloadBytes);
        parcel.writeInt(downloadStatus);
    }

    public void increaseDownloadBytes(int pobrano) {
        this.downloadBytes += pobrano;
    }
    /*
    public void setDownloadFinished(){
        this.downloadStatus = DOWNLOAD_STOP;
        this.downloadBytes = this.fileSize;

    }
    */

}
