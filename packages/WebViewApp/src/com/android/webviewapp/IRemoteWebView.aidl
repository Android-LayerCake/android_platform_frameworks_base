package com.android.webviewapp;

import java.lang.String;

interface IRemoteWebView {

    void setJavaScriptEnabled(boolean b);
    void setJavaScriptCanOpenWindowsAutomatically(boolean b);
    void setVerticalScrollBarEnabled(boolean b);
    void loadUrl(String url);
    void loadDataWithBaseURL(String baseUrl, String data,
            String mimeType, String encoding, String historyUrl);
    void shouldSaveCookies(boolean b);
    void shouldLoadLinksInWebView(boolean b);

}
