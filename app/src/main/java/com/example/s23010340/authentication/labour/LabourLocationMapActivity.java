package com.example.s23010340.authentication.labour;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;
import com.example.s23010340.R;

public class LabourLocationMapActivity extends AppCompatActivity {
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_DISTRICT = "extra_district";
    public static final String EXTRA_CITY = "extra_city";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.labour_location_map);

        String location = getIntent().getStringExtra(EXTRA_LOCATION);
        String district = getIntent().getStringExtra(EXTRA_DISTRICT);
        String city = getIntent().getStringExtra(EXTRA_CITY);

        if (location == null) location = "";
        if (district == null) district = "";
        if (city == null) city = "";

        String query = location;
        if (!city.isEmpty()) query = query + " " + city;
        if (!district.isEmpty()) query = query + " " + district;
        if (query.trim().isEmpty()) query = "Kandy Sri Lanka";

        String encodedQuery = Uri.encode(query);
        
        // Try to open Google Maps app first
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + encodedQuery));
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
            finish();
            return;
        }

        // Fallback to WebView with Maps Embed API
        WebView mapWebView = findViewById(R.id.map_web_view);
        WebSettings settings = mapWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        mapWebView.setWebViewClient(new WebViewClient());
        
        String mapsApiKey = getMapsApiKey();
        if (mapsApiKey.isEmpty()) {
            mapWebView.loadUrl("https://www.google.com/maps/search/?api=1&query=" + encodedQuery);
        } else {
            String embedUrl = "https://www.google.com/maps/embed/v1/place?key="
                + Uri.encode(mapsApiKey)
                + "&q="
                + encodedQuery;
            String html = "<!DOCTYPE html><html><body style='margin:0;padding:0;'>"
                + "<iframe width='100%' height='100%' style='border:0;' loading='lazy' allowfullscreen "
                + "referrerpolicy='no-referrer-when-downgrade' src='"
                + embedUrl
                + "'></iframe></body></html>";
            mapWebView.loadDataWithBaseURL("https://www.google.com", html, "text/html", "UTF-8", null);
        }

        findViewById(R.id.map_back_button).setOnClickListener(view -> finish());
    }

    private String getMapsApiKey() {
        try {
            ApplicationInfo appInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appInfo = getPackageManager().getApplicationInfo(
                    getPackageName(),
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA)
                );
            } else {
                appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            }
            if (appInfo.metaData == null) return "";
            Object keyObj = appInfo.metaData.get("com.google.android.geo.API_KEY");
            if (keyObj instanceof String) return ((String) keyObj).trim();
            return "";
        } catch (PackageManager.NameNotFoundException exception) {
            return "";
        }
    }
}
