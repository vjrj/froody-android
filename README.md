# ![Froody](https://avatars1.githubusercontent.com/u/24797651?v=3&s=48) Froody-Android

<a name="badgers"></a>[![CircleCI](https://circleci.com/gh/froodyapp/froody-android.svg?style=shield)](https://circleci.com/gh/froodyapp/froody-android)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/froodyapp/localized.svg)](https://crowdin.com/project/froodyapp)
<span class="badge-bitcoin"><a href="https://gsantner.github.io/donate/#donate" title="Donate once-off to this project using Bitcoin"><img src="https://img.shields.io/badge/bitcoin-donate-yellow.svg" alt="Bitcoin donate button" /></a></span>[![Chat - Matrix](https://img.shields.io/badge/chat-on%20matrix-blue.svg)](https://matrix.to/#/#froodyapp:matrix.org) [![Chat - FreeNode IRC](https://img.shields.io/badge/chat-on%20irc-blue.svg)](https://kiwiirc.com/client/irc.freenode.net/?nick=froodyapp-anon|?#froodyapp)


Android part of the [Froody](https://froodyapp.github.io) application.
Developed and maintained by [Gregor Santner](<https://gsantner.github.io>), 2016-  
License: **Apache 2.0**  

[![F-Droid](https://f-droid.org/wiki/images/0/06/F-Droid-button_get-it-on.png)](https://f-droid.org/repository/browse/?fdid=io.github.froodyapp) [![Google Play](https://raw.githubusercontent.com/froodyapp/froody-extras/master/graphics/stores/en_badge_web_generic_small.png)](https://play.google.com/store/apps/details?id=io.github.froodyapp&referrer=utm_source%3Dgithub)

## Description
* Lets you share (mainly naturally growing) food to others
* The main idea is to share to and help other people by e.g. sharing pears, which would rot anyway if unused
* Intended to use in the local area - zoom in to your location and look for entries
* Everyone can add entries (and delete them), no login needed
* Entries will be available 60 days
* Available in many languages

## Contributions<a name="contributions"></a>
The project is always open for contributions and accepts pull requests.
The project uses [AOSP Java Code Style](https://source.android.com/source/code-style#follow-field-naming-conventions), with one exception: private members are `_camelCase` instead of `mBigCamel`. You may use Android Studios _auto reformat feature_ before sending a PR.

Join our IRC or Matrix channel (bridged) and say hello! Don't be afraid to start talking. [![Chat - Matrix](https://img.shields.io/badge/chat-on%20matrix-blue.svg)](https://matrix.to/#/#froodyapp:matrix.org) [![Chat - FreeNode IRC](https://img.shields.io/badge/chat-on%20irc-blue.svg)](https://kiwiirc.com/client/irc.freenode.net/?nick=froodyapp-anon|?#froodyapp)

Translations can be contributed on [Crowdin](https://crowdin.com/project/froodyapp). [![Crowdin](https://d322cqt584bo4o.cloudfront.net/froodyapp/localized.svg)](https://crowdin.com/project/froodyapp)


* Share the app and tell other people about it
* Tell things that could be improvedp
* Supply artwork or vector icons for the different types of entries


## Permissions & Privacy<a name="privacy"></a>
* The app requests your location so you can create entries and automatically zoom to your location
* If you add a new entry the location gets
  * Reverse geocoded via OpenStreetMap Nominatim
  * Stored on the setted server - together with other input data (visible in Publish tab)
* The connection to the default server makes use of SSL/TLS
* Others can find your created entries if they are using the same server
* You can delete your entries by tapping the trash icon or ask for removal by email
  * Entries will get automatically get removed after 60 days unless deleted

## Notices
The default `froody-server` instance is hosted by [gsantner](https://gsantner.github.io/).
The default server domain `froody-app.at` is sponsored by [Robert Diesenreither](http://www.zero-emission.at/index.php).
The app uses OpenStreetMap for displaying maps.

### License
Froody is released under Apache 2.0 LICENSE (see [LICENCE](https://github.com/froodyapp/froody-android/blob/master/LICENSE.txt)).  
Localization files and ressources (strings-\*.xml) are licensed CC0 1.0.

## Screenshots

<table>
  <tr>
    <td> <img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/01.png" alt="Map view"/> </td>
    <td> <img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/07.png" alt="Detail view"/> </td>
  </tr><tr>
    <td> <img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/02.png" alt="Publish view"/> </td>
    <td> <img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/04.png" alt="More view" /> </td>
  </tr>
</table>
