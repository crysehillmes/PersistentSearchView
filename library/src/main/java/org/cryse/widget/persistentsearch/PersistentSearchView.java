package org.cryse.widget.persistentsearch;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

@SuppressWarnings("unused")
public class PersistentSearchView extends RevealViewGroup {
    public static final int VOICE_RECOGNITION_CODE = 8185102;

    public enum DisplayMode {
        MENUITEM(0), TOOLBAR(1);
        int mode;

        DisplayMode(int mode) {
            this.mode = mode;
        }

        public static DisplayMode fromInt(int mode) {
            for (DisplayMode enumMode : values()) {
                if (enumMode.mode == mode) return enumMode;
            }
            throw new IllegalArgumentException();
        }

        public int toInt() {
            return mode;
        }
    }

    public enum SearchViewState {
        TOOLBAR, MENUITEM, EDITING, SEARCH;
    }

    private static final int DURATION_REVEAL_OPEN = 500;
    private static final int DURATION_REVEAL_CLOSE = 500;
    private static final int DURATION_HOME_BUTTON = 300;
    private static final int DURATION_LAYOUT_TRANSITION = 100;

    private static final float mOneMinusCos45 = 0.2928932188134524f;

    private HomeButton.IconState mHomeButtonCloseIconState;
    private HomeButton.IconState mHomeButtonOpenIconState;
    private HomeButton.IconState mHomeButtonSearchIconState;

    private SearchViewState mCurrentState;
    private SearchViewState mLastState;
    private DisplayMode mDisplayMode;

    private int mCardVisiblePadding;
    private int mSearchCardElevation;
    private int mFromX, mFromY;
    // Views
    private LogoView mLogoView;
    private CardView mSearchCardView;
    private HomeButton mHomeButton;
    private EditText mSearchEditText;
    private ListView mSuggestionListView;
    private ImageView mMicButton;
    private SearchListener mSearchListener;
    private HomeButtonListener mHomeButtonListener;
    private FrameLayout mRootLayout;
    private VoiceRecognitionListener mVoiceRecognitionListener;
    private Activity mContainerActivity;
    private Fragment mContainerFragment;
    private android.support.v4.app.Fragment mContainerSupportFragment;


    private boolean mAvoidTriggerTextWatcher;
    private boolean mIsVoiceRecognitionIntentSupported;
    private boolean mIsMic;
    private int mSearchTextColor;
    private int mArrorButtonColor;
    private Drawable mLogoDrawable;
    private int mSearchEditTextColor;
    private String mSearchEditTextHint;
    private int mSearchEditTextHintColor;
    private SearchSuggestionsBuilder mSuggestionBuilder;
    private SearchItemAdapter mSearchItemAdapter;
    private ArrayList<SearchItem> mSearchSuggestions;

    public PersistentSearchView(Context context) {
        super(context);
        init(null);
    }

    public PersistentSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public PersistentSearchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private static boolean isIntentAvailable(Context context, Intent intent) {
        PackageManager mgr = context.getPackageManager();
        if (mgr != null) {
            List<ResolveInfo> list = mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        }
        return false;
    }

    private void init(AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_searchview, this, true);
        if(attrs != null) {
            TypedArray attributesValue = getContext().obtainStyledAttributes(attrs,
                    R.styleable.PersistentSearchView);
            mDisplayMode = DisplayMode.fromInt(attributesValue.getInt(R.styleable.PersistentSearchView_persistentSV_displayMode, DisplayMode.MENUITEM.toInt()));
            mSearchCardElevation = attributesValue.getDimensionPixelSize(R.styleable.PersistentSearchView_persistentSV_searchCardElevation, -1);
            mSearchTextColor = attributesValue.getColor(R.styleable.PersistentSearchView_persistentSV_searchTextColor, Color.BLACK);
            mLogoDrawable = attributesValue.getDrawable(R.styleable.PersistentSearchView_persistentSV_logoDrawable);
            mSearchEditTextColor = attributesValue.getColor(R.styleable.PersistentSearchView_persistentSV_editTextColor, Color.BLACK);
            mSearchEditTextHint = attributesValue.getString(R.styleable.PersistentSearchView_persistentSV_editHintText);
            mSearchEditTextHintColor = attributesValue.getColor(R.styleable.PersistentSearchView_persistentSV_editHintTextColor, Color.BLACK);
            mArrorButtonColor = attributesValue.getColor(R.styleable.PersistentSearchView_persistentSV_homeButtonColor, Color.BLACK);
            attributesValue.recycle();
        }

