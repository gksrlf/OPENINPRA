package com.little_wizard.tdc.util;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;

public class S3Transfer {
    private Context context;
    private TransferCallback callback;

    private TransferUtility transferUtility;
    private TransferObserver transferObserver;

    public interface TransferCallback {
        void onStateChanged(TransferState state);

        void onError(int id, Exception e);
    }

    /*
    클래스 사용법
    1)   S3Transfer 생성자 등록. ex) S3Transfer transfer = new S3Transfer(this);
    2)   Callback 또는 TransferObserver 등록.
    Callback 등록 예시)         transfer.setCallback(this);
    TransferObserver 등록 예시) transfer.getTransferObserver().setTransferListener(new TransferListener() {...});
     */

    public S3Transfer(Context context) {
        this.context = context;

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                "ap-northeast-2:13fe9921-fc1f-49ab-b998-c59c0a367efe",
                Regions.AP_NORTHEAST_2
        );

        transferUtility = TransferUtility.builder().s3Client(new AmazonS3Client(credentialsProvider,
                Region.getRegion(Regions.AP_NORTHEAST_2))).context(context).build();
        TransferNetworkLossHandler.getInstance(context);
    }

    public void setCallback(TransferCallback callback) {
        this.callback = callback;
    }

    public TransferObserver getTransferObserver() {
        return transferObserver;
    }

    public void upload(int bucket, File file) {
        transferObserver = transferUtility.upload(
                context.getString(bucket),
                file.getName(),
                file
        );
        if (callback != null) {
            transferObserver.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    callback.onStateChanged(state);
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                }

                @Override
                public void onError(int id, Exception e) {
                    callback.onError(id, e);
                }
            });
        }
    }

    public void download(int bucket, File file) {
        transferObserver = transferUtility.download(
                context.getString(bucket),
                file.getName(),
                file
        );
        if (callback != null) {
            transferObserver.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    callback.onStateChanged(state);
                }

                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                }

                @Override
                public void onError(int id, Exception e) {
                    callback.onError(id, e);
                }
            });
        }
    }
}
