package org.cryse.widget.persistentsearch.sample;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.cryse.widget.persistentsearch.PersistentSearchView;
import org.cryse.widget.persistentsearch.PersistentSearchView.HomeButtonListener;
import org.cryse.widget.persistentsearch.PersistentSearchView.SearchListener;

import java.util.ArrayList;

public class MenuItemSampleActivity extends AppCompatActivity {

	private PersistentSearchView mSearchView;
	private Toolbar mToolbar;
    private MenuItem mSearchMenuItem;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu_item_sample);
        mSearchView = (PersistentSearchView) findViewById(R.id.searchview);
        mSearchView.enableVoiceRecognition(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
		this.setSupportActionBar(mToolbar);
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
				// Use this to tint the screen

			}

			@Override
			public void onSearchEditClosed() {
				// Use this to un-tint the screen
				//closeSearch();
			}

			@Override
			public void onSearchExit() {

			}

			@Override
            public void onSearchTermChanged(String term) {

            }

			@Override
			public void onSearch(String string) {
				Toast.makeText(MenuItemSampleActivity.this, string + " Searched",
						Toast.LENGTH_LONG).show();
                mToolbar.setTitle(string);

			}

			@Override
			public void onSearchCleared() {
				
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

	protected void closeSearch() {
		//mSearchView.closeSearch();
        //mSearchView.hideCircularlyToMenuItem(R.id.action_search, this);
		//if(mSearchView.getSearchText().isEmpty())
			//mToolbar.setTitle(R.string.app_name);
	}

}
