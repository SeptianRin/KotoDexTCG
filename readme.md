# KotoDex TCG

KotoDex TCG is a modern Android application built with 100% Kotlin and Jetpack Compose. It serves as a Pokédex for the Pokémon Trading Card Game, allowing users to browse, search, and collect cards. The app also features an engaging gacha-style booster pack opening simulator.

This project was developed as a take-home assignment, demonstrating best practices in modern Android development, including a clean MVVM architecture, dependency injection, reactive UI, and a full CI/CD pipeline for automated releases.

## ✨ Features

* **Card Catalog:** An infinitely scrolling grid that displays Pokémon cards from the official Pokémon TCG API.
* **Advanced Search:** A functional, debounced search bar to filter cards by name, type, and `evolvesFrom`.
* **Card Detail Page:** A dedicated screen showing rich card details, including HP, rarity, flavor text, attacks with visual energy costs, and a dynamic background that changes based on the Pokémon's type.
* **Interactive Gacha Simulator:**
    * A dedicated screen for opening booster packs.
    * A weighted rarity system to simulate realistic pack contents.
    * An engaging, animated pack-opening experience where the user "slashes" the pack to open it, complete with haptic feedback and a rainbow slash trail.
* **Local Collection:**
    * A "My Collection" screen that displays all cards obtained from the gacha simulator.
    * Uses a local Room database to persist the user's collection.
    * Displays a badge on card items to show the count of duplicates.
    * A confirmation dialog for clearing the entire collection.
* **Advanced UI/UX:**
    * **Zoomable Card View:** An overlay for viewing high-resolution card images with pinch-to-zoom functionality.
    * **UI Polish:** A global theme toggle for light/dark mode, smooth animations for card placement, and custom info states for empty or error conditions.
* **CI/CD Pipeline:**
    * A complete GitHub Actions workflow that automatically runs unit tests on every push to `main`.
    * On a successful push, it builds, signs, and creates a new release with an installable APK.

## 🛠 Tech Stack & Architecture

This project follows a modern, feature-oriented MVVM architecture with a Unidirectional Data Flow (UDF) pattern.

* **Tech Stack:**
    * **Language:** 100% [Kotlin](https://kotlinlang.org/)
    * **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
    * **Asynchronous Programming:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
    * **Architecture:** MVVM (Model-View-ViewModel) with UDF
    * **Dependency Injection:** [Koin](https://insert-koin.io/)
    * **Networking:** [Retrofit](https://square.github.io/retrofit/) & [OkHttp](https://square.github.io/okhttp/)
    * **Image Loading:** [Coil](https://coil-kt.github.io/coil/)
    * **Local Database:** [Room](https://developer.android.com/training/data-storage/room)
    * **Testing:** JUnit4, MockK, Turbine
* **Architecture:**
    * **Feature-Based Packaging:** The UI layer is organized by feature (e.g., `cardlist`, `gacha`, `collection`), with each feature containing its own View and ViewModel.
    * **Repository Pattern:** A repository abstracts the data sources (network and local database) from the rest of the app.
    * **Clean & Reusable Components:** Shared UI elements are separated into a `component` package.

## 🤖 CI/CD

This project uses **GitHub Actions** for Continuous Integration and Continuous Deployment. The workflow is defined in `.github/workflows/android-release.yml`.

On every push to the `main` branch, the workflow will:

1.  Set up a Java environment.
2.  Decode the Base64 keystore from GitHub Secrets.
3.  Run all unit tests.
4.  If the tests pass, build a signed release APK.
5.  Create a new tag and release on GitHub.
6.  Upload the signed APK as a release asset.

To enable this for your fork, you must configure the four required secrets (`KEYSTORE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) in your repository's settings.

---
*This project was built with passion and a love for clean code. Enjoy!*