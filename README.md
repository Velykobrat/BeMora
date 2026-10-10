# BeMora

Android app for tracking movies, books and games, built with Kotlin and Jetpack Compose.

## Catalogs

- Movies: TMDB (existing integration).
- Books: [Open Library](https://openlibrary.org/dev/docs/api/search), no API key required. Search is work-based, so editions of the same book share a Library entry. Results include authors, first publication year and covers; details add the work description.
- Games: [RAWG](https://rawg.io/apidocs), requires your own API key. Results include release year, supported platforms and artwork; details add a description. RAWG ratings are converted from 0–5 to 0–10 for comparison with TMDB.

Search provides All / Movies / Books / Games filters and the first page of up to 20 results per catalog. All searches retain successful results if another provider fails and show which catalog needs attention. Changing a query or category cancels the previous search. Successful queries are cached in memory for five minutes (up to 30 queries per repository). Open Library requests are serialized and limited to one per second.

Saved entries support Planned / In progress / Completed / Dropped statuses. Library filters, title search and sorting work across all three types. Profile shows totals by type and status. Saved details remain available when a catalog cannot be reached. Existing version-1 movie Library data and old movie routes are preserved.

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

Unit tests cover provider dispatch, partial failures, cancellation, DTO mapping, ID validation, mixed Library persistence, legacy movie data, combined filters and Profile counts. Instrumented tests also verify saving, updating, removing and restoring movies, books and games through SharedPreferences.

On a device, search for a book and game, open each result, add it to Library, change its status, restart the app and verify the type filters and Profile counts. Test All search without a RAWG key to verify that other catalog results remain visible.
