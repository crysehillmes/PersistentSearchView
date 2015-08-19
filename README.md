#Persistent Search
A library that implements PersistentSearch View in Google Play app.

- API: 14+
- 3 Modes: Toolbar with burger, MenuItem and SearchView
- MenuItem mode reveal animation (Using [ozodrukh/CircularReveal](https://github.com/ozodrukh/CircularReveal))
- Use `android.support.v7.widget.CardView` to draw background and shadow, you can set `persistentSV_searchCardElevation` to modify shadow size.
- Most metrics measured from Google Play Store app.

##Demo
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

### Display as MenuItem
`displayMode` should be `displayAsMenuItem`, and `android:visibility="gone"`
```xml
<org.cryse.widget.persistentsearch.PersistentSearchView
        android:layout_width="match_parent"
        android:id="@+id/searchview"
        android:layout_height="wrap_content"
        android:visibility="gone"
        android:layout_alignParentLeft="true"
        android:layout_alignParentTop="true"
        android:elevation="6dp"
        app:persistentSV_displayMode="displayAsMenuItem"
        app:persistentSV_searchTextColor="#DE000000"
        app:persistentSV_editTextColor="#DE000000"
        app:persistentSV_editHintText="Search"
        app:persistentSV_editHintTextColor="#61000000"
        app:persistentSV_searchCardElevation="4dp"/>
```
In Java, call `openSearch(View view)` to show SearchView, start position is determinate by `view` param, for example:
```java
        View menuItemView = findViewById(R.id.action_search);
        mSearchView.openSearch(menuItemView);
```
### Display as Toolbar with BackArrow
`displayMode` should be `displayAsToolbarBackArrow`
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
        app:persistentSV_displayMode="displayAsToolbarBackArrow"
        app:persistentSV_searchCardElevation="2dp"/>
```

##Screenshot
##Thanks

- The project originally came from  [Quinny898][PersistentSearch].
- [Ozodrukh][CircularReveal] for reveal animation.

##License

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