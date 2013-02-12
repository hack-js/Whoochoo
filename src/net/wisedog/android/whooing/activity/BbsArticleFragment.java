/**
 * 
 */
package net.wisedog.android.whooing.activity;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.wisedog.android.whooing.Define;
import net.wisedog.android.whooing.R;
import net.wisedog.android.whooing.dataset.BoardItem;
import net.wisedog.android.whooing.network.ThreadRestAPI;
import net.wisedog.android.whooing.network.ThreadThumbnailLoader;
import net.wisedog.android.whooing.ui.BbsReplyEntity;
import net.wisedog.android.whooing.utils.DateUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author Wisedog(me@wisedog.net)
 *
 */
public class BbsArticleFragment extends SherlockFragment {

    private int mBoardType = -1;
    private BoardItem mItemData = null;

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Bundle b = new Bundle();
        b.putInt("bbs_id", mItemData.id);
        b.putInt("board_type", mBoardType);
        ThreadRestAPI thread = new ThreadRestAPI(mHandler,Define.API_GET_BOARD_ARTICLE, b);
        thread.start();
        super.onCreate(savedInstanceState);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bbs_article_fragment, container, false);
    }

    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onActivityCreated(android.os.Bundle)
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
    	ProgressBar progress = (ProgressBar)getActivity().findViewById(R.id.bbs_article_progress);
        if(progress != null){
        	progress.setVisibility(View.VISIBLE);
        }
        
        Button confirmButton = (Button)getActivity().findViewById(R.id.bbs_article_post_reply_btn);
        if(confirmButton != null){
        	confirmButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					EditText text = (EditText)getActivity().findViewById(R.id.bbs_article_post_reply_box);
			    	if(text != null){
			    		String str = text.getText().toString();
			    		if(str == null || str.equals("")){
			    			Toast.makeText(getActivity(), "입력좀해", Toast.LENGTH_SHORT).show();
			    			return;
			    		}
			    		Bundle b = new Bundle();
			    		b.putInt("bbs_id", mItemData.id);
			    		b.putString("contents", str);
			    		b.putString("category", mItemData.category);
			    		ThreadRestAPI thread = new ThreadRestAPI(mHandler,Define.API_POST_BOARD_REPLY, b);
			           thread.start();
			           setEnableStatus(false);
			           /*((Button)getActivity().findViewById(R.id.bbs_article_post_reply_btn)).setEnabled(false);
			           ((EditText)getActivity().findViewById(R.id.bbs_article_post_reply_box)).setEnabled(false);*/
			    	}
				}
			});
        }
        
        
        super.onActivityCreated(savedInstanceState);
    }
    
    protected Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == Define.MSG_API_OK){
                if(msg.arg1 == Define.API_GET_BOARD_ARTICLE){
                    JSONObject obj = (JSONObject)msg.obj;
                    try {
                        showArticle(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                else if(msg.arg1 == Define.API_POST_BOARD_REPLY){
                	JSONObject obj = (JSONObject)msg.obj;
                	setEnableStatus(true);
                	((EditText)getActivity().findViewById(R.id.bbs_article_post_reply_box)).setText("");
                	
                	int result = 0;
                	try {
						result = obj.getInt("code");
					} catch (JSONException e) {
						e.printStackTrace();
						//TODO Toast
					}
                	if(result == Define.RESULT_OK){
                		LinearLayout ll = (LinearLayout)getActivity().findViewById(R.id.bbs_article_reply_container);
                		BbsReplyEntity entity = new BbsReplyEntity(getActivity());
                    	try {
							entity.setupReply(obj);
							ll.addView(entity, 0);
						} catch (JSONException e) {
							e.printStackTrace();
						}
                	}
                	Log.i("wisedog", "BOARD_REPLY : " + obj.toString());
                }
            }
            
            else if(msg.what == 0){
                ImageView image = 
                        (ImageView)getActivity().findViewById(R.id.bbs_article_profile_image);
                if(msg.obj == null){
                    image.setImageResource(R.drawable.profile_anonymous);
                }
                else{
                    image.setImageBitmap((Bitmap)msg.obj);              
                }           
            }
            super.handleMessage(msg);
        }
        
    };
    
    
    public void setData(int boardType, BoardItem item){
        mBoardType = boardType;
        mItemData = item;
    }
    
    public void setEnableStatus(boolean enable){
    	((EditText)getActivity().findViewById(R.id.bbs_article_post_reply_box)).setEnabled(enable);
    	((Button)getActivity().findViewById(R.id.bbs_article_post_reply_btn)).setEnabled(enable);
    	
    	if(enable){
    		((ProgressBar)getActivity().findViewById(R.id.bbs_article_post_reply_progress)).setVisibility(View.INVISIBLE);
    	}else{
    		((ProgressBar)getActivity().findViewById(R.id.bbs_article_post_reply_progress)).setVisibility(View.VISIBLE);
    	}
    }
    
    /**
     * Show article data
     * @param		Bbs data formatted in JSON
     * */
    protected void showArticle(JSONObject obj) throws JSONException{
        JSONObject objResult = obj.getJSONObject("results");
        JSONObject objWriter = objResult.getJSONObject("writer");
        
        ProgressBar progress = (ProgressBar)getActivity().findViewById(R.id.bbs_article_progress);
        if(progress != null){
        	progress.setVisibility(View.GONE);
        }

        TextView textSubject = (TextView)getActivity().findViewById(R.id.bbs_article_subject);
        if(textSubject != null){
            textSubject.setText(objResult.getString("subject"));
        }
        
        TextView textName = (TextView)getActivity().findViewById(R.id.bbs_article_text_name);
        if(textName != null){
            textName.setText(objWriter.getString("username"));
        }
        TextView textLevel = (TextView)getActivity().findViewById(R.id.bbs_article_text_level);
        if(textLevel != null){
            textLevel.setText("lv. " + objWriter.getInt("level"));
        }
        
        TextView textDate = (TextView)getActivity().findViewById(R.id.bbs_article_text_date);
        if(textDate != null){
            String dateString = DateUtil.getDateWithTimestamp(objResult.getLong("timestamp") * 1000);
            textDate.setText(dateString);
        }
        
        TextView textContents = (TextView)getActivity().findViewById(R.id.bbs_article_text_contents);
        if(textContents != null){
            textContents.setText(objResult.getString("contents"));
        }
        
        ImageView profileImage = (ImageView)getActivity().findViewById(R.id.bbs_article_profile_image);
        profileImage.setScaleType(ImageView.ScaleType.FIT_XY);
        String profileUrl = objWriter.getString("image_url");
        if(profileImage != null){
            URL url = null;
            try {
                url = new URL(profileUrl);
            } catch (MalformedURLException e) {
                profileImage.setImageResource(R.drawable.profile_anonymous);                 
                e.printStackTrace();
                return;
            }
            
            ThreadThumbnailLoader thread = new ThreadThumbnailLoader(mHandler, url);
            try{
                thread.start();
            }
            catch(IllegalThreadStateException e){
                profileImage.setImageResource(R.drawable.profile_anonymous);
            }
        }
        
        //inflate reply
        JSONArray replyArray = objResult.getJSONArray("rows");
        int len = replyArray.length();
        LinearLayout ll = (LinearLayout)getActivity().findViewById(R.id.bbs_article_reply_container);
        for(int i = 0;i < len; i++){
        	BbsReplyEntity entity = new BbsReplyEntity(getActivity());
        	entity.setupReply((JSONObject) replyArray.get(i));
        	ll.addView(entity);
        }
        
    }
}
