
<h1 align="center">Immuni Android</h1>
 
<div align="center">
<img widht="256" height="256" src=".github/logo.png">
</div>

<br />

<div align="center">
    <!-- Latest Release -->
    <a href="https://github.com/immuni-app/app-android/releases">
      <img alt="GitHub release (latest SemVer)"
      src="https://img.shields.io/github/v/release/immuni-app/app-android">
    </a>
    <!-- CoC -->
		<a href="CODE_OF_CONDUCT.md">
      <img src="https://img.shields.io/badge/Contributor%20Covenant-v2.0%20adopted-ff69b4.svg" />
    </a>
    <a href="https://circleci.com/gh/immuni-app/immuni-app-android">
      <img alt="Circle CI Status"
      src="https://circleci.com/gh/immuni-app/immuni-app-android.svg?style=svg">
    </a>
</div>

<div align="center">
  <h3>
    <a href="https://github.com/immuni-app/documentation">
      Documentation
    </a>
    <span> | </span>    
    <a href="CONTRIBUTING.md">
      Contributing
    </a>
  </h3>
</div>

# Table of contents

- [Introduction](#introduction)
- [Installation](#installation)
  - [Backend services](#backend-services)
- [Testing](#testing)
- [Checking the build](#checking-the-build)
- [Contributing](#contributing)
  - [Contributors](#contributors)
- [License](#license)
  - [Authors / Copyright](#authors--copyright)
  - [Third-party component licenses](#third-party-component-licenses)
    - [Tools](#tools)
    - [Libraries](#libraries)
  - [License details](#license-details)

# Introduction

This repository contains the source code of Immuni's Android client. More detailed information about Immuni can be found in the following documents:

- [High-Level Description](https://github.com/immuni-app/documentation)
- [Product Description](https://github.com/immuni-app/documentation/blob/master/Product%20Description.md)
- [Technology Description](https://github.com/immuni-app/documentation/blob/master/Technology%20Description.md)
- [Traffic Analysis Mitigation](https://github.com/immuni-app/immuni-documentation/blob/master/Traffic%20Analysis%20Mitigation.md)

**Please take the time to read and consider these documents in full before digging into the source code or opening an Issue. They contain a lot of details that are fundamental to understanding the source code and this repository's documentation.**

# Installation

The app can be installed using [Android Studio](https://developer.android.com/studio) or the [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) (gradlew) command line tool.

## Using Android Studio

This is the recommended and most straightforward method. First, clone the repository with:

```sh
git clone git@github.com:immuni-app/immuni-app-android.git
```

From Android Studio, select *Import Project*, then select the root folder of the cloned repository.
Click *Make Project* to build the app and download all the required dependencies.
Click *Run app* to install the app on your device or emulator.

## Using the Gradle Wrapper command line tool

The Gradle Wrapper can be built using [Gradle](https://docs.gradle.org/current/userguide/installation.html#installation). You can install Gradle using [Brew](https://brew.sh/):

```sh
brew install gradle
```

To generate the wrapper, execute this task:

```sh
gradle wrapper
```
 
Clone the repository with:

```sh
git clone git@github.com:immuni-app/immuni-app-android.git
```

Enter the project root folder with:

```sh
cd immuni-app-android
```

Execute the command:

  
```sh
./gradlew assembleDebug
```
  
This creates an APK named *app-debug.apk* in *immuni-app-android/app/build/outputs/apk/*. The file is already signed with the debug key and aligned with [zipalign](https://developer.android.com/studio/command-line/zipalign), so you can immediately install it on a device.

To build the APK and immediately install it on a running emulator or connected device, instead invoke installDebug:

```sh
./gradlew installDebug
```
  

>Please note that Google restricts the usage of the [Exposure Notification API](https://www.google.com/covid19/exposurenotifications/) to government entities or developers approved by a government entity to develop an application on behalf of a government for COVID-19 response efforts. Full details are in the [Additional Terms](https://blog.google/documents/72/Exposure_Notifications_Service_Additional_Terms.pdf) document. Otherwise, you may build and use the application, but you will not be able to use the underlying Exposure Notification system.

For more information about how the project is generated and structured, please refer to the [CONTRIBUTING](CONTRIBUTING.md) file.

# Testing

The repository contains several unit and integration tests to guarantee high code quality and maintainability.

To run the local unit tests, execute the following Gradle task from the desired app module:

```sh
./gradlew :[module name]:testDebugUnitTest
```

To run the instrumentation/UI tests, execute the following Gradle task:

```sh
./gradlew connectedAndroidTest
```

# Checking the build

In addition to making the code open-source, we wish to help people verify that builds published on the App Store are coming from a specific commit of this repository. Please refer to the [Immuni Technology Description](https://github.com/immuni-app/documentation/blob/master/Technology%20Description.md#android-app-technologies) for a complete overview of the goals and status of this effort.

Currently, we have a working open continuous integration for building the client. [Here](.circleci/config.yml) is the full specification. When it comes to reproducible builds, we will instead open an issue explaining what we have done so far and any missing steps.

# Contributing

Contributions are most welcome. Before proceeding, please read the [Code of Conduct](CODE_OF_CONDUCT.md) for guidance on how to approach the community and create a positive environment. Additionally, please read our [CONTRIBUTING](CONTRIBUTING.md) file, which contains guidance on ensuring a smooth contribution process.

The Immuni project is composed of different repositories—one for each component or service. Please use this repository for contributions strictly relevant to the Immuni Android client. To propose a feature request, please open an issue in the [Documentation repository](https://github.com/immuni-app/documentation). This lets everyone involved see it, consider it, and participate in the discussion. Opening an issue or pull request in this repository may slow down the overall process.

## Contributors

Here is a list of Immuni's contributors. Thank you to everyone involved for improving Immuni, day by day.

<a href="https://github.com/immuni-app/immuni-app-android/graphs/contributors">
  <img
  src="https://contributors-img.web.app/image?repo=immuni-app/immuni-app-android"
  />
</a>

# License

## Authors / Copyright

2020 (c) Presidenza del Consiglio dei Ministri.

## Third-party component licenses

### Tools

| Name                                                        | License                   |
| ----------------------------------------------------------- | ------------------------- |
| [Brew](https://brew.sh/)                                    | BSD 2-Clause 'Simplified' |
| [Gradle](https://gradle.org/)                         | Apache 2.0                       |
| [CommitLint](https://commitlint.js.org/#/)                  | MIT                       |
| [Danger](https://danger.systems/js/)                        | MIT                       |
| [Ktlint](https://github.com/pinterest/ktlint) | MIT                       |


### Libraries

| Name                                                       | License    |
| ---------------------------------------------------------- | ---------- |
| [Glide](https://github.com/bumptech/glide)    | Apache 2.0        |
| [Koin](https://github.com/InsertKoinIO/koin) | Apache 2.0        |
| [Lottie](https://github.com/airbnb/lottie-android)       | Apache 2.0        |
| [Moshi](https://github.com/square/moshi)          | Apache 2.0        |
| [MockK](https://github.com/mockk/mockk)             | Apache 2.0        |
| [OkHttp](https://github.com/square/okhttp/)            | Apache 2.0 |
| [Retrofit](https://github.com/square/retrofit)      | Apache 2.0        |

## License details

The licence for this repository is a [GNU Affero General Public Licence version 3](https://www.gnu.org/licenses/agpl-3.0.html) (SPDX: AGPL-3.0). Please see the [LICENSE](LICENSE) file for full reference.
