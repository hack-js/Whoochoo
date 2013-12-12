/*
 * Copyright (C) 2013 Jongha Kim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wisedog.android.whooing.dialog;

import com.actionbarsherlock.app.SherlockDialogFragment;

import net.wisedog.android.whooing.R;
import net.wisedog.android.whooing.widget.WiTextView;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * @author Wisedog(me@wisedog.net)
 *
 */
public class AboutDialog extends SherlockDialogFragment {

    static public AboutDialog newInstance() {
        AboutDialog f = new AboutDialog();
        return f;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(android.support.v4.app.DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Dialog);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.about, container, false);
        Button btnEmailWhooing = (Button) v.findViewById(R.id.about_btn_contact_whooing);
        if(btnEmailWhooing != null){
            btnEmailWhooing.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    String emailAddress = getActivity().getString(R.string.about_app_contact_whooing_email);
                    Intent it = new Intent(Intent.ACTION_SEND);   
                    it.setType("message/rfc822");   
                    it.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});   
                    
                    startActivity(Intent.createChooser(it, getActivity().getString(R.string.about_app_send_email)));  
                }
            });
        }
        
        Button btnEmailDeveloper = (Button) v.findViewById(R.id.about_btn_contact_developer);
        if(btnEmailDeveloper != null){
            btnEmailDeveloper.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    String emailAddress = getActivity().getString(R.string.about_app_contact_developer_email);
                    Intent it = new Intent(Intent.ACTION_SEND);
                    it.setType("message/rfc822");
                    it.putExtra(Intent.EXTRA_EMAIL, new String[]{emailAddress});
                    startActivity(Intent.createChooser(it, getActivity().getString(R.string.about_app_send_email)));  
                }
            });
        }
        
        WiTextView versionText = (WiTextView)v.findViewById(R.id.about_app_version);
        if(versionText != null){
            versionText.setText(getActivity().getString(R.string.about_app_version) + getAppVersion());
        }
        return v;
    }
    
    public String getAppVersion() {
        String packageName = null;
        String appVersion = null;
        try {
            PackageInfo _package;
            packageName = getActivity().getPackageName();
            _package = getActivity().getPackageManager().getPackageInfo(packageName, 0);
            appVersion = _package.versionName;
        } catch (NameNotFoundException e1) {
            appVersion = "Unknown";
        }
        return appVersion;
    }

}
