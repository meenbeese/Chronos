⏰ Alarmio
[![Build Status](https://github.com/fennifith/Alarmio/workflows/Gradle%20Build/badge.svg)](https://github.com/meenbeese/Alarmio/actions)
[![Liberapay](https://img.shields.io/badge/liberapay-donate-yellow.svg?logo=liberapay)](https://liberapay.com/meenbeese/)
=======

<img src="https://forthebadge.com/images/badges/built-for-android.svg" alt="badge one"> <img src="https://forthebadge.com/images/badges/built-with-love.svg" alt="badge two"/>

Alarmio is a simple alarm clock that implements many useful features while following regular design standards to ensure that it is quick and intuitive to use.

## 🌄 Screenshots    

[<img src="./.github/images/home.png" height=80% width=20% alt="Home">](./.github/images/home.png)
[<img src="./.github/images/alarms.png" height=80% width=20% alt="Alarms">](./.github/images/alarms.png)
[<img src="./.github/images/timers.png" height=80% width=20% alt="Timers">](./.github/images/timers.png)
[<img src="./.github/images/themes.png" height=80% width=20% alt="Themes">](./.github/images/themes.png)

## ⭐ Features

- Custom backgrounds & ringtones,
- No unnecessary permissions,
- Dark, Light, Amoled themes
- Granular controls everywhere
- Unique, minimal, efficient design,
- Custom backgrounds & Internet radio
- Portrait and landscape orientation
- Countless default ringtones

## ⚙️ Permissions

- `SET_ALARM`, `VIBRATE`, `WAKE_LOCK`: necessary for the core functionality of the app
- `ACCESS_COARSE_LOCATION`: determining automatic sunrise/sunset times for "scheduled" light/dark themes
- `INTERNET`: obtaining a set of sunrise/sunset times (only stored locally) and fetching graphical assets and some of the information in the about page
- `RECEIVE_BOOT_COMPLETED`: re-scheduling alarms on startup
- `READ_EXTERNAL_STORAGE`: setting custom background / header images in the settings
- `FOREGROUND_SERVICE`: notifying you to sleep - see the "Sleep Reminder" option in the settings
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`: please see [dontkillmyapp.com](https://dontkillmyapp.com/)

## 📲 Installation

[<img width='200' height='80' alt='F-Droid Download' src='https://fdroid.gitlab.io/artwork/badge/get-it-on.png'>](https://f-droid.org/packages/me.jfenn.Alarmio/)
[<img width='200' height='80' alt='APK Download' src='https://user-images.githubusercontent.com/114044633/223920025-83687de0-e463-4c5d-8122-e06e4bb7d40c.png'>]((../../releases/))

## 📝 How to Contribute

Alarmio is written by users - everyone working on this project (including myself) is motivated entirely by their own needs and ideals. There is no professional support line to handle complaints or paid development team to fix bugs. Anyone that volunteers their time and expertise to improve this app does so without any obligation or commitment to the users of this software.

As a contributor, you are highly valued - any contribution, no matter how small, misled, or incorrectly formatted, is welcome. We'd much prefer to work together and resolve an issue than turn any genuine effort away. **If you need help with this process, have any questions or confusion, or want to get feedback before a contribution, please don't hesitate to get in touch.**

- **🖥️ Development:** Developers can help Alarmio by [fixing bugs](https://github.com/meenbeese/Alarmio/issues), implementing features, or helping to debug & research new issues. After reviewing [CONTRIBUTING.md](./.github/CONTRIBUTING.md), you can open a PR request which will be reviewed. If it looks good to me, I will merge it into the main branch.
- **🍥 Designing:** Alarmio should be intuitive and accessible to a wide variety of users - suggestions to improve certain interfaces are always welcome. This includes compatibility with screen readers, problems with contrast / color blindness, and the sizing/positioning of touch targets in the app - many of which are shamefully untested in Alarmio's present state.
- **🤝 Helping users:** Often times, issues are created that go untouched for a couple days due to various factors, when they could be resolved immediately. Responding to obvious questions and identifying duplicate issues can be immensely helpful in reducing the workload on other maintainers and developers.
- **📂 Filing issues:** Accurately reporting bugs and edge cases you experience can drastically reduce the amount of work we need to do to identify a problem. Providing relevant info - the device name & manufacturer, Android version, the version of Alarmio the bug was encountered in, and a thorough description helps others to understand what the issue is and how it could be solved. If you want to go the extra mile, screen recordings and logcat info are often immensely helpful. Don't forget to check for existing similar issues though!
- **🌍 Localization:** If Alarmio doesn't have support for your fluent language(s), please consider translating it! Most in-app text is stored in [strings.xml](./app/src/main/res/values/strings.xml) - this file should be copied to `../values-{lang}/strings.xml` when translated. (this is an absurdly concise explanation - if this isn't clear, simply sending us translations in a new issue or email is perfectly fine!)
- **📄 Documentation:** Writing guides and explanations of how Alarmio works, how to use it, and how to contribute to it, can go a long way to ensuring its usefulness and stability in the future. Whether this involves an update to the README, a tutorial for users and contributors, or adding Javadocs & comments to undocumented functions in the app - anything is valid!

### ✅ Other ways of support

Not everyone has the time or technical knowledge to help out with many of the above - we understand that. With this in mind, here are a few other ways to help us out that don't require as much time or dedication.

- **💈 Advertising:** Spread the word! If you like what we're doing here, getting more people involved is the best way to help improve it. Suggestions include: posting on social media... writing a blog post... yeah, right, you get the idea.
- **💵 Donations:** I've invested a lot of time into this app - as have many of its contributors. Many of us have paying jobs, difficult classes, or otherwise important life occurrences that prevent us from putting all our time into software.
- **📢 Politics:** Yep, you're reading this right. Free software is, in fact, a very political thing - and I'd like to think we're taking the right approach. If you can, consider supporting FOSS applications and services over proprietary ones.

## ✏️ Acknowledgements

I would like to give a huge thanks to all of Alarmio's [contributors](https://github.com/fennifith/Alarmio/graphs/contributors), the developers that write the software we depend on, and the users that support our goal. Also, props to [F-Droid](https://f-droid.org/en/about/) for maintaining the free software repository that distributes our app and many others like it.

I have received a lot of thanks from various people for the time I've put into this, and that thought helps me get up in the morning. If someone fixes a bug you encountered, helps you out in an issue, or implements a feature you enjoy, please consider sending them a tip or a thank-you note to let them know that you appreciate their time :)

## 🏗️ Building From Source

You need Android Studio to build this App.
After Installing Android Studio, select `Project` from `Version Control` and paste the link of this repository.
Navigate to `Build > APK > Create New Keystore > Enter the password` and wait for the build to finish.

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
