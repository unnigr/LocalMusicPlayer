package com.example.localmusicplayer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private static final int PICK_FOLDER = 1001;
    private WebView webView;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        setContentView(webView);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        webView.loadUrl("file:///android_asset/index.html");
    }

    private class AndroidBridge {
        @JavascriptInterface public void chooseMusicFolder() {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
            startActivityForResult(i, PICK_FOLDER);
        }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_FOLDER || resultCode != RESULT_OK || data == null || data.getData() == null) return;
        Uri tree = data.getData();
        try { getContentResolver().takePersistableUriPermission(tree, Intent.FLAG_GRANT_READ_URI_PERMISSION); } catch (Exception ignored) {}
        List<Track> tracks = new ArrayList<>();
        scanTree(tree, tracks);
        Collections.sort(tracks, Comparator.comparing(t -> t.name.toLowerCase()));
        JSONArray arr = new JSONArray();
        for (Track t : tracks) {
            JSONObject o = new JSONObject();
            try { o.put("name", t.name); o.put("uri", t.uri.toString()); } catch (Exception ignored) {}
            arr.put(o);
        }
        String js = "window.onAndroidFiles(" + JSONObject.quote(arr.toString()) + ");";
        webView.post(() -> webView.evaluateJavascript(js, null));
    }

    private void scanTree(Uri tree, List<Track> out) {
        android.database.Cursor c = null;
        try {
            Uri children = android.provider.DocumentsContract.buildChildDocumentsUriUsingTree(tree, android.provider.DocumentsContract.getTreeDocumentId(tree));
            c = getContentResolver().query(children, new String[]{android.provider.DocumentsContract.Document.COLUMN_DOCUMENT_ID, android.provider.DocumentsContract.Document.COLUMN_DISPLAY_NAME, android.provider.DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
            if (c == null) return;
            while (c.moveToNext()) {
                String id = c.getString(0), name = c.getString(1), mime = c.getString(2);
                Uri doc = android.provider.DocumentsContract.buildDocumentUriUsingTree(tree, id);
                if (android.provider.DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) scanTree(doc, out);
                else if (isAudio(name, mime)) out.add(new Track(name, doc));
            }
        } catch (Exception ignored) {} finally { if (c != null) c.close(); }
    }

    private boolean isAudio(String name, String mime) {
        if (mime != null && mime.startsWith("audio/")) return true;
        String n = name == null ? "" : name.toLowerCase();
        return n.endsWith(".mp3") || n.endsWith(".wav") || n.endsWith(".ogg") || n.endsWith(".m4a") || n.endsWith(".flac") || n.endsWith(".aac") || n.endsWith(".opus");
    }
    private static class Track { final String name; final Uri uri; Track(String n, Uri u){name=n;uri=u;} }

    @Override public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack(); else super.onBackPressed();
    }
}
