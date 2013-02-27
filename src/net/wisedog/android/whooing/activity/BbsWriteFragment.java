/**
 * 
 */
package net.wisedog.android.whooing.activity;

import org.json.JSONException;
import org.json.JSONObject;

import net.wisedog.android.whooing.Define;
import net.wisedog.android.whooing.R;
import net.wisedog.android.whooing.network.ThreadRestAPI;
import android.annotation.SuppressLint;
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

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author Wisedog(me@wisedog.net)
 *
 */
public class BbsWriteFragment extends SherlockFragment {
    public static final String BBS_WRITE_FRAGMENT_TAG = "bbs_write_tag";
    public static final int MODE_WRITE_ARTICLE = 0;
    public static final int MODE_MODIFY_ARTICLE = 1;
    public static final int MODE_MODIFY_REPLY = 2;
    
    private int mBoardType = -1;
    private String mSubject = null;
    private String mContent = null;
    private int mMode = 0;
    
    
    public void setData(int mode, int boardType, String subject, String content){
        mSubject = subject;
        mContent = content;
        mBoardType = boardType;
        mMode = mode;
    }
    
    //TODO hide actionbar button
    @Override
    public void onCreate(Bundle savedInstanceState) {
/*        Bundle b = new Bundle();
        b.putInt("bbs_id", mItemData.id);
        b.putInt("board_type", mBoardType);
        ThreadRestAPI thread = new ThreadRestAPI(mHandler,Define.API_GET_BOARD_ARTICLE, b);
        thread.start();*/
        super.onCreate(savedInstanceState);
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
                    Toast.makeText(getSherlockActivity(), getString(R.string.bbs_write_fill_content), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mMode == MODE_WRITE_ARTICLE){
                    if(subject == null || subject.equals("")){
                        Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                        button.startAnimation(shake);
                        Toast.makeText(getSherlockActivity(), getString(R.string.bbs_write_fill_subject), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                      imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    button.setEnabled(false);
                    Bundle b = new Bundle();
                    b.putString("contents", content);
                    b.putString("subject", subject);
                    b.putInt("board_type", mBoardType);
                    ProgressBar progress = (ProgressBar) getActivity().findViewById(R.id.bbs_write_progress_bar);
                    if(progress != null){
                        progress.setVisibility(View.VISIBLE);
                    }
                    ThreadRestAPI thread = new ThreadRestAPI(mHandler,Define.API_POST_BOARD_ARTICLE, b);
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
                if(msg.arg1 == Define.API_POST_BOARD_ARTICLE){
                    JSONObject obj = (JSONObject)msg.obj;
                    if(Define.DEBUG){
                        Log.i("wisedog", "POST_BOARD_ARTICLE : " + obj.toString());
                    }
                    int result = 0;
                    try {
                        result = obj.getInt("code");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                        //TODO Toast
                    }
                    if(result == Define.RESULT_OK){
                        ProgressBar progress = (ProgressBar) getActivity().findViewById(R.id.bbs_write_progress_bar);
                        if(progress != null){
                            progress.setVisibility(View.INVISIBLE);
                        }
                        ((BbsFragmentActivity)getActivity()).setListRefreshFlag(true);
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                    
                }
            }
            super.handleMessage(msg);
        }
        
    };
    
    

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @SuppressLint("NewApi")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        ((BbsFragmentActivity)getActivity()).mItemVisible = false;
        getSherlockActivity().invalidateOptionsMenu();
        super.onActivityCreated(savedInstanceState);
    }
    
    
}