        if(mSearchCardElevation < 0) {
            mSearchCardElevation = getContext().getResources().getDimensionPixelSize(R.dimen.search_card_default_card_elevation);
        }

        switch (mDisplayMode) {
            case MENUITEM:
            default:
                mCardVisiblePadding = getResources().getDimensionPixelSize(R.dimen.search_card_visible_padding_menu_item_mode);
                mHomeButtonCloseIconState = HomeButton.IconState.ARROW;
                mHomeButtonOpenIconState = HomeButton.IconState.ARROW;
                setCurrentState(SearchViewState.MENUITEM);
                break;
            case TOOLBAR:
                mHomeButtonCloseIconState = HomeButton.IconState.BURGER;
                mHomeButtonOpenIconState = HomeButton.IconState.ARROW;
                mCardVisiblePadding = getResources().getDimensionPixelSize(R.dimen.search_card_visible_padding_toolbar_mode);
                setCurrentState(SearchViewState.TOOLBAR);
                break;
        }
        mHomeButtonSearchIconState = HomeButton.IconState.ARROW;

        bindViews();
        setValuesToViews();

        this.mIsMic = true;
        mSearchSuggestions = new ArrayList<>();
        mSearchItemAdapter = new SearchItemAdapter(getContext(), mSearchSuggestions);
        mSuggestionListView.setAdapter(mSearchItemAdapter);
        mIsVoiceRecognitionIntentSupported = isIntentAvailable(getContext(), new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH));

        setUpLayoutTransition();
        setUpListeners();
    }

    private void bindViews() {
        this.mSearchCardView = (CardView)findViewById(R.id.cardview_search);
        this.mHomeButton = (HomeButton) findViewById(R.id.button_home);
        this.mLogoView = (LogoView) findViewById(R.id.logoview);
        this.mSearchEditText = (EditText) findViewById(R.id.edittext_search);
        this.mSuggestionListView = (ListView) findViewById(R.id.listview_suggestions);
        this.mMicButton = (ImageView) findViewById(R.id.button_mic);
    }

    private void setValuesToViews () {
        this.mSearchCardView.setCardElevation(mSearchCardElevation);
        this.mSearchCardView.setMaxCardElevation(mSearchCardElevation);
        this.mHomeButton.setArrowDrawableColor(mArrorButtonColor);
        this.mHomeButton.setState(mHomeButtonCloseIconState);
        this.mHomeButton.setAnimationDuration(DURATION_HOME_BUTTON);
        this.mSearchEditText.setTextColor(mSearchEditTextColor);
        this.mSearchEditText.setHint(mSearchEditTextHint);
        this.mSearchEditText.setHintTextColor(mSearchEditTextHintColor);
        if (mLogoDrawable != null) {
            this.mLogoView.setLogo(mLogoDrawable);
        }
        this.mLogoView.setTextColor(mSearchTextColor);
    }

    private void setUpListeners() {
        mHomeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCurrentState == SearchViewState.EDITING) {
                    cancelEditing();
                } else if(mCurrentState == SearchViewState.SEARCH) {
                    stateFromSearchToNormal();
                } else {
                    if (mHomeButtonListener != null)
                        mHomeButtonListener.onHomeButtonClick();
                }
            }

        });
        mLogoView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if(mCurrentState == SearchViewState.TOOLBAR) {
                    stateFromToolbarToEditing();
                } else if(mCurrentState == SearchViewState.SEARCH) {
                    stateFromSearchToEditing();
                }
            }

        });
        mSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    clearSuggestions();
                    search();
                    return true;
                }
                return false;
            }
        });
        mSearchEditText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    clearSuggestions();
                    search();
                    return true;
                }
                return false;
            }
        });
        micStateChanged();
        mMicButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mVoiceRecognitionListener != null) {
                    mVoiceRecognitionListener.onClick();
                } else {
                    micClick();
                }
            }
        });
        mSearchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (!mAvoidTriggerTextWatcher) {
                    if (s.length() > 0) {
                        micStateChanged(false);
                        mMicButton.setImageDrawable(
                                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_clear_black, null)
                        );
                        buildSearchSuggestions(getSearchText());
                    } else {
                        micStateChanged(true);
                        mMicButton.setImageDrawable(
                                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_mic_black, null)
                        );
                        buildEmptySearchSuggestions();
                    }
                }
                if (mSearchListener != null)
                    mSearchListener.onSearchTermChanged(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

        });

    }

    private void setUpLayoutTransition() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            RelativeLayout searchRoot = (RelativeLayout) findViewById(R.id.search_root);
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.setDuration(DURATION_LAYOUT_TRANSITION);
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                // layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
                layoutTransition.enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
                layoutTransition.setStartDelay(LayoutTransition.CHANGING, 0);
            }
            layoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
            mSearchCardView.setLayoutTransition(layoutTransition);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int totalHeight = 0;
        int searchCardWidth;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                if(i == 0 && child instanceof CardView) {
                    CardView searchCard = (CardView)child;
                    int horizontalPadding = (int)Math.ceil(searchCard.getCardElevation() + mOneMinusCos45 * searchCard.getRadius());
                    int verticalPadding = (int)Math.ceil(searchCard.getCardElevation() * 1.5f + mOneMinusCos45 * searchCard.getRadius());
                    // searchCardWidth = widthSize - 2 * mCardVisiblePadding + horizontalPadding * 2;
                    int searchCardLeft = mCardVisiblePadding - horizontalPadding;
                    // searchCardTop = mCardVisiblePadding - verticalPadding;
                    searchCardWidth = widthSize - searchCardLeft * 2;
                    int cardWidthSpec = MeasureSpec.makeMeasureSpec(searchCardWidth, MeasureSpec.EXACTLY);
                    // int cardHeightSpec = MeasureSpec.makeMeasureSpec(searchCardHeight, MeasureSpec.EXACTLY);
                    measureChild(child, cardWidthSpec, heightMeasureSpec);
                    int childMeasuredHeight = child.getMeasuredHeight();
                    int childMeasuredWidth = child.getMeasuredWidth();
                    int childHeight = childMeasuredHeight - verticalPadding * 2;
                    totalHeight = totalHeight + childHeight + mCardVisiblePadding * 2;
                }
            }
        }
        setMeasuredDimension(widthSize, totalHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        int searchViewWidth = r - l;
        int searchViewHeight = b - t;
        int searchCardLeft;
        int searchCardTop;
        int searchCardRight;
        int searchCardBottom;
        int searchCardWidth;
        int searchCardHeight;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
                if(i == 0 && child instanceof CardView) {
                    CardView searchCard = (CardView)child;
                    int horizontalPadding = (int)Math.ceil(searchCard.getCardElevation() + mOneMinusCos45 * searchCard.getRadius());
                    int verticalPadding = (int)Math.ceil(searchCard.getCardElevation() * 1.5f + mOneMinusCos45 * searchCard.getRadius());
                    searchCardLeft = mCardVisiblePadding - horizontalPadding;
                    searchCardTop = mCardVisiblePadding - verticalPadding;
                    searchCardWidth = searchViewWidth - searchCardLeft * 2;
                    searchCardHeight = child.getMeasuredHeight();
                    searchCardRight = searchCardLeft + searchCardWidth;
                    searchCardBottom = searchCardTop + searchCardHeight;
                    child.layout(searchCardLeft, searchCardTop, searchCardRight, searchCardBottom);
                }
        }
    }

    private void revealFromMenuItem(View menuItemView, int desireRevealWidth) {
        setVisibility(View.VISIBLE);
        if (menuItemView != null) {
            int[] location = new int[2];
            menuItemView.getLocationInWindow(location);
            int menuItemWidth = menuItemView.getWidth();
            this.mFromX = location[0] + menuItemWidth / 2;
            this.mFromY = location[1];
            revealFrom(mFromX, mFromY, desireRevealWidth);
        }
    }

    private void hideCircularlyToMenuItem() {
        if(mFromX == 0 || mFromY == 0) {
            mFromX = getRight();
            mFromY = getTop();
        }
        hideCircularly(mFromX, mFromY);
    }

    /***
     * Hide the PersistentSearchView using the circle animation. Can be called regardless of result list length
     */
    private void hideCircularly(int x, int y){

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96,
                r.getDisplayMetrics());
        int finalRadius = (int) Math.max(this.getMeasuredWidth() * 1.5, px);

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(
                mSearchCardView, x, y, 0, finalRadius);
        animator = animator.reverse();
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(DURATION_REVEAL_CLOSE);
        animator.start();
        animator.addListener(new SupportAnimator.AnimatorListener() {

            @Override
            public void onAnimationStart() {

            }

            @Override
            public void onAnimationEnd() {
                setVisibility(View.GONE);
                mSuggestionListView.setVisibility(View.GONE);
                // closeSearch();
            }

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationRepeat() {

            }

        });
    }

    /***
     * Hide the PersistentSearchView using the circle animation. Can be called regardless of result list length
     */
    private void hideCircularly(){
        hideCircularly(getLeft() + getRight(), getTop());
    }


    public boolean getSearchOpen() {
        return getVisibility() == VISIBLE && (mCurrentState == SearchViewState.SEARCH || mCurrentState == SearchViewState.EDITING);
    }

    /***
     * Hide the search suggestions manually
     */
    public void hideSuggestions(){
        this.mSearchEditText.setVisibility(View.GONE);
        this.mSuggestionListView.setVisibility(View.GONE);
    }

    /***
     * Start the voice input activity manually
     */
    public void startVoiceRecognition() {
        if (isMicEnabled()) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    getContext().getString(R.string.speak_now));
            if (mContainerActivity != null) {
                mContainerActivity.startActivityForResult(intent, VOICE_RECOGNITION_CODE);
            } else if (mContainerFragment != null) {
                mContainerFragment.startActivityForResult(intent, VOICE_RECOGNITION_CODE);
            } else if (mContainerSupportFragment != null) {
                mContainerSupportFragment.startActivityForResult(intent, VOICE_RECOGNITION_CODE);
            }
        }
    }

    /***
     * Enable voice recognition for Activity
     * @param context Context
     */
    public void enableVoiceRecognition(Activity context) {
        mContainerActivity = context;
        micStateChanged();
    }

    /***
     * Enable voice recognition for Fragment
     * @param context Fragment
     */
    public void enableVoiceRecognition(Fragment context) {
        mContainerFragment = context;
        micStateChanged();
    }

    /***
     * Enable voice recognition for Support Fragment
     * @param context Fragment
     */
    public void enableVoiceRecognition(android.support.v4.app.Fragment context) {
        mContainerSupportFragment = context;
        micStateChanged();
    }

    private boolean isMicEnabled() {
        return mIsVoiceRecognitionIntentSupported && (mContainerActivity != null || mContainerSupportFragment != null || mContainerFragment != null);
    }

    private void micStateChanged() {
        mMicButton.setVisibility((!mIsMic || isMicEnabled()) ? VISIBLE : INVISIBLE);
    }

    private void micStateChanged(boolean isMic) {
        this.mIsMic = isMic;
        micStateChanged();
    }

    /***
     * Mandatory method for the onClick event
     */
    public void micClick() {
        if (!mIsMic) {
            setSearchString("", false);
        } else {
            startVoiceRecognition();
        }

    }

    /***
     * Populate the PersistentSearchView with words, in an ArrayList. Used by the voice input
     * @param matches Matches
     */
    public void populateEditText(ArrayList<String> matches) {
        String text = matches.get(0).trim();
        setSearchString(text, true);
        search();
    }

    /***
     * Set whether the menu button should be shown. Particularly useful for apps that adapt to screen sizes
     * @param visibility Whether to show
     */

    public void setHomeButtonVisibility(int visibility){
        this.mHomeButton.setVisibility(visibility);
    }

    /***
     * Set the menu listener
     * @param homeButtonListener MenuListener
     */
    public void setHomeButtonListener(HomeButtonListener homeButtonListener) {
        this.mHomeButtonListener = homeButtonListener;
    }

    /***
     * Set the search listener
     * @param listener SearchListener
     */
    public void setSearchListener(SearchListener listener) {
        this.mSearchListener = listener;
    }

    /***
     * Set the text color of the logo
     * @param color logo text color
     */
    public void setLogoTextColor(int color){
        mLogoView.setTextColor(color);
    }

    /***
     * Get the PersistentSearchView's current text
     * @return Text
     */
    public String getSearchText() {
        return mSearchEditText.getText().toString();
    }

    public void clearSuggestions() {
        mSearchItemAdapter.clear();
    }

    /***
     * Set the PersistentSearchView's current text manually
     * @param text Text
     * @param avoidTriggerTextWatcher avoid trigger TextWatcher(TextChangedListener)
     */
    public void setSearchString(String text, boolean avoidTriggerTextWatcher) {
        if(avoidTriggerTextWatcher)
            mAvoidTriggerTextWatcher = true;
        mSearchEditText.setText("");
        mSearchEditText.append(text);
        mAvoidTriggerTextWatcher = false;
    }

    private void buildEmptySearchSuggestions() {
        if(mSuggestionBuilder != null) {
            mSearchSuggestions.clear();
            Collection<SearchItem> suggestions = mSuggestionBuilder.buildEmptySearchSuggestion(10);
            if(suggestions != null && suggestions.size() > 0) {
                mSearchSuggestions.addAll(suggestions);
            }
            mSearchItemAdapter.notifyDataSetChanged();
        }
    }

    private void buildSearchSuggestions(String query) {
        if(mSuggestionBuilder != null) {
            mSearchSuggestions.clear();
            Collection<SearchItem> suggestions = mSuggestionBuilder.buildSearchSuggestion(10, query);
            if(suggestions != null && suggestions.size() > 0) {
                mSearchSuggestions.addAll(suggestions);
            }
            mSearchItemAdapter.notifyDataSetChanged();
        }
    }

    private void revealFrom(float x, float y, int desireRevealWidth) {
        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96,
                r.getDisplayMetrics());

        int measuredHeight = getMeasuredWidth();
        int finalRadius = (int) Math.max(Math.max(measuredHeight, px), desireRevealWidth);

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(
                mSearchCardView, (int)x, (int)y, 0, finalRadius);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(DURATION_REVEAL_OPEN);
        animator.addListener(new SupportAnimator.AnimatorListener() {

            @Override
            public void onAnimationCancel() {

            }

            @Override
            public void onAnimationEnd() {
                // show search view here
                openSearchInternal(true);
            }

            @Override
            public void onAnimationRepeat() {

            }

            @Override
            public void onAnimationStart() {

            }

        });
        animator.start();
    }

    private void search() {
        String searchTerm = getSearchText();
        if (!TextUtils.isEmpty(searchTerm)) {
            setLogoTextInt(searchTerm);
            stateFromEditingToSearch();
            if (mSearchListener != null)
                mSearchListener.onSearch(searchTerm);
        } else {
            stateFromEditingToNormal();
        }
    }

    private void openSearchInternal(Boolean openKeyboard) {
        this.mHomeButton.animateState(mHomeButtonOpenIconState);
        this.mLogoView.setVisibility(View.GONE);
        this.mSearchEditText.setVisibility(View.VISIBLE);
        mSearchEditText.requestFocus();
        this.mSuggestionListView.setVisibility(View.VISIBLE);
        mSuggestionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                SearchItem result = mSearchSuggestions.get(arg2);
                setSearchString(result.getValue(), true);
                search();
            }

        });
        String currentSearchText = getSearchText();
        if(currentSearchText.length() > 0) {
            buildSearchSuggestions(currentSearchText);
        } else {
            buildEmptySearchSuggestions();
        }

        if (mSearchListener != null)
            mSearchListener.onSearchEditOpened();
        if (getSearchText().length() > 0) {
            micStateChanged(false);
            mMicButton.setImageDrawable(
                    ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_clear_black, null));
        }
        if (openKeyboard) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInputFromWindow(
                    getApplicationWindowToken(),
                    InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void closeSearchInternal() {
        if(mCurrentState == SearchViewState.SEARCH) {
            this.mHomeButton.animateState(mHomeButtonSearchIconState);
        } else {
            this.mHomeButton.animateState(mHomeButtonCloseIconState);
        }
        this.mLogoView.setVisibility(View.VISIBLE);
        this.mSearchEditText.setVisibility(View.GONE);
        // if(mDisplayMode == DISPLAY_MODE_AS_TOOLBAR) {
            mSuggestionListView.setVisibility(View.GONE);
        // }
        // this.mSuggestionListView.setVisibility(View.GONE);
        /*if (mTintView != null && mRootLayout != null) {
            mRootLayout.removeView(mTintView);
        }*/
        if (mSearchListener != null)
            mSearchListener.onSearchEditClosed();
        micStateChanged(true);
        mMicButton.setImageDrawable(
                ResourcesCompat.getDrawable(getResources(), R.drawable.ic_action_mic_black, null));
        InputMethodManager inputMethodManager = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getApplicationWindowToken(),
                0);
    }

    public boolean isSearching() {
        return mCurrentState == SearchViewState.EDITING || mCurrentState == SearchViewState.SEARCH;
    }

    private void setLogoTextInt(String text) {
        mLogoView.setText(text);
    }

    public void setHomeButtonOpenIconState(HomeButton.IconState homeButtonOpenIconState) {
        this.mHomeButtonOpenIconState = homeButtonOpenIconState;
    }

    public void setHomeButtonCloseIconState(HomeButton.IconState homeButtonCloseIconState) {
        this.mHomeButtonCloseIconState = homeButtonCloseIconState;
    }

    public void setSuggestionBuilder(SearchSuggestionsBuilder suggestionBuilder) {
        this.mSuggestionBuilder = suggestionBuilder;
    }

    private void stateFromToolbarToEditing() {
        openSearchInternal(true);
        setCurrentState(SearchViewState.EDITING);
    }

    private void stateFromMenuItemToEditing() {
        setCurrentState(SearchViewState.EDITING);
    }

    private void stateFromSearchToEditing() {
        openSearchInternal(true);
        setCurrentState(SearchViewState.EDITING);
    }

    private void stateFromEditingToNormal() {
        if(mDisplayMode == DisplayMode.TOOLBAR) {
            setCurrentState(SearchViewState.TOOLBAR);
            setSearchString("", false);
            closeSearchInternal();
        } else if(mDisplayMode == DisplayMode.MENUITEM) {
            setCurrentState(SearchViewState.MENUITEM);
            setSearchString("", false);
            hideCircularlyToMenuItem();
        }
        if(mSearchListener != null)
            mSearchListener.onSearchExit();
    }

    private void stateFromEditingToSearch() {
        setCurrentState(SearchViewState.SEARCH);
        closeSearchInternal();
    }

    private void stateFromSearchToNormal() {
        setLogoTextInt("");
        setSearchString("", true);
        if(mDisplayMode == DisplayMode.TOOLBAR) {
            setCurrentState(SearchViewState.TOOLBAR);
            closeSearchInternal();
        } else if(mDisplayMode == DisplayMode.MENUITEM) {
            setCurrentState(SearchViewState.MENUITEM);
            hideCircularlyToMenuItem();
        }
        if(mSearchListener != null)
            mSearchListener.onSearchExit();
    }

    private void setCurrentState(SearchViewState state) {
        mLastState = mCurrentState;
        mCurrentState = state;
    }

    public void openSearch() {
        if(mCurrentState == SearchViewState.TOOLBAR) {
            stateFromToolbarToEditing();
        } else if(mCurrentState == SearchViewState.MENUITEM) {
            stateFromMenuItemToEditing();
        } else if(mCurrentState == SearchViewState.SEARCH) {
            stateFromSearchToEditing();
        }
    }

    public void openSearch(View menuItemView, int desireRevealWidth) {
        if(mCurrentState == SearchViewState.MENUITEM) {
            revealFromMenuItem(menuItemView, desireRevealWidth);
            stateFromMenuItemToEditing();
        }
    }

    public void openSearch(View menuItemView) {
        if(mCurrentState == SearchViewState.MENUITEM) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            revealFromMenuItem(menuItemView, width);
            stateFromMenuItemToEditing();
        }
    }

    public void closeSearch() {
        if(mCurrentState == SearchViewState.EDITING) {
            stateFromSearchToNormal();
        } else if(mCurrentState == SearchViewState.SEARCH) {
            stateFromSearchToNormal();
        }
    }

    public void cancelEditing() {
        if(mLastState == SearchViewState.SEARCH)
            stateFromEditingToSearch();
        else
            stateFromEditingToNormal();
    }

    public interface SearchListener {

        /**
         * Called when the clear button is pressed
         */
        void onSearchCleared();

        /**
         * Called when the PersistentSearchView's EditText text changes
         */
        void onSearchTermChanged(String term);

        /**
         * Called when search happens
         * @param query search string
         */
        void onSearch(String query);

        /**
         * Called when search state change to SEARCH and EditText, Suggestions visible
         */
        void onSearchEditOpened();

        /**
         * Called when search state change from SEARCH and EditText, Suggestions gone
         */
        void onSearchEditClosed();

        /**
         * Called when search back to start state.
         */
        void onSearchExit();
    }

    public interface HomeButtonListener {
        /**
         * Called when the menu button is pressed
         */
        void onHomeButtonClick();
    }

    public interface VoiceRecognitionListener {
        /**
         * Called when the menu button is pressed
         */
        void onClick();
    }
}
