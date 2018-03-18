/*
 * This is the source code of the Hydrogen Atom Orbitals app for Android.
 * It is licensed under the MIT License.
 *
 * Copyright (c) 2015-2018 Volodymyr Vovchenko.
 */

package com.vlvolad.hydrogenatom;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Volodymyr on 18.05.2015.
 */
public class OrbitalDialog extends Activity {
    private TextView tvn, tvl, tvm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTitle(R.string.orbital_selection);
        setContentView(R.layout.orbital_selection);

        tvn = (TextView)findViewById(R.id.tvnn);
        tvl = (TextView)findViewById(R.id.tvln);
        tvm = (TextView)findViewById(R.id.tvmn);

        SharedPreferences settings =  PreferenceManager.getDefaultSharedPreferences(
                this);

        tvn.setText("" + settings.getInt("n", 1));
        tvl.setText("" + settings.getInt("l", 0));
        tvm.setText("" + settings.getInt("m", 0));

        findViewById(R.id.bSubn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = Integer.parseInt(tvn.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                if (n-1>l) {
                    n--;
                    tvn.setText("" + n);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bAddn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = Integer.parseInt(tvn.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                if (n+1<=25) {
                    n++;
                    tvn.setText("" + n);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bSubl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = Integer.parseInt(tvn.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                int m = Integer.parseInt(tvm.getText().toString());
                if (l-1>=Math.abs(m)) {
                    l--;
                    tvl.setText("" + l);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bAddl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = Integer.parseInt(tvn.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                int m = Integer.parseInt(tvm.getText().toString());
                if (l+1<n) {
                    l++;
                    tvl.setText("" + l);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bSubm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = Integer.parseInt(tvn.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                int m = Integer.parseInt(tvm.getText().toString());
                if (Math.abs(m-1)<=l) {
                    m--;
                    tvm.setText("" + m);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.bAddm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = Integer.parseInt(tvn.getText().toString());
                int l = Integer.parseInt(tvl.getText().toString());
                int m = Integer.parseInt(tvm.getText().toString());
                if (Math.abs(m+1)<=l) {
                    m++;
                    tvm.setText("" + m);
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.numbers_condition, Toast.LENGTH_SHORT).show();
                }
            }
        });

        boolean isReal = settings.getBoolean("wave_function_real", true);
        if (isReal) {
            ((RadioButton)findViewById(R.id.radioReal)).setChecked(true);
            //((RadioButton)findViewById(R.id.radioComplex)).setChecked(false);
        }
        else {
            ((RadioButton)findViewById(R.id.radioComplex)).setChecked(true);
        }
    }

    /**
     * Callback method defined by the View
     * @param v
     */
    public void okDialog(View v) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                this).edit();

        editor.putInt("n", Integer.parseInt(tvn.getText().toString()));
        editor.putInt("l", Integer.parseInt(tvl.getText().toString()));
        editor.putInt("m", Integer.parseInt(tvm.getText().toString()));

        boolean isReal = ((RadioButton)findViewById(R.id.radioReal)).isChecked();
        editor.putBoolean("wave_function_real", isReal);

        editor.commit();

        HAGLRenderer.mAtom.toCont = true;

        OrbitalDialog.this.finish();
    }
    public void cancelDialog(View v) {
        OrbitalDialog.this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
