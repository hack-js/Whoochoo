/**
 * 
 */
package net.wisedog.android.whooing.activity;

import net.wisedog.android.whooing.Define;
import net.wisedog.android.whooing.R;
import net.wisedog.android.whooing.dataset.PostItItem;
import net.wisedog.android.whooing.network.ThreadRestAPI;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Post it article fragment
 * @author Wisedog(me@wisedog.net)
 *
 */
public class PostItArticleFragment extends SherlockFragment {
    
    private PostItItem mItem = null;
    private boolean mInEditMode = false;
    private boolean mInAddMode = false;
    
    public void setData(PostItItem item, boolean addMode){
        this.mItem = item;
        this.mInAddMode = addMode;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.post_it_article, container, false);
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        final EditText editText = (EditText)getActivity().findViewById(R.id.post_it_article_edit_text);
        final Button modifyBtn = (Button)getActivity().findViewById(R.id.post_it_article_btn_modify);
        Button removeBtn = (Button)getActivity().findViewById(R.id.post_it_article_btn_remove);
        final Button confirmBtn = (Button)getActivity().findViewById(R.id.post_it_article_btn_confirm);
        
        if(editText == null || modifyBtn == null || removeBtn == null || confirmBtn == null){
            super.onActivityCreated(savedInstanceState);
            return;
        }
        
        if(mInAddMode){
            editText.setEnabled(true);
            editText.requestFocus();
            removeBtn.setVisibility(View.GONE);
            modifyBtn.setVisibility(View.GONE);
            editText.addTextChangedListener(new TextWatcher(){

                @Override
                public void afterTextChanged(Editable arg0) {
                    String content = editText.getText().toString();
                    if(content.compareTo("") == 0){
                        confirmBtn.setEnabled(false);
                    }else{
                        confirmBtn.setEnabled(true);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    ; //Do nothing
                }

                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                	; //Do nothing
                }
            });

			confirmBtn.setEnabled(false);
			confirmBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Bundle b = new Bundle();
					b.putString("page", "_dashboard");
					b.putString("contents", editText.getText().toString());
					((ProgressBar)getActivity().findViewById(R.id.post_it_article_progress)).setVisibility(View.VISIBLE);
					confirmBtn.setEnabled(false);
					ThreadRestAPI thread = new ThreadRestAPI(mHandler,
							Define.API_POST_POSTIT, b);
					thread.start();
				}
			});
        }else{  //
            if(mItem != null){
                editText.setText(mItem.content);
                editText.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						String content = editText.getText().toString();
                        if(content.compareTo(mItem.content) == 0){
                            modifyBtn.setEnabled(false);
                        }else{
                            modifyBtn.setEnabled(true);
                        }
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						;//Do nothing
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						;//Do nothing
					}
				});
                
            }
            modifyBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mInEditMode) {
                        editText.setEnabled(false);
                        modifyBtn.setText(getString(R.string.text_modify));
                        Toast.makeText(getActivity(), "Modified", Toast.LENGTH_SHORT).show();
                    } else {
                        editText.setEnabled(true);
                        editText.requestFocus();
                        modifyBtn.setText(getString(R.string.text_confirm));
                        modifyBtn.setEnabled(false);
                        mInEditMode = true;
                    }
                }
            });
            removeBtn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    // TODO Auto-generated method stub

                }
            });
            confirmBtn.setVisibility(View.GONE);
        }
        
        
        super.onActivityCreated(savedInstanceState);
    }
    
    protected Handler mHandler = new Handler(){
    	@Override
        public void handleMessage(Message msg) {
            if(msg.what == Define.MSG_API_OK){
                if(msg.arg1 == Define.API_POST_POSTIT){
                    ((ProgressBar)getActivity().findViewById(R.id.post_it_article_progress))
                    .setVisibility(View.INVISIBLE);
                    ((PostItFragmentActivity)getActivity()).needToRefresh(true);
                    getActivity().getSupportFragmentManager().popBackStack();
                    
                }
                else if(msg.arg1 == Define.API_PUT_POSTIT){
                	
                }
            }
            super.handleMessage(msg);
        }
    };
    
    
    public boolean allowBackPressed(){
        final EditText editText = (EditText)getActivity().findViewById(R.id.post_it_article_edit_text);
        int dirtyFlag = editText.getText().toString().compareTo(mItem.content);
        return (mInEditMode && dirtyFlag == 0);
    }
    
    
}