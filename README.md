# ![Froody](https://avatars1.githubusercontent.com/u/24797651?v=3&s=48) Froody-Android

<a name="badgers"></a>[![CircleCI](https://circleci.com/gh/froodyapp/froody-android.svg?style=shield)](https://circleci.com/gh/froodyapp/froody-android)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/froodyapp/localized.svg)](https://crowdin.com/project/froodyapp)
<span class="badge-bitcoin"><a href="https://gsantner.github.io/donate/#donate" title="Donate once-off to this project using Bitcoin"><img src="https://img.shields.io/badge/bitcoin-donate-yellow.svg" alt="Bitcoin donate button" /></a></span>

Android part of the [Froody](https://froodyapp.github.io) application.
Developed and maintained by [Gregor Santner](<https://gsantner.github.io>), 2016-
License: **GPLv3**

[![F-Droid](https://f-droid.org/wiki/images/0/06/F-Droid-button_get-it-on.png)](https://f-droid.org/repository/browse/?fdid=io.github.froodyapp) [![Google Play](https://raw.githubusercontent.com/froodyapp/froody-extras/master/img/screens/v0.2.0/en_badge_web_generic_small.png)](https://play.google.com/store/apps/details?id=io.github.froodyapp&referrer=utm_source%3Dgithub)

## Description
* Lets you share (mainly naturally growing) food to others
* The main idea is to share to and help other people by e.g. sharing pears, which would rot anyway if unused
* Intended to use in the local area - zoom in to your location and look for entries
* Everyone can add entries (and delete them), no login needed
* Entries will be available 60 days
* Available in many languages

**How to support the project?**

* Share the app and tell other people about it
* Tell things that could be improved
* Submit translations on Crowdin - https://crowdin.com/project/froodyapp
* Supply artwork or vector icons for the different types of entries


## Permissions & Privacy
* The app requests your location so you can create entries, and to automatically zoom to your location.
If you add a new entry your location gets sent to the setted server (default server uses https).
Others users can find entries when they swipe to this location. You can delete your entries by tapping the trash icon. Entries will get automatically deleted after 60 days.
* The app requests the storage permission for caching the Map-Tiles

## Notices
Influenced by ideas and snippets from other apps by gsantner ([kimai-android](https://github.com/gsantner/kimai-android), [dandelion\*](https://github.com/Diaspora-for-Android/dandelion), ..) which this app is allowed to use.
Main servers domain `froody-app.at` is sponsored by [Robert Diesenreither](http://www.zero-emission.at/index.php), the `froody-server` is hosted by @gsantner.
Project icon comes from the EmojiOne project, licensed CC-BY 4.0.

## Screenshots

<table>
  <tr>
    <td> <img src="https://raw.githubusercontent.com/froodyapp/froody-extras/master/img/screens/v0.2.0/map.png" alt="Map view"/> </td>
    <td> <img src="https://raw.githubusercontent.com/froodyapp/froody-extras/master/img/screens/v0.2.0/en/Screenshot_20170127-012429.png" alt="Detail view"/> </td>
  </tr><tr>
    <td> <img src="https://raw.githubusercontent.com/froodyapp/froody-extras/master/img/screens/v0.2.0/en/Screenshot_20170127-012402.png" alt="Publish view"/> </td>
    <td> <img src="https://raw.githubusercontent.com/froodyapp/froody-extras/master/img/screens/v0.2.0/en/Screenshot_20170127-012556.png" alt="Filter view" /> </td>
  </tr>
</table>
