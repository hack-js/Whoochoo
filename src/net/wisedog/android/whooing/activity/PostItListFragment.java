/**
 * 
 */
package net.wisedog.android.whooing.activity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.wisedog.android.whooing.Define;
import net.wisedog.android.whooing.R;
import net.wisedog.android.whooing.adapter.PostItAdapter;
import net.wisedog.android.whooing.dataset.PostItItem;
import net.wisedog.android.whooing.network.ThreadRestAPI;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * @author Wisedog(me@wisedog.net)
 *
 */
public class PostItListFragment extends SherlockListFragment{
    public static final String LIST_FRAGMENT_TAG = "post_it_list_tag";
    
    protected ArrayList<PostItItem> mDataArray;
    protected View footerView;
    protected PostItAdapter mAdapter;
    
    
    /* (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataArray = new ArrayList<PostItItem>();
        mAdapter = new PostItAdapter(getActivity(), mDataArray);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.footer, null, false);
        getListView().addFooterView(footerView, null, false);
        setListAdapter(mAdapter);
        ThreadRestAPI thread = new ThreadRestAPI(mHandler, Define.API_GET_POST_IT);
        thread.start();
    }

	@Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        PostItItem item = (PostItItem)getListView().getItemAtPosition(position);
        ((PostItFragmentActivity)getActivity()).addArticleFragment(item, false);
    }
    
    protected Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == Define.MSG_API_OK){
                if(msg.arg1 == Define.API_GET_POST_IT){
                    JSONObject obj = (JSONObject)msg.obj;
                    try {
                        showPostIt(obj);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            super.handleMessage(msg);
        }
    };
    
    protected void showPostIt(JSONObject obj) throws JSONException{
        JSONArray array = obj.getJSONArray("results");
        int length = array.length();
        mDataArray.clear();
        
        for(int i = 0; i < length; i++){
            JSONObject entity = array.getJSONObject(i);
            int id = entity.getInt("post_it_id");
            String page = entity.getString("page");
            String everywhere = entity.getString("everywhere");
            String content = entity.getString("contents");
            PostItItem item = new PostItItem(id, page, everywhere, content);
            mDataArray.add(item);
        }
        getListView().removeFooterView(footerView);
        mAdapter.notifyDataSetChanged();
        

    }

	@Override
	public void onHiddenChanged(boolean hidden) {
		PostItFragmentActivity activity = ((PostItFragmentActivity) getActivity());
		if(activity.getNeedRefresh()){
	        getListView().addFooterView(footerView);
            activity.needToRefresh(false);
            ThreadRestAPI thread = new ThreadRestAPI(mHandler, Define.API_GET_POST_IT);
            thread.start();
			
		}
		super.onHiddenChanged(hidden);
	}
}
