# Persistent Search
A library that implements Google Play like PersistentSearch view.

- API: 14+
- 3 modes: Toolbar with drawer button, MenuItem and Toolbar with back button
- MenuItem mode reveal animation (Using [ozodrukh/CircularReveal](https://github.com/ozodrukh/CircularReveal))
- Voice recognition support.
- Use `android.support.v7.widget.CardView` to draw background and shadow, you can set `persistentSV_searchCardElevation` to modify shadow size.
- Most metrics measured from Google Play Store app.

## Demo
<a href="https://play.google.com/store/apps/details?id=org.cryse.widget.persistentsearch.sample">
  <img alt="Get it on Google Play"
       src="https://developer.android.com/images/brand/en_generic_rgb_wo_60.png" />
</a>

## Custom Attributes
```xml
<declare-styleable name="PersistentSearchView">
	<attr name="persistentSV_searchTextColor" format="color"/>
	<attr name="persistentSV_logoDrawable" format="reference"/>
	<attr name="persistentSV_editTextColor" format="color"/>
	<attr name="persistentSV_editHintText" format="string"/>
	<attr name="persistentSV_editHintTextColor" format="color"/>
	<attr name="persistentSV_searchCardElevation" format="dimension"/>
	<attr name="persistentSV_displayMode" format="enum">
		<enum name="displayAsMenuItem" value="0" />
		<enum name="displayAsToolbarDrawer" value="1" />
		<enum name="displayAsToolbarBackArrow" value="2" />
	</attr>
	<attr name="persistentSV_homeButtonColor" format="color"/>
</declare-styleable>
```
Note:
`android:elevation` attribute only decide z-axis position of SearchView, but not draw any shadow, shadow size is decided by `app:persistentSV_searchCardElevation`.

## Sample Usages
### Display as Toolbar with drawer button
`displayMode` should be `displayAsToolbarDrawer`

```xml
<org.cryse.widget.persistentsearch.PersistentSearchView
	android:id="@+id/searchview"
	android:layout_width="match_parent"
	android:layout_height="wrap_content"
	android:layout_alignParentTop="true"
	android:elevation="4dp"
	app:persistentSV_logoDrawable="@drawable/ic_logo"
	app:persistentSV_searchTextColor="?android:textColorPrimary"
	app:persistentSV_editTextColor="?android:textColorPrimary"
	app:persistentSV_editHintText="Search"
	app:persistentSV_editHintTextColor="?android:textColorHint"
	app:persistentSV_displayMode="displayAsToolbarDrawer"
	app:persistentSV_searchCardElevation="2dp"/>
```

### Display as Toolbar with BackArrow
Change the attribute in layout file:

`app:persistentSV_displayMode="displayAsToolbarBackArrow"`

### Display as MenuItem
Change the attributes in layout file:

`app:persistentSV_displayMode="displayAsMenuItem"`

`android:visibility="gone"`

When you need to show it, call `openSearch(View view)` to show SearchView, start position is determinate by `view` param, for example:
```java
	View menuItemView = findViewById(R.id.action_search);
	mSearchView.openSearch(menuItemView);
```
When you need to hide it, call `searchView.closeSearch()`
### Voice Recognition
If you want use voice recognition, just check the availability and setup:
```java 
	VoiceRecognitionDelegate delegate = new DefaultVoiceRecognizerDelegate(this, VOICE_RECOGNITION_REQUEST_CODE);
	if(delegate.isVoiceRecognitionAvailable()) {
		mSearchView.setVoiceRecognitionDelegate(delegate);
	}
```
then in onActivityResult():
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
		ArrayList<String> matches = data
			.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
		mSearchView.populateEditText(matches);  // Set result to PersistentSearchView
	}
	super.onActivityResult(requestCode, resultCode, data);
}
```

If you don't want to use default voice recognizer, you could inherit from abstract class VoiceRecognitionDelegate and implement your own recognizer.

## Screenshot

## Thanks

- The project originally came from  [Quinny898/PersistentSearch](https://github.com/Quinny898/PersistentSearch).
- [Ozodrukh/CircularReveal](https://github.com/ozodrukh/CircularReveal) for reveal animation.

## License

    Copyright 2015 Cryse Hillmes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.