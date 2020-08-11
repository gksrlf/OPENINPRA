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

    public interface TransferCallback {
        void onStateChanged(TransferState state);

        void onError(int id, Exception e);
    }

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

    public TransferObserver upload(int bucket, String name, File file) {
        TransferObserver transferObserver = transferUtility.upload(
                context.getString(bucket),
                name,
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
        return transferObserver;
    }

    public TransferObserver download(int bucket, File file) {
        TransferObserver transferObserver = transferUtility.download(
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
        return transferObserver;
    }
}
