package com.gvourr.currentstobrowser;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class ShareActivity extends Activity {
	@Override
	public void onStart() {
		super.onStart();

		// Get intent data
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			String url = (String) bundle.get(Intent.EXTRA_TEXT);
			url = url.substring(url.indexOf("http"));
			Log.v("CtB", "Incoming intent data: " + url);
			// if it is a currents URL, get the article URL and show a spinner
			if ((url.startsWith("https://www.google.com/producer"))
					|| (url.startsWith("http://goo.gl/mag"))) { // added case of shortened url
				showDialog(0);
				ArticleURL task = new ArticleURL(this);
				task.execute(url);
				// otherwise, just show a browser with the URL
			} else {
				startBrowser(url);
			}
		}
	}

	// Launches browser if it is a valid url, otherwise shows a toast
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

	// Spinner shown when getting article URL
	@Override
	protected Dialog onCreateDialog(int id) {
		ProgressDialog progressDialog = new ProgressDialog(ShareActivity.this);
		progressDialog.setMessage("Retrieving article URL");
		progressDialog.setCancelable(false);
		return progressDialog;
	}
}

// Turns google producer URL into article URL
class ArticleURL extends AsyncTask<String, Void, String> {
	private final ShareActivity ref;

	public ArticleURL(ShareActivity share) {
		ref = share;
	}

	@Override
	protected String doInBackground(String... params) {
		String url = params[0];

		try {
			URLConnection con = new URL(url).openConnection();
			// Setting the user agent as a desktop browser so google producer redirects us to
			// article
			con.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/536.5 (KHTML, like Gecko) Chrome/19.0.1084.9 Safari/536.5");
			// Connect to article to get redirection url
			con.connect();
			InputStream is = con.getInputStream();
			is.close();
			url = con.getURL().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}

	@Override
	protected void onPostExecute(String url) {
		// if url isn't the final one continue until got it
		if ((url.startsWith("https://www.google.com/producer"))
				|| (url.startsWith("http://goo.gl/mag"))) {
			ref.showDialog(0);
			ArticleURL task = new ArticleURL(ref);
			task.execute(url);
			// otherwise, just show a browser with the URL
		} else {
			ref.startBrowser(url);
		}
	}
}
