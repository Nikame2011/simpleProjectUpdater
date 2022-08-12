package com.example.testintent;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApkChecksum;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.UseFeature;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public int PERMISSION_REQUEST_CODE = 1332;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            boolean showRationale = ActivityCompat
                    .shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //if (showRationale) {
                confirmAllPermission();
           // }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { //если версия выше 10, нужно дополнительное разрешение чтобы увидеть не мультимедиа файлы.
            if(!Environment.isExternalStorageManager()){

                AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(this);
                alertBuilder.setCancelable(false);
                alertBuilder.setTitle("Необходимо предоставить дополнительные разрешения");
                alertBuilder.setMessage("Для корректной работы автообновлений приложению требуется разрешение на доступ ко всем файлам. Будет открыто меню настроек, выберите в нём данное приложение и предоставьте разрешение.");
                alertBuilder.setPositiveButton("ОК", (dialogInterface, i) -> {
                            Intent in=new Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                            startActivity(in);
                        }
                );
                AlertDialog alert = alertBuilder.create();
                alert.show();
            }
            else{
                loadfiles();
            }
        }
        else{
            loadfiles();
        }



      /*  Context context = getApplication().getApplicationContext();
        String packName = context.getApplicationContext().getPackageName();


        File directoryForFileSaving = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File newApk = new File(directoryForFileSaving, "call03-release-1.8.5927.apk");//call03-debug-1.8.6335.apk
        if(newApk.exists()){
            try (ApkFile apkFile = new ApkFile(directoryForFileSaving)) {
                ApkMeta apkMeta = apkFile.getApkMeta();
                System.out.println(apkMeta.getLabel());
                System.out.println(apkMeta.getPackageName());
                System.out.println(apkMeta.getVersionCode());
                for (UseFeature feature : apkMeta.getUsesFeatures()) {
                    System.out.println(feature.getName());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkURI = FileProvider.getUriForFile(context, packName + ".provider", newApk);
                intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkURI);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {

                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(newApk), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
        }*/
    }

    protected void confirmAllPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            //Manifest.permission.ACCESS_FINE_LOCATION, /*Manifest.permission.CALL_PHONE,*/
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            //Manifest.permission.READ_PHONE_STATE,
                            //Manifest.permission.READ_CONTACTS/*, Manifest.permission.WRITE_CONTACTS*//*, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
                           // Manifest.permission.LOCATION_HARDWARE*/
                            },

                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
      /*  if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                for(int i=0;i<grantResults.length;i++) { //для каждого из запрошенных разрешений проверяем, что это за разрешения, т.к.
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED ) {//вызывается из нескольких мест с разными запросами
                        boolean showRationale;
                        showRationale = ActivityCompat
                                .shouldShowRequestPermissionRationale(this, permissions[i]);
                        if (showRationale) {
                            confirmAllPermission();
                        } else {
                            switch (permissions[i]) {
                                case Manifest.permission.ACCESS_FINE_LOCATION:
                                    buildAlertMessagePermission(R.string.location_permission_explanation,
                                            R.string.grant,
                                            R.string.close_application,
                                            R.string.open_location_permission,
                                            true);
                                    break;

                                case Manifest.permission.READ_EXTERNAL_STORAGE:
                                    buildAlertMessagePermission(R.string.external_storage_permission_explanation,
                                            R.string.grant,
                                            R.string.close_application,
                                            R.string.open_external_storage_permission,
                                            true);
                                    break;

                                case Manifest.permission.READ_PHONE_STATE:
                                    buildAlertMessagePermission(R.string.phone_permission_explanation,
                                            R.string.grant,
                                            R.string.close_application,
                                            R.string.open_phone_permission,
                                            true);
                                    break;

                                case Manifest.permission.READ_CONTACTS:
                                    buildAlertMessagePermission(R.string.contacts_permission_explanation,
                                            R.string.grant,
                                            R.string.cancel,
                                            R.string.open_contacts_permission,
                                            false);
                                    break;
                            }
                        }
                    }
                    else if(permissions[i].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        if (!mCurrentLocationListener.isGpsLocationProviderEnabled() && !mCurrentLocationListener
                                .isNetworkLocationProviderEnabled()) {
                            buildAlertMessageNoGps();
                            return;
                        }
                        sentRequestCurrentAddress();
                    }
                }
            }
        }*/
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        loadfiles();
    }

    private void loadfiles(){
        if(needUpdate()) {

            Context context = getApplication().getApplicationContext();
            String packName = context.getApplicationContext().getPackageName();
            File directoryForFileSaving = Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File[] list = directoryForFileSaving.listFiles((dir, name) -> name.toLowerCase().endsWith(".apk")); //получаем все файлы в загрузках типа apk
            for (File apk : list) {
                try (ApkFile apkFile = new ApkFile(apk)) { //для каждого файла из списка проверяем название и версию
                    ApkMeta apkMeta = apkFile.getApkMeta();
                    System.out.println(apkMeta.getPackageName());
                    System.out.println(apkMeta.getVersionCode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            File newApk = new File(directoryForFileSaving, "call03-release-1.8.5927.apk");//call03-debug-1.8.6335.apk
            if (newApk.exists()) {
                try (ApkFile apkFile = new ApkFile(newApk)) {
                    ApkMeta apkMeta = apkFile.getApkMeta();
                    System.out.println(apkMeta.getLabel());
                    System.out.println(apkMeta.getPackageName());
                    System.out.println(apkMeta.getVersionCode());
                    for (UseFeature feature : apkMeta.getUsesFeatures()) {
                        System.out.println(feature.getName());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean needUpdate(){
        return true;
    }


}