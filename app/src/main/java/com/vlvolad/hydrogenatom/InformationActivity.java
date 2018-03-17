/*
 * This is the source code of the Hydrogen Atom Orbitals app for Android.
 * It is licensed under the MIT License.
 *
 * Copyright (c) 2015-2018 Volodymyr Vovchenko.
 */
package com.vlvolad.hydrogenatom;

/**
 * Created by Volodymyr on 02.05.2015.
 */

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class InformationActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        setTitle(R.string.information);

        TextView tver = (TextView) findViewById(R.id.version_text);
        tver.setText(getText(R.string.version).toString() + " " + BuildConfig.VERSION_NAME);

        TextView tv = (TextView) findViewById(R.id.google_play);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (Exception e) {
//                    Log.d("Information", "Message =" + e);

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                PreferenceManager.getDefaultSharedPreferences(InformationActivity.this).edit().putBoolean("rate_clicked", true).apply();
            }
        });

        TextView tv2 = (TextView) findViewById(R.id.wikipedia_link);
        makeTextViewHyperlink(tv2);
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getText(R.string.wikipedia_url).toString())));
            }
        });

        TextView tv3 = (TextView) findViewById(R.id.more_apps);
        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=Voladd")));
                } catch (Exception e) {
//                    Log.d("Information", "Message =" + e);
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Voladd")));
                }
            }
        });

        TextView tv4 = (TextView) findViewById(R.id.quantumoscillator_link);
        tv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String appPackageName = "com.vlvolad.quantumoscillator";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (Exception e) {
//                    Log.d("Information", "Message =" + e);

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        TextView tv5 = (TextView) findViewById(R.id.share_link);
        tv5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getText(R.string.share_subject).toString());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getText(R.string.share_text).toString());
                startActivity(Intent.createChooser(sharingIntent, getText(R.string.share_via).toString()));
            }
        });

        TextView tv6 = (TextView) findViewById(R.id.github_link);
        tv6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getText(R.string.github_url).toString())));
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Sets a hyperlink style to the textview.
     */
    public static void makeTextViewHyperlink(TextView tv) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(tv.getText());
        ssb.setSpan(new URLSpan("#"), 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(ssb, TextView.BufferType.SPANNABLE);
    }
}
