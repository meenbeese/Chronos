<div align="center">
  <h1>⏰ Chronos</h1>
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
  <table>
    <tr>
      <td style="width: 25%; height: 100px;"><img src="./.github/images/home.png" alt="Home" style="width: 100%; height: 100%;"></td>
      <td style="width: 25%; height: 100px;"><img src="./.github/images/alarms.png" alt="Alarms" style="width: 100%; height: 100%;"></td>
      <td style="width: 25%; height: 100px;"><img src="./.github/images/timers.png" alt="Timers" style="width: 100%; height: 100%;"></td>
      <td style="width: 25%; height: 100px;"><img src="./.github/images/themes.png" alt="Themes" style="width: 100%; height: 100%;"></td>
    </tr>
  </table>
</div>

## ⭐ Features

- Custom backgrounds & ringtones
- No unnecessary permissions
- Dark, Light, Amoled themes
- Granular controls everywhere
- Unique, minimal, efficient design
- Portrait and landscape orientation
- Countless default ringtones

## 📲 Installation

<div style="display: flex; justify-content: center; align-items: center; flex-direction: row;">
    <a href="https://apt.izzysoft.de/fdroid/index/apk/com.meenbeese.chronos">
        <img width="200" height="80" alt="Izzy Download" src="./assets/izzy_install_badge.png">
    </a>
    <a href="https://github.com/meenbeese/Chronos/releases/latest">
        <img width="200" height="84" alt="APK Download" src="./assets/apk_install_badge.png">
    </a>
</div>

## ⚙️ Permissions

- `SET_ALARM`, `VIBRATE`, `WAKE_LOCK`: necessary for the core functionality of the app
- `INTERNET`: fetching graphical assets and some of the information in the about page
- `RECEIVE_BOOT_COMPLETED`: automatically re-scheduling alarms on startup
- `READ_MEDIA_AUDIO`: setting custom ringtones for the alarms or timers
- `READ_MEDIA_IMAGES`: setting custom background / header images in the settings
- `FOREGROUND_SERVICE`: notifying you to sleep - see the "Sleep Reminder" option in the settings
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: please see [dontkillmyapp.com](https://dontkillmyapp.com/)

## 📝 Contribute to Chronos

Chronos is a user-driven project. We welcome any contribution, big or small.

- **🖥️ Development:** Fix bugs, implement features, or research issues. Open a PR for review.
- **🍥 Design:** Improve interfaces, including accessibility and usability.
- **🤝 User Support:** Respond to issues and identify duplicates.
- **📂 Issue Reporting:** Report bugs and edge cases with relevant info.
- **🌍 Localization:** Translate Chronos if it doesn't support your language.
- **📄 Documentation:** Write guides, explanations, README updates, user tutorials, or documentation.

### ✅ Other Support Methods

If you can't contribute directly, consider these options:

- **💈 Advertising:** Spread the word about Chronos.
- **💵 Donations:** [Support us](https://liberapay.com/meenbeese/) financially if you can.
- **📢 Advocacy:** Support FOSS services over closed ones.

## ✏️ Acknowledgements

Thanks to Chronos' contributors, the developers of our dependencies, and our users. Please support them with tips or thank-you notes if you like their work :)

## 🏗️ Building From Source

You need to use Android Studio to build this App.
Select `Project` from `Version Control` and paste the link of this repository.
Then go to `Build > APK > Create New Keystore > Enter the password`.

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
