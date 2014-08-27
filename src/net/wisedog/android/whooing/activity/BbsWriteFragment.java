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
package net.wisedog.android.whooing.activity;

import org.json.JSONException;
import org.json.JSONObject;

import net.wisedog.android.whooing.Define;
import net.wisedog.android.whooing.R;
import net.wisedog.android.whooing.network.ThreadRestAPI;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A fragment for writing/modifying article/comment on BBS 
 *
 */
public class BbsWriteFragment extends Fragment {
    public static final int MODE_WRITE_ARTICLE = 0;
    public static final int MODE_MODIFY_ARTICLE = 1;
    public static final int MODE_MODIFY_REPLY = 2;
    
    /** Board type(free, counseling ...) */
    private int mBoardType = -1;
    /** subject string */
    private String mSubject = null;
    /** content string*/
    private String mContent = null;
    private int mMode = 0;
    private int mBbsId = 0;
	private String mCommentId = null;
    
    
    public void setData(int mode, int boardType, String subject, String content, int bbsid){
        mSubject = subject;
        mContent = content;
        mBoardType = boardType;
        mMode = mode;
        mBbsId = bbsid;
    }
    
    public void setData(int mode, int boardType, String subject, String content, int bbsid, String commentId){
        mSubject = subject;
        mContent = content;
        mBoardType = boardType;
        mMode = mode;
        mBbsId = bbsid;
        mCommentId = commentId;
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bbs_write, container, false);
        final EditText subjectEdit = (EditText)v.findViewById(R.id.bbs_write_edit_subject);
        final TextView textView = (TextView)v.findViewById(R.id.bbs_write_text_subject);
        final EditText contentEdit = (EditText)v.findViewById(R.id.bbs_write_edit_content);
        if(mMode == MODE_WRITE_ARTICLE){
            subjectEdit.requestFocus();
            
        }else if(mMode == MODE_MODIFY_ARTICLE){
            subjectEdit.setText(mSubject);
            contentEdit.setText(mContent);
            
        }else if(mMode == MODE_MODIFY_REPLY){
            subjectEdit.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            contentEdit.setText(mContent);
            contentEdit.requestFocus();
        }
        
        final Button button = (Button)v.findViewById(R.id.bbs_write_confirm_btn);
        button.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                String content = contentEdit.getText().toString();
                String subject = subjectEdit.getText().toString();
                if(content == null || content.equals("")){
                    Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                    button.startAnimation(shake);
                    Toast.makeText(getActivity(), getString(R.string.bbs_write_fill_content), Toast.LENGTH_SHORT).show();
                    return;
                }
                
                Bundle b = new Bundle();
                
				// Pre-filtering & common 
				if (mMode == MODE_WRITE_ARTICLE || mMode == MODE_MODIFY_ARTICLE) {
					if (subject == null || subject.equals("")) {
						Animation shake = AnimationUtils.loadAnimation(
								getActivity(), R.anim.shake);
						button.startAnimation(shake);
						Toast.makeText(getActivity(),
								getString(R.string.bbs_write_fill_subject),
								Toast.LENGTH_SHORT).show();
						return;
					}
                    b.putString("contents", content);
                    b.putString("subject", subject);
                    b.putInt("board_type", mBoardType);
                    ProgressBar progress = (ProgressBar) getActivity().findViewById(R.id.bbs_write_progress_bar);
                    if(progress != null){
                        progress.setVisibility(View.VISIBLE);
                    }
				}
				
				//Hide keyboard
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getActivity().getCurrentFocus()
						.getWindowToken(), 0);
				button.setEnabled(false);
                
                if(mMode == MODE_WRITE_ARTICLE){                    
                    ThreadRestAPI thread = new ThreadRestAPI(mHandler,Define.API_POST_BOARD_ARTICLE, b);
                    thread.start();
                }
                else if(mMode == MODE_MODIFY_ARTICLE){
                	b.putInt("bbs_id", mBbsId);
                	ThreadRestAPI thread = new ThreadRestAPI(mHandler,Define.API_PUT_BOARD_ARTICLE, b);
                    thread.start();
                }
                else if(mMode == MODE_MODIFY_REPLY){
                	ProgressBar progress = (ProgressBar) getActivity().findViewById(R.id.bbs_write_progress_bar);
                    if(progress != null){
                        progress.setVisibility(View.VISIBLE);
                    }
                	b.putInt("bbs_id", mBbsId);
                	b.putInt("board_type", mBoardType);
                	b.putString("comment_id", mCommentId);
                	b.putString("contents", content);
                	ThreadRestAPI thread = new ThreadRestAPI(mHandler,Define.API_PUT_BOARD_REPLY, b);
                    thread.start();
                }
            }
        });

        return v;
    }
    
    Handler mHandler = new Handler(){

        /* (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == Define.MSG_API_OK){
            	ProgressBar progress = (ProgressBar) getActivity().findViewById(R.id.bbs_write_progress_bar);
                if(progress != null){
                    progress.setVisibility(View.INVISIBLE);
                }
                MainFragmentActivity activity = (MainFragmentActivity)getActivity();
                JSONObject obj = (JSONObject)msg.obj;
                int result = 0;
                try {
                    result = obj.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(), "Error - BBS-01", Toast.LENGTH_LONG).show();
                    return;
                }
                if(msg.arg1 == Define.API_POST_BOARD_ARTICLE){
                    
                    if(Define.DEBUG){
                        Log.d("wisedog", "POST_BOARD_ARTICLE : " + obj.toString());
                    }
                    
                    if(result == Define.RESULT_OK){
                        activity.getFragmentManager().popBackStack();
                    }
                    
                }else if(msg.arg1== Define.API_PUT_BOARD_ARTICLE){
                    if(Define.DEBUG){
                        Log.d("wisedog", "PUT_BOARD_ARTICLE : " + obj.toString());
                    }
                    
                    if(result == Define.RESULT_OK){
                    	activity.mDirtyFlagModifyBbs = true;
                        activity.getFragmentManager().popBackStack();
                    }
                }else if(msg.arg1 == Define.API_PUT_BOARD_REPLY){
                	if(Define.DEBUG){
                		Log.d("wisedog", "API_PUT_BOARD_REPLY : " + obj.toString());
                	}
                	
                	if(result == Define.RESULT_OK){
                        getActivity().getFragmentManager().popBackStack();
                	}
                }
            }
            super.handleMessage(msg);
        }        
    };    
}
