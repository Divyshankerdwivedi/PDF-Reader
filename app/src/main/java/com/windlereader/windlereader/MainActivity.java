package com.windlereader.windlereader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnPdfFileSelectListener {
    private PdfAdapter pdfAdapter;
    private List<File> pdfList;
    private RecyclerView recyclerView;
    private WebView mWebView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         runtimePermission();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu,menu);
        return true;

    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            mWebView.loadUrl("https://www.google.com/");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    private void runtimePermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE}, 1);

        if(!Environment.isExternalStorageManager()){
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }else{
            displayPdf();
        }
    }

//    public ArrayList<File> findPdf (File file)
//    {
//        ArrayList<File> arrayList = new ArrayList<>();
//
//        File[]  files = file.listFiles();
//         for (File singleFile:files)
//         {
//             if (singleFile.isDirectory() && !singleFile.isHidden())
//             {
//                 arrayList.addAll(findPdf(singleFile));
//             }
//             else
//                 {
//                     if (singleFile.getName().endsWith(".pdf")){
//                         arrayList.add(singleFile);
//                     }
//             }
//         }
//         return arrayList;
//    }

    private void displayPdf() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        pdfList = new ArrayList<>();
        Log.e("len", String.valueOf(Environment.getExternalStorageDirectory()));
        List<String> stringFiles = getPdfList();
        for(int i = 0; i < stringFiles.size(); i++){
            File curr = new File(stringFiles.get(i));
            Log.e("file", String.valueOf(curr));
            pdfList.add(curr);
        }
        pdfAdapter = new PdfAdapter(this, pdfList,this);
        recyclerView.setAdapter(pdfAdapter);
    }

    @Override
    public void onPdfSelected(File file) {
        startActivity(new Intent(MainActivity.this,DocumentActivity.class)
        .putExtra("path" , file.getAbsolutePath()));

    }

    protected List<String> getPdfList() {
        List<String> pdfList = new ArrayList<>();

        final String[] projection = new String[]{
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.DATE_ADDED,
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MIME_TYPE,
        };

        final String sortOrder = MediaStore.Files.FileColumns.DATE_ADDED + " DESC";

        final String selection = MediaStore.Files.FileColumns.MIME_TYPE + " = ?";

        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf");
        final String[] selectionArgs = new String[]{mimeType};

        Uri collection = MediaStore.Files.getContentUri("external");
        pdfList.addAll(getPdfList(collection, projection, selection, selectionArgs, sortOrder));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Downloads.getContentUri("external");
            pdfList.addAll(getPdfList(collection,projection, selection, selectionArgs, sortOrder));
        }

        return pdfList;
    }

    private List<String> getPdfList(Uri collection, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        List<String> pdfList = new ArrayList<>();

        try (Cursor cursor = getContentResolver().query(collection, projection, selection, selectionArgs, sortOrder)) {
            assert cursor != null;

            if (cursor.moveToFirst()) {
                int columnData = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                do {
                    pdfList.add((cursor.getString(columnData)));
                    Log.d("TAG", "getPdf: " + cursor.getString(columnData));
                    //you can get your pdf files
                } while (cursor.moveToNext());
            }
        }
        return pdfList;
    }

}