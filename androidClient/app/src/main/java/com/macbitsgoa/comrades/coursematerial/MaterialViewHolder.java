package com.macbitsgoa.comrades.coursematerial;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.material.snackbar.Snackbar;
import com.macbitsgoa.comrades.BuildConfig;
import com.macbitsgoa.comrades.GetGoogleSignInActivity;
import com.macbitsgoa.comrades.R;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;


/**
 * @author aayush singla
 */
public class MaterialViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final TextView tvFileName;
    private final TextView tvOwnerName;
    private final View rootView;
    private final CircularProgressBar donutProgress;
    private final TextView tvDownloadStatus;
    private final SimpleDraweeView iconDraweeView;
    private CourseMaterial data;

    public MaterialViewHolder(final View itemView) {
        super(itemView);
        rootView = itemView;
        tvFileName = itemView.findViewById(R.id.tv_file_name);
        iconDraweeView = itemView.findViewById(R.id.icon);
        tvOwnerName = itemView.findViewById(R.id.tv_owner_name);
        donutProgress = itemView.findViewById(R.id.donut_progress);
        tvDownloadStatus = itemView.findViewById(R.id.status);
    }

    /**
     * updates the view in recycler with the data and sets onClick listener to it.
     *
     * @param data  object of class @{@link CourseMaterial}
     */
    public void populate(CourseMaterial data) {
        this.data = data;
        tvOwnerName.setText("Added by " + data.getAddedBy());
        tvFileName.setText(data.getFileName());
        Boolean isDownloading = data.getDownloading();
        Boolean isWaiting = data.getWaiting();
        Boolean fileAvailable = data.getFileAvailable();

        if (fileAvailable) {
            donutProgress.setProgress(100);
            tvDownloadStatus.setText("Click to Open");
        } else if (isDownloading) {
            donutProgress.setProgress(data.getProgress());
            tvDownloadStatus.setText("Downloading");
        } else if (isWaiting) {
            donutProgress.enableIndeterminateMode(true);
            tvDownloadStatus.setText("Waiting");
        } else {
            donutProgress.setProgress(0);
            tvDownloadStatus.setText("Click to Download");
        }

        rootView.setOnClickListener(this);
        if (data.getIconLink() != null)
            iconDraweeView.setImageURI(data.getIconLink());


    }

    private static void handleSignInAndStorage(final Context context) {
        final Intent intent = new Intent(context, GetGoogleSignInActivity.class);
        context.startActivity(intent);
    }

    private void openFile(CourseMaterial obj) {
        final File file = new File(obj.getFilePath() + obj.getFileName() + obj.getExtension());
        final Intent generic = new Intent();
        final Uri uri =
                FileProvider.getUriForFile(itemView.getContext(), BuildConfig.APPLICATION_ID, file);
        generic.setAction(Intent.ACTION_VIEW);
        generic.setDataAndType(uri, obj.getMimeType());
        generic.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        itemView.getContext().startActivity(generic);
    }


    @Override
    public void onClick(View view) {
        final Context context = rootView.getContext();
        final boolean signedIn = GoogleSignIn.getLastSignedInAccount(context) != null;
        boolean storagePermission = true;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            storagePermission =
                    context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED;
        }

        if (signedIn && storagePermission) {
            if (data.getFileAvailable()) {
                openFile(data);
            } else if (!data.getDownloading()) {
                data.setDownloading(false);
                data.setWaiting(true);
                MaterialVm materialVm = ViewModelProviders.of((AppCompatActivity) context).get(MaterialVm.class);
                materialVm.update(data);

                final Intent downloadIntent =
                        DownloadService.makeDownloadIntent(itemView.getContext(), data.getLink(),
                                data.getFileName(), data.getExtension(), data.getFilePath(),
                                data.getId(), data.getFileSize());
                itemView.getContext().startService(downloadIntent);
            }
        } else if (signedIn) {
            Snackbar.make(rootView, context.getString(R.string.storage_permission_needed),
                    Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.allow), v ->
                            handleSignInAndStorage(context))
                    .show();
        } else {
            Snackbar.make(rootView, context.getString(R.string.login_to_download_file),
                    Snackbar.LENGTH_LONG)
                    .setAction(context.getString(R.string.login), v ->
                            handleSignInAndStorage(context))
                    .show();
        }
    }
}
