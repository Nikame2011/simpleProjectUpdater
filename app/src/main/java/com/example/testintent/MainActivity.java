package com.example.testintent;

import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApkChecksum;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.UseFeature;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public int PERMISSION_REQUEST_CODE = 1332;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         context = getApplication().getApplicationContext();
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        loadfiles();
    }

    private void loadfiles(){
        //preferenceManager.get

        //TODO здесь загружать из преференс 2 вещи - ид загрузки - если что-то начинало скачиваться до этого
        //и ссылку на файл версии, если чтото уже скачалось но не установилось
        //если ни того ни другого нет, но надо обновиться, тогда как сейчас - проверяем папку довнлоад, ну мало ли... а потом качаем

        //получаем из преференс список уже обновляемых файлов
        //перебираем их и сравниваем с общим списком поддерживаемых пакетов
        //если был в преференс - проверяем, есть ли ид скачивания - если есть, проверяем статус загрузки и как-то вешаем листенер снова?
        //если нет ида скачивания - проверяем наличие ссылки на скачанный апк - если есть, запускаем установку
        //если нет ссылки на апк - обновляем серверную инфу и решаем обновлять или нет
        //если нет в преференс - создаём его с нуля, проверяем на сервере статус версии и определяем, надо ли обновлять, то есть find need update


        String packageName=findNeedUpdate();//TODO параллельная загрузка и обновление нескольких приложений. String[] = find и потом их перебор
        if(!packageName.equals(""))
            findUpdatesFile(packageName);
    }

    private void findUpdatesFile(String targetPackage){
        File directoryForFileSaving = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File[] list = directoryForFileSaving.listFiles((dir, name) -> name.toLowerCase().endsWith(".apk")); //получаем все файлы в загрузках типа apk
        for (File apk : list) {
            try (ApkFile apkFile = new ApkFile(apk)) { //для каждого файла из списка проверяем название и версию
                ApkMeta apkMeta = apkFile.getApkMeta();
                if(apkMeta.getPackageName().equalsIgnoreCase(targetPackage)){
                    if(isTargetVersion(apkMeta.getVersionCode(),0,0/*target.getVersionCode(),target.getbaseVersionCode()*/)){//если версия подходит для обновления, обновляем
                        setUpNewVersion(apk);
                        //TODO алерт с предложением обновить позже или прямо сейчас
                        //TODO алерт перенести в сетап
                        AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(this);
                        alertBuilder.setCancelable(false);
                        alertBuilder.setTitle("Обновление приложения");
                        alertBuilder.setMessage("Новая версия приложения готова к установке. Установить?");
                        alertBuilder.setPositiveButton("ДА", (dialogInterface, i) -> {
                                    setUpNewVersion(apk);
                            //устанавливаем
                                }
                        );
                        AlertDialog alert = alertBuilder.create();
                        alert.show();
                        return;
                    }
                }
                //System.out.println(apkMeta.getPackageName());
                //System.out.println(apkMeta.getVersionCode());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        downloadFile();//если дошли до сюда - на устройстве не найдено нужной версии, скачиваем
    }

    private void downloadFile(){
        registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        preferenceManager = PreferenceManager.getDefaultSharedPreferences(this);
        downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse("http://speedtest.ftp.otenet.gr/files/test10Mb.db");//Uri.parse(/*откуда загружаем*/);//TODO здесь указывать адрес для загрузки
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
        long download_id = downloadManager.enqueue(request);
        SharedPreferences.Editor PrefEdit = preferenceManager.edit();
        PrefEdit.putLong(Download_ID, download_id);//сохраняем в преференс ид загрузчика, так при перезапуске можно будет его снова получить и проверить как он там
        PrefEdit.commit();

    }

    SharedPreferences preferenceManager;
    DownloadManager downloadManager;
    String Download_ID = "DOWNLOAD_ID";

    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(preferenceManager.getLong(Download_ID, 0));
                Cursor cursor = downloadManager.query(query);
//TODO крч, это говно на 12 андойде не работает надо разбираться и проверять на нескольких устройствах
                if(cursor.moveToFirst()){
                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = cursor.getInt(columnIndex);
                    int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                    int reason = cursor.getInt(columnReason);

                    if(status == DownloadManager.STATUS_SUCCESSFUL){
                        //Retrieve the saved download id
                        long downloadID = preferenceManager.getLong(Download_ID, 0);
                        long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                        if (downloadID == id) {//проверяем, что скачалось то, что нам нужно
                            //запускаем поиск этого файла или получаем ссылку на него здесь?
//скачалось что надо, запускаем установку
                            Uri uri=downloadManager.getUriForDownloadedFile(id);
                        }
                     /*   ParcelFileDescriptor file;
                        try {
                            file = downloadManager.openDownloadedFile(downloadID);
                            Toast.makeText(AndroidDownloadActivity.this,
                                    "File Downloaded: " + file.toString(),
                                    Toast.LENGTH_LONG).show();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            Toast.makeText(AndroidDownloadActivity.this,
                                    e.toString(),
                                    Toast.LENGTH_LONG).show();
                        }*/

                    }else if(status == DownloadManager.STATUS_FAILED){
                        Toast.makeText(MainActivity.this,
                                "FAILED!\n" + "reason of " + reason,
                               Toast.LENGTH_LONG).show();
                    }else if(status == DownloadManager.STATUS_PAUSED){
                     //   Toast.makeText(AndroidDownloadActivity.this,
                    //            "PAUSED!\n" + "reason of " + reason,
                     //           Toast.LENGTH_LONG).show();
                    }else if(status == DownloadManager.STATUS_PENDING){
                    //    Toast.makeText(AndroidDownloadActivity.this,
                    //            "PENDING!",
                     //           Toast.LENGTH_LONG).show();
                    }else if(status == DownloadManager.STATUS_RUNNING){
                     //   Toast.makeText(AndroidDownloadActivity.this,
                     //           "RUNNING!",
                     //           Toast.LENGTH_LONG).show();
                    }
                }
            }

        };



    private String findNeedUpdate(){
        //TODO здесь же возвращать текущую версию
        /*  PackageManager manager = context.getPackageManager();
            PackageInfo info = null;
            try {
                info = manager.getPackageInfo(
                        context.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return info.versionCode
            */
        //TODO здесь же возвращать нужную версию
        //TODO здесь проверка необходимости загрузки, которая показывает какой файл нужно обновлять
        //TODO если нужно обновить другое приложение, получать его текущую версию как-нибудь
        //TODO здесь же нужно отдавать ссылку на скачивание новой версии
        return "fifi";//"com.git.call03";//context.getApplicationContext().getPackageName();
    }

   // long targetVersion=0;

    private boolean isTargetVersion(long findVersion,long targetVersion,long baseVersion){//проверяем версию обнаруженного приложения
        if (targetVersion==0) //если с сервера не передали версию нового приложения, то просто ищем поновее
        {
            return baseVersion<findVersion;
        }
        else{//если сервер прислал конкретную версию, ищем её в скачанных
            return targetVersion==findVersion;
        }
    }

    private void setUpNewVersion(File file){
        //todo параметр - неотложная установка, для случаев когда стартуем с уже загруженным обновлением или если обновление критическое, тогда стартует обновление без вопросов к пользователю
        //Context context = getActivity().getApplication().getApplicationContext();
        //String packName = context.getApplicationContext().getPackageName();
//todo по алерту отложенной установки сохранять путь к файлу в преференс менеджер, если он там есть, то при запуске приложения устанавливать сразу же
        Intent intent;
        //TODO выше андроид 10 использовать PACKAGE INSTALLER хз как, правда
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            //File directoryForFileSaving = Environment
            //        .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            //File newApk = new File(directoryForFileSaving, nameFile);

            Uri apkURI = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
            intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setData(apkURI);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            //File directoryForFileSaving = Environment
             //       .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            //File newApk = new File(directoryForFileSaving, nameFile);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        startActivity(intent);
    }

    ArrayList<DownloadPackage> downloadPackages=new ArrayList<>();

    private class DownloadPackage{
        //ЗАПОЛНЯЕМ ПРИ СОЗДАНИИ
        String packageName; //имя обновляемого пакета
        long baseVersion; //текущая версия

        //ЗАПОЛНЯЕМ ПО МЕРЕ РАБОТЫ
        String uriFile; //адрес скачанного файла, если скачан. появляется здесь, если пользователь отказался обновляться прямо сейчас, чтобы получить доступ к нему при новом запуске
        String managerID; //айди менеджера загрузок, если файл начал загружаться

        //ПОЛУЧАЕМ С СЕРВЕРА//
        boolean needUpdate; //надо ли обновить
        long targetVersion; //версия, которую хотим, может быть==0 если хотим просто поновее
        String urlForLoad; //адрес для загрузки нового файла
        boolean isCritical; //указывает, может ли пользователь работать во время загрузки и может ли отложить установку

        public DownloadPackage(String packageName, long baseVersion){
            this.packageName=packageName;
            this.baseVersion=baseVersion;
        }

        public String getUriFile() {
            return uriFile;
        }
        public void setUriFile(String uriFile) {
            this.uriFile = uriFile;
        }

        public String getManagerID() {
            return managerID;
        }
        public void setManagerID(String managerID) {
            this.managerID = managerID;
        }

        public boolean isNeedUpdate() {
            return needUpdate;
        }
        public void setNeedUpdate(boolean needUpdate) {
            this.needUpdate = needUpdate;
        }

        public long getTargetVersion() {
            return targetVersion;
        }
        public void setTargetVersion(long targetVersion) {
            this.targetVersion = targetVersion;
        }

        public String getUrlForLoad() {
            return urlForLoad;
        }
        public void setUrlForLoad(String urlForLoad) {
            this.urlForLoad = urlForLoad;
        }

        public boolean isCritical() {
            return isCritical;
        }
        public void setCritical(boolean critical) {
            isCritical = critical;
        }
    }

}