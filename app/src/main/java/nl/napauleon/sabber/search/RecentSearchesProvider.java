package nl.napauleon.sabber.search;

import android.content.SearchRecentSuggestionsProvider;

public class RecentSearchesProvider extends SearchRecentSuggestionsProvider {
    public static final String AUTHORITY = "nl.napauleon.sabber.search";
    public static final int MODE = DATABASE_MODE_QUERIES;

    public RecentSearchesProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
