/*
 * Copyright (c) 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.backend.qwikserve;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.backend.R;
import com.google.cloud.backend.core.CloudBackendFragment;
import com.google.cloud.backend.core.CloudBackendFragment.OnListener;
import com.google.cloud.backend.core.CloudCallbackHandler;
import com.google.cloud.backend.core.CloudEntity;
import com.google.cloud.backend.core.CloudQuery.Order;
import com.google.cloud.backend.core.CloudQuery.Scope;
import com.google.cloud.backend.core.Consts;

/**
 * Sample Guestbook app with Mobile Backend Starter.
 */
public class CookActivity extends Activity implements OnListener {

    private static final String BROADCAST_PROP_DURATION = "duration";
    private static final String BROADCAST_PROP_MESSAGE = "message";

    private static final String PROCESSING_FRAGMENT_TAG = "BACKEND_FRAGMENT";
    
    
    /* UI components   */
    private GridView mPostsView;
    private TextView mEmptyView;
    private ImageView mSendBtn;
    private ImageView mRfrshBtn;
    private TextView mAnnounceTxt;
    
    private int testint;
    private CloudBackendFragment mProcessingFragment;
    private FragmentManager mFragmentManager;
    

    /**
     * A list of posts to be displayed
     */
    private List<CloudEntity> mPosts = new LinkedList<CloudEntity>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the view
        mPostsView = (GridView) findViewById(R.id.GridView);
        mEmptyView = (TextView) findViewById(R.id.no_messages);
        testint=0;
      
        mSendBtn = (ImageView) findViewById(R.id.send_btn);
        mSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendButtonPressed(v);
            }
        });
        mRfrshBtn = (ImageView) findViewById(R.id.Refresh);
        mRfrshBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onRefreshPressed(v);
				
			}
		});
        mSendBtn.setEnabled(true);
        mRfrshBtn.setEnabled(true);
        mAnnounceTxt = (TextView) findViewById(R.id.announce_text);
        mFragmentManager = getFragmentManager();
        initiateFragments();
        
    }

    /**
     * Override Activity lifecycle method.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Override Activity lifecycle method.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem loginItem = menu.findItem(R.id.switch_account);
        loginItem.setVisible(Consts.IS_AUTH_ENABLED);
        return true;
    }

    /**
     * Override Activity lifecycle method.
     * <p>
     * To add more option menu items in your client, add the item to menu/activity_main.xml,
     * and provide additional case statements in this method.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_account:
                mProcessingFragment.signInAndSubscribe(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateFinished() {
        listPosts();
    }

    /**
     * Method called via OnListener in {@link CloudBackendFragment}.
     */
    @Override
    public void onBroadcastMessageReceived(List<CloudEntity> l) {
        for (CloudEntity e : l) {
            String message = (String) e.get(BROADCAST_PROP_MESSAGE);
            int duration = Integer.parseInt((String) e.get(BROADCAST_PROP_DURATION));
            Toast.makeText(this, message, duration).show();
            Log.i(Consts.TAG, "A message was recieved with content: " + message);
        }
    }


    /**
     * onClick method.
     */
    public void onSendButtonPressed(View view) {

        // create a CloudEntity with the new post
        CloudEntity newPost = new CloudEntity("Guestbook");
        newPost.put("message", Integer.toString(testint) );
        testint ++;
       
        // create a response handler that will receive the result or an error
        CloudCallbackHandler<CloudEntity> handler = new CloudCallbackHandler<CloudEntity>() {
            @Override
            public void onComplete(final CloudEntity result) {
                mPosts.add(0, result);
                updateGuestbookView();
                mSendBtn.setEnabled(true);
            }

            @Override
            public void onError(final IOException exception) {
                handleEndpointException(exception);
            }
        };
        // execute the insertion with the handler
        mProcessingFragment.getCloudBackend().insert(newPost, handler);
        mSendBtn.setEnabled(true);
    }
    public void onRefreshPressed(View view){
    	listPosts();
    }
    
    
    /**
     * Retrieves the list of all posts from the backend and updates the UI. For
     * demonstration in this sample, the query that is executed is:
     * "SELECT * FROM Guestbook ORDER BY _createdAt DESC LIMIT 50" This query
     * will be re-executed when matching entity is updated.
     */
    private void listPosts() {
        // create a response handler that will receive the result or an error
        CloudCallbackHandler<List<CloudEntity>> handler =
                new CloudCallbackHandler<List<CloudEntity>>() {
                    @Override
                    public void onComplete(List<CloudEntity> results) {
                        mAnnounceTxt.setText(R.string.announce_success);
                        mPosts = results;
                        animateArrival();
                        updateGuestbookView();
                    }

                    @Override
                    public void onError(IOException exception) {
                        mAnnounceTxt.setText(R.string.announce_fail);
                        animateArrival();
                        handleEndpointException(exception);
                    }
                };

        // execute the query with the handler
        mProcessingFragment.getCloudBackend().listByKind(
                "Guestbook", CloudEntity.PROP_CREATED_AT, Order.DESC, 50,
                Scope.FUTURE_AND_PAST, handler);
    }
    
    private void initiateFragments() {
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        // Check to see if we have retained the fragment which handles
        // asynchronous backend calls
        mProcessingFragment = (CloudBackendFragment) mFragmentManager.
                findFragmentByTag(PROCESSING_FRAGMENT_TAG);
        // If not retained (or first time running), create a new one
        if (mProcessingFragment == null) {
            mProcessingFragment = new CloudBackendFragment();
            mProcessingFragment.setRetainInstance(true);
            fragmentTransaction.add(mProcessingFragment, PROCESSING_FRAGMENT_TAG);
        }

        // Add the splash screen fragment
            
            fragmentTransaction.commit();
    }
    
    private boolean firstArrival = true;
    
    private void animateArrival() {
      
        if (firstArrival) {
            mAnnounceTxt.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.translate_progress);
            anim.setAnimationListener(new Animation.AnimationListener() {
    
                @Override
                public void onAnimationStart(Animation animation) {
                }
    
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
    
                @Override
                public void onAnimationEnd(Animation animation) {
                    mAnnounceTxt.setVisibility(View.GONE);
                }
            });
            mAnnounceTxt.startAnimation(anim);
            firstArrival = false;
        }
    }

    private void updateGuestbookView() {
            mSendBtn.setEnabled(true);
            if (!mPosts.isEmpty()) {
                mEmptyView.setVisibility(View.GONE);
                mPostsView.setVisibility(View.VISIBLE);
                mPostsView.setAdapter(new PostAdapter(
                        this, android.R.layout.simple_list_item_1, mPosts));
            } else {
                mEmptyView.setVisibility(View.VISIBLE);
                mPostsView.setVisibility(View.GONE);
            }
    }

    private void handleEndpointException(IOException e) {
        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        mSendBtn.setEnabled(true);
    }

}
