# Immuni

[![BendingSpoons](https://circleci.com/gh/BendingSpoons/immuni-android-kotlin/tree/development.svg?style=shield)](https://app.circleci.com/pipelines/github/BendingSpoons/immuni-android-kotlin)

The Immuni Android app is a native app written in **Kotlin**.

Compatibility
-------------  

- Android 6+ (API level 23)
- Screen size: ALL  
- Device orientation: PORTRAIT ONLY
- Multi-windows enabled: NO
- Auto-backup enabled: NO

## Modules

 - **app**: UI and business logic
 - **base**: utilities
 - **theirs**: third party libraries
 - **oracle**: networking and API
 - **concierge**: IDs manager
 - **secret menu**: debugging menu

## Why does the app need location permissions?

We ask for location permission because Android/Google requires that apps requesting access to Bluetooth also obtain location permission, as Bluetooth can be used to derive location information when combined with beacons in fixed locations.

See [https://developer.android.com/guide/topics/connectivity/bluetooth](https://developer.android.com/guide/topics/connectivity/bluetooth)

In short, we do ask for location permissions because we need Bluetooth permissions. But Immuni does not collect or use location data.


## Architecture and Technical details

- *Android Architecture Components* (ViewModels, LiveData, Room, WorkManager) 
- Kotlin *coroutines* for all async operations (*Flows* and *Channels*)
- *Retrofit* + *OkHttp* + *Moshi* for networking
- *Koin* for Dependency Injection
- *Jetpack Navigation* for single activity fragments navigation with multiple back stacks
- *Jetpack Security* for data encryption

Testing
-------------

To run unit tests:
```
./gradlew clean assembleDebug testDebug
```
To run instrumentation tests:
```
./gradlew connectedAndroidTest
```

## Contribute


```
git clone git@github.com:BendingSpoons/immuni-android-kotlin.git
```