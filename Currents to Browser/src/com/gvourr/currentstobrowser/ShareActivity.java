package com.gvourr.currentstobrowser;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ShareActivity extends Activity {
    public void onStart() {
        super.onStart();
        
        //Get intent data
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null){
        	String url = (String)bundle.get(Intent.EXTRA_TEXT);
        	Log.v("CtB", "Incoming intent data: " + url);
        	//if it is a currents URL, get the article URL and show a spinner
        	if (url.startsWith("https://www.google.com/producer")) {
        		showDialog(0);
	        	ArticleURL task = new ArticleURL(this);
	        	task.execute(url);
	        //otherwise, just show a browser with the URL
        	} else {
        		startBrowser(url);
        	}
        }
    }
    
    //Launches browser if it is a valid url, otherwise shows a toast
    public void startBrowser(String url) {
		if (url.startsWith("http://") || url.startsWith("https://")) {
	    	Uri webpage = Uri.parse(url);
	    	Intent webIntent = new Intent(Intent.ACTION_VIEW, webpage);
	    	Log.v("CtB", "Launching browser for url: " + url);
	    	this.startActivity(webIntent);
		} else {
			Toast.makeText(getApplicationContext(), "Not a valid URL", Toast.LENGTH_LONG).show();
		}
    }
    
    //Spinner shown when getting article URL
    protected Dialog onCreateDialog(int id) {
    	ProgressDialog progressDialog = new ProgressDialog(ShareActivity.this);
        progressDialog.setMessage("Retrieving article URL");
        progressDialog.setCancelable(false);
        return progressDialog;
    }
}

//Turns google producer URL into article URL
class ArticleURL extends AsyncTask<String, Void, String> {
	private ShareActivity ref;
	
	public ArticleURL(ShareActivity share){
		ref = share;
	}
	
	protected String doInBackground(String... params) {
		String url = params[0];
        
        try {
        	URLConnection con = new URL(url).openConnection();
        	//Setting the user agent as a desktop browser so google producer redirects us to article
        	con.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.9 Safari/536.5");
        	//Connect to article to get redirection url
        	con.connect();
        	InputStream is = con.getInputStream();
        	is.close();
        	url = con.getURL().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return url;
	}
	
	protected void onPostExecute(String url) {
		ref.startBrowser(url);
	}
}
