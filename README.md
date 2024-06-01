<div align="center">
  <img src="./fastlane/metadata/android/en-US/images/icon.png" width="15%" alt="Chronos Logo">
  <h1>Chronos</h1>
  <p>A simple, quick, and intuitive alarm clock with many useful features.</p>
  <img src="https://forthebadge.com/images/badges/built-for-android.svg" alt="Built for Android">
  <img src="https://forthebadge.com/images/badges/built-with-love.svg" alt="Built with love">
  <br>
  <a href="https://github.com/meenbeese/Chronos/actions/workflows/android.yml">
    <img src="https://github.com/meenbeese/Chronos/actions/workflows/android.yml/badge.svg?branch=main" alt="Build Status">
  </a>
  <a href="https://liberapay.com/meenbeese/">
    <img src="https://img.shields.io/badge/liberapay-donate-yellow.svg?logo=liberapay" alt="Liberapay">
  </a>
</div>

## 🌄 Screenshots

<div align="center">
    <img src="./assets/images/home.png" alt="Home" style="width: 24%"> <img src="./assets/images/alarms.png" alt="Alarms" style="width: 24%"> <img src="./assets/images/timers.png" alt="Timers" style="width: 24%"> <img src="./assets/images/themes.png" alt="Themes" style="width: 24%">
</div>

## ⭐ Features

- Custom backgrounds & ringtones
- No unnecessary permissions
- Dark, Light, AMOLED themes
- Granular controls everywhere
- Unique, minimal, efficient design
- Portrait and landscape orientation
- Countless default ringtones

## 📲 Installation

<a href="https://apt.izzysoft.de/fdroid/index/apk/com.meenbeese.chronos"><img height="80" alt="Izzy Download" src="./assets/badges/izzy_install_badge.png" align="center"></a>
<a href="https://github.com/meenbeese/Chronos/releases/latest"><img height="80" alt="APK Download" src="./assets/badges/apk_install_badge.png" align="center"></a>
<a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/meenbeese/Chronos"><img height="55" alt="Obtainium Download" src="./assets/badges/badge_obtainium.png" align="center"></a>

## ⚙️ Permissions

- `SET_ALARM`, `VIBRATE`, `WAKE_LOCK`: necessary for the core functionality of the app
- `INTERNET`: fetching some of the graphical assets in the about page
- `RECEIVE_BOOT_COMPLETED`: automatically re-scheduling alarms on startup
- `READ_MEDIA_AUDIO`: setting custom ringtones for the alarms or timers
- `READ_MEDIA_IMAGES`: setting custom background / header images in the settings
- `FOREGROUND_SERVICE`: notifying you to sleep - see the "Sleep Reminder" option in the settings
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: please see [dontkillmyapp.com](https://dontkillmyapp.com/)

## 📝 Contribute to Chronos

Chronos is a user-driven project. We welcome any contribution, big or small.

- **Development:** Fix bugs, implement features, or research issues. Open a PR for review.
- **Design:** Improve interfaces, including accessibility and usability.
- **User Support:** Respond to issues and identify duplicates.
- **Issue Reporting:** Report bugs and edge cases with relevant info.
- **Localization:** Translate Chronos if it doesn't support your language.
- **Documentation:** Write guides, explanations, README updates, or documentation.

### ✅ Other Support Methods

If you can't contribute directly, consider these options:

- **Advertising:** Spread the word about Chronos.
- **Donations:** [Support us](https://liberapay.com/meenbeese/) financially if you can.
- **Advocacy:** Support FOSS services over closed ones.

## ✏️ Acknowledgements

Thanks to Chronos' contributors, the developers of our dependencies, and our users. Please support them with tips or thank-you notes if you like their work :)

## 🏗️ Building From Source

1. First you need to get the source code of Chronos.
```sh
git clone https://github.com/meenbeese/Chronos.git
```
2. Open the project in [Android Studio](https://developer.android.com/studio).
3. When you click the `▶ Run` button, it will be built automatically.
4. Launch Chronos.

## 📝 License

```
Copyright (C) 2024 Meenbeese

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at:

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
```
