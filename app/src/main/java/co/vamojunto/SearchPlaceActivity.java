package co.vamojunto;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.EditText;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import co.vamojunto.adapters.SearchPlaceAdapter;
import co.vamojunto.helpers.GooglePlacesHelper;


public class SearchPlaceActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_place, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        private static final String TAG = "SearchPlaceFragment";

        private RecyclerView mRecyclerView;
        private SearchPlaceAdapter mAdapter;
        private RecyclerView.LayoutManager mLayoutManager;
        private GooglePlacesHelper mGooglePlacesHelper;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search_place, container, false);

            initComponents(rootView);

            return rootView;
        }

        private void initComponents(View rootView) {
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_recycler_view);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            mRecyclerView.setHasFixedSize(true);

            // use a linear layout manager
            mLayoutManager = new LinearLayoutManager(rootView.getContext());
            mRecyclerView.setLayoutManager(mLayoutManager);

            // specify an adapter (see also next example)
            mAdapter = new SearchPlaceAdapter(rootView.getContext(), Arrays.asList("Item 1", "Item 2", "Item 1", "Item 2", "Item 1", "Item 2", "Item 1", "Item 2", "Item 1", "Item 2"));
            mRecyclerView.setAdapter(mAdapter);

            mGooglePlacesHelper = new GooglePlacesHelper(rootView.getContext());

            EditText localEditText = (EditText) rootView.findViewById(R.id.local_edit_text);
            localEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(final CharSequence s, int start, int before, int count) {
                    Log.d(TAG, s.toString());

                    if ( s.length() > 2 ) {
                        mGooglePlacesHelper.autocompleteAsync(s.toString()).
                                continueWith(new Continuation<List<String>, Void>() {
                            @Override
                            public Void then(Task<List<String>> task) throws Exception {
                                Log.d(TAG, "Finalizou a consulta");
                                mAdapter.setDataset(task.getResult());
                                mRecyclerView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });

                                return null;
                            }
                        });
                    }
                }

                @Override
                public void afterTextChanged(Editable s) { }
            });
        }

    }
}
