# Dictionary App (Android)

A simple Android dictionary application that allows users to search for word definitions. The app is built with considerations for e-ink display devices, keeping the UI minimal. Built with Kotlin, 
Material Components, and Room for local data persistence.

## Features

*   Search for word definitions.
*   View search history (if implemented or planned).
*   Clean, user-friendly interface.
*   (Add any other key features you have or plan, e.g., "Offline access to previously searched words")

## Screenshots

![IMG_0088](https://github.com/user-attachments/assets/d911a442-996e-462b-abff-19bf69ee70cc)

![IMG_0090](https://github.com/user-attachments/assets/f88ffcec-80b4-4058-8a7a-bf9b62444294)

## Tech Stack & Libraries

*   **Kotlin:** Primary programming language.
*   **Android SDK:** Core Android framework.
*   **Material Components for Android:** For UI elements and theming.
*   **Room Persistence Library:** For local database storage (e.g., search history, cached definitions).
*   **Coroutines & Flow:** For asynchronous operations.
*   **ViewModel & LiveData/StateFlow:** For UI-related data lifecycle management.
*   **Retrofit & Gson/Moshi:** (If fetching definitions from a remote API) For networking.
*   **Hilt/Koin:** (If using dependency injection)

## Setup / Build

1.  Clone the repository:
    bash git clone [https://github.com/findEthics/Dictionary.git](https://github.com/findEthics/Dictionary.git)
2.  Open the project in Android Studio (latest stable version recommended).
3.  Let Gradle sync and download dependencies.
4.  Build and run the app on an emulator or physical device.
