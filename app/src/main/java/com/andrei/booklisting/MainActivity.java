package com.andrei.booklisting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String ADAPTER_ITEMS = "items";
    private static final String LIST_FIRST_POSITION = "position";
    private static final String GOOGLE_BOOKS_REQUEST_URL = "https://www.googleapis.com/books/v1";
    private static final String SEARCH_MADE = "searchMade";

    @BindView(R.id.search) SearchView searchView;
    @BindView(R.id.list) ListView booksListView;
    @BindView(R.id.empty_view) TextView emptyTextView;
    @BindView(R.id.loading_spinner) ProgressBar loadingSpinner;

    private BookAdapter adapter;
    private boolean isConnected;
    private boolean searchMade = false;
    private String mQuery;
    private Context mContext = this;
    private SearchView.OnQueryTextListener searchViewOnQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            emptyTextView.setVisibility(View.GONE);
            if (isConnected) {
                mQuery = query.trim();
                new SearchAsyncTask().execute(GOOGLE_BOOKS_REQUEST_URL);
                loadingSpinner.setVisibility(View.VISIBLE);
            } else {
                adapter.clear();
                loadingSpinner.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyTextView.setText(R.string.no_internet);
            }
            searchView.clearFocus();
            searchMade = true;
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        booksListView.setEmptyView(emptyTextView);
        emptyTextView.setText(R.string.instructions);

        if (savedInstanceState != null) {
            searchMade = savedInstanceState.getBoolean(SEARCH_MADE);
            ArrayList<Book> items = savedInstanceState.getParcelableArrayList(ADAPTER_ITEMS);
            if (items != null) {
                adapter = new BookAdapter(this, items);
                booksListView.setAdapter(adapter);
            }
            if (adapter.isEmpty() && isConnected && searchMade) {
                emptyTextView.setText(R.string.no_books_found);
                emptyTextView.setVisibility(View.VISIBLE);
            } else if (adapter.isEmpty() && !isConnected && searchMade) {
                emptyTextView.setText(R.string.no_internet);
                emptyTextView.setVisibility(View.VISIBLE);
            }
            booksListView.setSelectionFromTop(savedInstanceState.getInt(LIST_FIRST_POSITION), 0);
        } else {
            adapter = new BookAdapter(this, new ArrayList<Book>());
            booksListView.setAdapter(adapter);
        }

        searchView.setOnQueryTextListener(searchViewOnQueryListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(ADAPTER_ITEMS, adapter.getAllItems());
        outState.putInt(LIST_FIRST_POSITION, booksListView.getFirstVisiblePosition());
        outState.putBoolean(SEARCH_MADE, searchMade);
    }

    private class SearchAsyncTask extends AsyncTask<String, Void, List<Book>> {

        @Override
        protected List<Book> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            String searchIn = sharedPrefs.getString(
                    getString(R.string.setting_search_in_key),
                    getString(R.string.settings_search_in_default));
            String maxResults = sharedPrefs.getString(
                    getString(R.string.settings_max_results_key),
                    getString(R.string.settings_max_results_default));
            String orderBy = sharedPrefs.getString(
                    getString(R.string.settings_order_by_key),
                    getString(R.string.settings_order_by_default));
            Uri baseUri = Uri.parse(urls[0]);
            Uri.Builder uriBuilder = baseUri.buildUpon();

            uriBuilder.appendEncodedPath(searchIn + mQuery);
            baseUri = Uri.parse(uriBuilder.toString());
            uriBuilder.clearQuery();
            uriBuilder = baseUri.buildUpon();
            uriBuilder.appendQueryParameter(getString(R.string.settings_order_by_key), orderBy);
            uriBuilder.appendQueryParameter(getString(R.string.settings_max_results_key), maxResults);
            uriBuilder.appendQueryParameter("printType", "books");
            uriBuilder.appendQueryParameter("projection", "lite");

            return Utils.fetchBookData(uriBuilder.toString());
        }

        @Override
        protected void onPostExecute(List<Book> books) {
            emptyTextView.setText(R.string.no_books_found);
            emptyTextView.setVisibility(View.VISIBLE);
            loadingSpinner.setVisibility(View.GONE);
            if (books == null) {
                return;
            }

            updateUi(books);
        }
    }

    private void updateUi(List<Book> books) {
        adapter.clear();
        adapter.addAll(books);
    }
}
