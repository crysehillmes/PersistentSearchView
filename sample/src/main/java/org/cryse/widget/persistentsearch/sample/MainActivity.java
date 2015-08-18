package org.cryse.widget.persistentsearch.sample;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.cryse.widget.persistentsearch.PersistentSearchView;
import org.cryse.widget.persistentsearch.PersistentSearchView.HomeButtonListener;
import org.cryse.widget.persistentsearch.PersistentSearchView.SearchListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
	private PersistentSearchView mSearchView;
    private Button mMenuItemSampleButton;
    private Button mSearchSampleButton;
    private Button mGithubRepoButton;
	private View mSearchTintView;
    private SearchResultAdapter mResultAdapter;
    private RecyclerView mRecyclerView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mSearchView = (PersistentSearchView) findViewById(R.id.searchview);
		mSearchTintView = findViewById(R.id.view_search_tint);
        mMenuItemSampleButton = (Button) findViewById(R.id.button_reveal_sample);
        mSearchSampleButton = (Button) findViewById(R.id.button_search_sample);
        mGithubRepoButton = (Button) findViewById(R.id.button_github_repo);
        mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview_search_result);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mResultAdapter = new SearchResultAdapter(new ArrayList<SearchResult>());
        mRecyclerView.setAdapter(mResultAdapter);
        mMenuItemSampleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MenuItemSampleActivity.class));
            }
        });
        mSearchSampleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
            }
        });
        mGithubRepoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(getString(R.string.url_github_repo)));
                startActivity(i);
            }
        });
		mSearchView.enableVoiceRecognition(this);
		mSearchView.setHomeButtonListener(new HomeButtonListener() {

            @Override
            public void onHomeButtonClick() {
                //Hamburger has been clicked
                Toast.makeText(MainActivity.this, "Menu click", Toast.LENGTH_LONG).show();
            }

        });
        mSearchTintView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchView.cancelEditing();
            }
        });
        mSearchView.setSuggestionBuilder(new SampleSuggestionsBuilder(this));
		mSearchView.setSearchListener(new SearchListener() {

			@Override
			public void onSearchEditOpened() {
				//Use this to tint the screen
				mSearchTintView.setVisibility(View.VISIBLE);
				mSearchTintView
                        .animate()
                        .alpha(1.0f)
                        .setDuration(300)
                        .setListener(new SimpleAnimationListener())
                        .start();
			}

			@Override
			public void onSearchEditClosed() {
				mSearchTintView
                        .animate()
                        .alpha(0.0f)
                        .setDuration(300)
                        .setListener(new SimpleAnimationListener() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mSearchTintView.setVisibility(View.GONE);
                            }
                        })
                        .start();
			}

            @Override
            public void onSearchExit() {
                mResultAdapter.clear();
                if(mRecyclerView.getVisibility() == View.VISIBLE) {
                    mRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
			public void onSearchTermChanged(String term) {

			}

			@Override
			public void onSearch(String string) {
				Toast.makeText(MainActivity.this, string +" Searched", Toast.LENGTH_LONG).show();
                mRecyclerView.setVisibility(View.VISIBLE);
                fillResultToRecyclerView(string);
			}

			@Override
			public void onSearchCleared() {
				//Called when the clear button is clicked
			}
			
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1234 && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			mSearchView.populateEditText(matches);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void fillResultToRecyclerView(String query) {
        List<SearchResult> newResults = new ArrayList<>();
        for(int i =0; i< 10; i++) {
            SearchResult result = new SearchResult(query, query + Integer.toString(i), "");
            newResults.add(result);
        }
        mResultAdapter.replaceWith(newResults);
    }

    @Override
    public void onBackPressed() {
        if(mSearchView.isSearching()) {
            mSearchView.closeSearch();
        } else if(mRecyclerView.getVisibility() == View.VISIBLE) {
            mResultAdapter.clear();
            mRecyclerView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }
}
