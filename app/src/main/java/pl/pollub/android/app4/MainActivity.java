package pl.pollub.android.app4;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
public class MainActivity extends AppCompatActivity {
    private EditText adresEt;
    private TextView rozmierPlikuTv;
    private TextView typPlikuTv;
    private Button pobierzInfoBt;
    private Button pobierzPlikBt;

    private ProgressBar postepPrb;
    private TextView postepTv;

    private static final String PREF_FILE_TYPE = "fileType";
    private static final String PREF_FILE_SIZE = "fileSize";

    private boolean isFirstRun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.adresEt = findViewById(R.id.adres_et);
        this.rozmierPlikuTv = findViewById(R.id.rozmiar_pliku_et);
        this.typPlikuTv = findViewById(R.id.typ_pliku_et);
        this.pobierzInfoBt = findViewById(R.id.pobierz_info_bt);
        this.pobierzInfoBt.setOnClickListener(view -> pobierzInfo());
        this.pobierzPlikBt = findViewById(R.id.pobierz_plik_bt);
        this.pobierzPlikBt.setOnClickListener(view -> pobierzPlik());
        this.postepPrb = findViewById(R.id.postep_pb);
        this.postepTv = findViewById(R.id.pobrano_bajtow_et);

        //zapisywanie danych Pobierz Informacje
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String fileType = sharedPreferences.getString(PREF_FILE_TYPE, "");
        int fileSize = sharedPreferences.getInt(PREF_FILE_SIZE, 0);
        typPlikuTv.setText(fileType);
        rozmierPlikuTv.setText(String.valueOf(fileSize));

        // czyszczenia danych w SharedPreferences
        clearSharedPreferences();
    }

    private void pobierzPlik() {
        pobierzInfo();
        this.pobierzPlikBt.setEnabled(false);//przycisk
        DownloadService.uruchomUsluge(this,this.adresEt.getText().toString());

    }

    private void pobierzInfo() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            final FileInfo fileInfo = pobierzInfo(this.adresEt.getText().toString());
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                this.typPlikuTv.setText(fileInfo.getFileType());
                this.rozmierPlikuTv.setText(Integer.toString(fileInfo.fileSize));
                // Zapisz dane w SharedPreferences
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_FILE_TYPE, fileInfo.getFileType());
                editor.putInt(PREF_FILE_SIZE, fileInfo.getFileSize());
                editor.apply();
            });
        });
    }


    private FileInfo pobierzInfo(String adres) {
        HttpsURLConnection polaczenie = null;
        final FileInfo fileInfo = new FileInfo();
        try {
            URL url = new URL(adres);
            polaczenie = (HttpsURLConnection) url.openConnection();
            polaczenie.setRequestMethod("GET");
            fileInfo.setFileSize(polaczenie.getContentLength());
            fileInfo.setFileType(polaczenie.getContentType());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(polaczenie != null){
                polaczenie.disconnect();
            }
        }
        return fileInfo;
    }
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(this.broadcastReceiver, new IntentFilter(DownloadService.POWIADOMIENIE));

        // Wyłącz przycisk, jeśli aplikacja została uruchomiona przez powiadomienie
        boolean isLaunchedFromNotification = getIntent().getBooleanExtra("isLaunchedFromNotification", false);
        if (isLaunchedFromNotification) {
            pobierzPlikBt.setEnabled(false);
        }
    }


    protected void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(this.broadcastReceiver);
        super.onPause();
    }

    class FileInfo {
        private String fileType;
        private int fileSize;

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public int getFileSize() {
            return fileSize;
        }

        public void setFileSize(int fileSize) {
            this.fileSize = fileSize;
        }
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ProgressInfo progressInfo = intent.getParcelableExtra(DownloadService.PROGRESS_INFO_KEY);
            MainActivity.this.postepPrb.setProgress(progressInfo.getProgressValue());
            MainActivity.this.postepTv.setText(Integer.toString(progressInfo.getDownloadBytes()));

            //przycisk wygasniety
            if (progressInfo.getProgressValue() == 100) {
                MainActivity.this.pobierzPlikBt.setEnabled(true);
            }
        }
    };
    private void clearSharedPreferences() {
        //czyszczenie danych z pobierz informacje
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }

}