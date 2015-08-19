package org.cryse.widget.persistentsearch.sample;

import android.animation.Animator;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.cryse.widget.persistentsearch.DefaultVoiceRecognizerDelegate;
import org.cryse.widget.persistentsearch.PersistentSearchView;
import org.cryse.widget.persistentsearch.PersistentSearchView.HomeButtonListener;
import org.cryse.widget.persistentsearch.PersistentSearchView.SearchListener;
import org.cryse.widget.persistentsearch.VoiceRecognitionDelegate;

import java.util.ArrayList;
import java.util.List;

public class MenuItemSampleActivity extends AppCompatActivity {
	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1023;
	private PersistentSearchView mSearchView;
	private Toolbar mToolbar;
    private MenuItem mSearchMenuItem;
	private View mSearchTintView;
	private SearchResultAdapter mResultAdapter;
	private RecyclerView mRecyclerView;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_item_sample);
        mSearchView = (PersistentSearchView) findViewById(R.id.searchview);
		VoiceRecognitionDelegate delegate = new DefaultVoiceRecognizerDelegate(this, VOICE_RECOGNITION_REQUEST_CODE);
		if(delegate.isVoiceRecognitionAvailable()) {
			mSearchView.setVoiceRecognitionDelegate(delegate);
		}
		mToolbar = (Toolbar) findViewById(R.id.toolbar);mSearchTintView = findViewById(R.id.view_search_tint);
		this.setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setUpSearchView();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_searchview, menu);
        mSearchMenuItem = menu.findItem(R.id.action_search);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search:
                if(mSearchMenuItem != null) {
                    openSearch();
                    return true;
                } else {
                    return false;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    public void openSearch() {
        View menuItemView = findViewById(R.id.action_search);
        mSearchView.openSearch(menuItemView);
    }

    public void setUpSearchView() {
		mRecyclerView = (RecyclerView)findViewById(R.id.recyclerview_search_result);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mResultAdapter = new SearchResultAdapter(new ArrayList<SearchResult>());
		mRecyclerView.setAdapter(mResultAdapter);
		mSearchTintView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSearchView.cancelEditing();
			}
		});
        mSearchView.setHomeButtonListener(new HomeButtonListener() {

			@Override
			public void onHomeButtonClick() {
				// Hamburger has been clicked
				Toast.makeText(MenuItemSampleActivity.this, "Menu click",
						Toast.LENGTH_LONG).show();
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
				Toast.makeText(MenuItemSampleActivity.this, string +" Searched", Toast.LENGTH_LONG).show();
				mRecyclerView.setVisibility(View.VISIBLE);
				fillResultToRecyclerView(string);

			}

			@Override
			public void onSearchCleared() {

			}

		});

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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            mSearchView.populateEditText(matches);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
