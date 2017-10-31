[![GitHub release](https://img.shields.io/github/tag/froodyapp/froody-android.svg)](https://github.com/froodyapp/froody-android/releases)
[![Donate](https://img.shields.io/badge/donate-bitcoin-orange.svg)](http://gsantner.net/#donate)
[![Donate LiberaPay](https://img.shields.io/badge/donate-liberapay-orange.svg)](https://liberapay.com/gsantner/donate)
[![CircleCI](https://circleci.com/gh/froodyapp/froody-android.svg?style=shield)](https://circleci.com/gh/froodyapp/froody-android)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/froodyapp/localized.svg)](https://crowdin.com/project/froodyapp)

# Froody
<img src="/app/src/main/ic_launcher-web.png" align="left" width="100" hspace="10" vspace="10">
Android part of the <a href="https://froodyapp.github.io/">Froody</a> project.
Share food that grows naturally. The projects idea is to share to and help other people by e.g. sharing pears, 
which would rot anyway if unused. Intended to use in the local area.

<div style="display:flex;" >
<a href="https://f-droid.org/repository/browse/?fdid=io.github.froodyapp">
    <img src="https://f-droid.org/badge/get-it-on.png" alt="Get it on F-Droid" height="80">
</a>
<a href="https://play.google.com/store/apps/details?id=io.github.froodyapp">
    <img alt="Get it on Google Play" height="80" src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png" />
</a>
</div></br>


## Description
* Lets you share (mainly naturally growing) food to others
* The main idea is to share to and help other people by e.g. sharing pears, which would rot anyway if unused
* Intended to use in the local area - zoom in to your location and look for entries
* Everyone can add entries (and delete them), no login needed
* Entries will be available 60 days
* Available in many languages

### Permissions & Privacy<a name="privacy"></a>
* The app requests your location so you can create entries and automatically zoom to your location
* If you add a new entry the location gets
  * Reverse geocoded via OpenStreetMap Nominatim
  * Stored on the setted server - together with other input data (visible in Publish tab)
* The connection to the default server makes use of SSL/TLS
* Others can find your created entries if they are using the same server
* You can delete your entries by tapping the trash icon or ask for removal by email
  * Entries will get automatically get removed after 60 days unless deleted

## Contributions
The project is always open for contributions and accepts pull requests.
The project uses [AOSP Java Code Style](https://source.android.com/source/code-style#follow-field-naming-conventions), with one exception: private members are `_camelCase` instead of `mBigCamel`. You may use Android Studios _auto reformat feature_ before sending a PR.

Translations can be contributed on [Crowdin](https://crowdin.com/project/froodyapp). [![Crowdin](https://d322cqt584bo4o.cloudfront.net/froodyapp/localized.svg)](https://crowdin.com/project/froodyapp)

* Share the app and tell other people about it
* Tell things that could be improved
* Supply artwork or vector icons for the different types of entries


If you like my work on <b>Froody</b> and many other Open Source projects, consider a small donation to my bitcoin address:
<a href="http://gsantner.net/#donate">1B9ZyYdQoY9BxMe9dRUEKaZbJWsbQqfXU5</a>


#### Resources
* Project: [Changelog](/CHANGELOG.md) | [Issues level/beginner](https://github.com/froodyapp/froody-android/issues?q=is%3Aissue+is%3Aopen+label%3Alevel%2Fbeginner) | [License](/LICENSE.txt) | [CoC](/CODE_OF_CONDUCT.md)
* App on F-Droid: [Metadata](https://gitlab.com/fdroid/fdroiddata/blob/master/metadata/io.github.froodyapp.txt) | [Page](https://f-droid.org/packages/io.github.froodyapp/) | [Wiki](https://f-droid.org/wiki/page/io.github.froodyapp) | [Build log](https://f-droid.org/wiki/page/io.github.froodyapp/lastbuild)
* Froody: [Homepage](https://froodyapp.github.io/)

Froody is released under Apache 2.0 LICENSE (see [LICENCE](https://github.com/froodyapp/froody-android/blob/master/LICENSE.txt)).  
Localization files and resources (strings-\*.xml) are licensed CC0 1.0.

## Licensing<a name="license"></a>
Project created and maintained by <a href="http://gsantner.net">Gregor Santner</a> since 2016.
The code of the app is licensed Apache 2.0. Localization files and resources (string\*.xml) are licensed CC0 1.0.
For more licensing informations, see [`3rd party licenses`](/app/src/main/res/raw/licenses_3rd_party.md).

## Screenshots
<div style="display:flex;" >
	<img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/01.png" width="19%" >
	<img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/02.png" width="19%" style="margin-left:10px;" >
	<img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/03.png" width="19%" style="margin-left:10px;" >
	<img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/04.png" width="19%" style="margin-left:10px;" >
	<img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/05.png" width="19%" style="margin-left:10px;" >
</div>

<div style="display:flex;" >
	<img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/06.png" width="19%" >
	<img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/07.png" width="19%" style="margin-left:10px;" >
	<img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/08.png" width="19%" style="margin-left:10px;" >
	<img src="https://raw.githubusercontent.com/froodyapp/froody-metadata-latest/master/en-US/phoneScreenshots/11.png" width="19%" style="margin-left:10px;" >
</div>


### Notice
The default `froody-server` instance is hosted by [gsantner](http://gsantner.net/).
The app uses OpenStreetMap for displaying maps.
