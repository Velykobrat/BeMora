# BeMora

Android app for tracking movies, books and games, built with Kotlin and Jetpack Compose.

## Catalogs

- Movies: TMDB (existing integration).
- Books: [Open Library](https://openlibrary.org/dev/docs/api/search), no API key required. Search is work-based, so editions of the same book share a Library entry. Results include authors, first publication year and covers; details add the work description.
- Games: [RAWG](https://rawg.io/apidocs), requires your own API key. Results include release year, supported platforms and artwork; details add a description. RAWG ratings are converted from 0–5 to 0–10 for comparison with TMDB.

Search provides All / Movies / Books / Games filters and the first page of up to 20 results per catalog. All searches publish each catalog as soon as it finishes, while slower catalogs continue loading. Successful results stay visible when another catalog fails. Changing a query or category cancels the previous search. Successful queries are cached in memory for five minutes (up to 30 queries per repository). Open Library requests are serialized and limited to one per second.

Saved entries support Planned / In progress / Completed / Dropped statuses. Library filters, title search and sorting work across all three types. Profile shows totals by type and status. Saved details appear immediately and refresh in the background. Status edits and removal stay available during refresh; refresh responses preserve pending status choices and existing metadata when an optional field is missing. Book work details remain usable if the separate search index fails. Existing version-1 movie Library data and old movie routes are preserved.

## Discover

The home screen has independent shelves for popular TMDB movies, fiction books from the Open Library Subjects API, and popular RAWG games ordered by added count. Each shelf has its own loading, error and retry state; retries leave other shelves untouched. Cards open the shared details screen and can be added to Library. The page scrolls vertically. RAWG still requires the configured API key.

## Setup

Open the project in Android Studio with the SDK and Java toolchain configured by the Gradle files (SDK 37, Java 25 daemon toolchain). Add credentials to the root `local.properties`, alongside your SDK path:

```properties
TMDB_TOKEN=your_tmdb_read_access_token
RAWG_API_KEY=your_rawg_api_key
```

Get your RAWG key from [RAWG API registration](https://rawg.io/apidocs). Without it, Games shows a configuration message while Books and configured Movies still work. Do not commit `local.properties`; it is ignored by Git. The app links back to Open Library and RAWG where their catalog data appears. Check the provider's current terms for your intended use.

## Verification

```sh
./gradlew testDebugUnitTest assembleDebug
./gradlew connectedDebugAndroidTest
```

Unit tests cover provider dispatch, incremental search emissions, provider/collector cancellation, optional book metadata failures, immediate saved details, status edits/removal during refresh, independent Discover shelves and targeted retry, DTO mapping, ID validation, mixed/legacy persistence, Library filters and Profile counts. Instrumented tests also verify saving, updating, removing and restoring movies, books and games through SharedPreferences.

On a device, search for a book and game, open each result, add it to Library, change its status, restart the app and verify the type filters and Profile counts. Test All search without a RAWG key to verify that other catalog results remain visible.
