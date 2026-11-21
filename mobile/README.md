# Android & iOS store apps

The Hub is one Spring Boot website that is also an installable app (PWA). This folder wraps the **live website**
in a native shell with [Capacitor](https://capacitorjs.com) so you can publish to Google Play and the App Store.
Every website update appears in the apps instantly, with no new store release.

## Prerequisites
1. Deploy the Spring Boot app to a public **HTTPS** domain (required by both platforms).
2. Put that domain in `capacitor.config.json` (`server.url`) and `www/index.html`.
3. Android: Node 18+, Android Studio. iOS: a Mac with Xcode, plus an Apple Developer account (US$99/yr). Google Play needs a one-time US$25 developer account.

## Build
```
cd mobile
npm install
npx cap add android      # and/or: npx cap add ios
npx cap sync
npx cap open android     # build the APK/AAB in Android Studio
npx cap open ios         # archive and upload in Xcode
```
Use `src/main/resources/static/images/icon-512.png` for the store icons (or generate sets with `@capacitor/assets`).

## Store review note
Apple rejects apps that are only a website in a frame. Before submitting to the App Store, add native value
(e.g. push notifications via `@capacitor/push-notifications`, camera upload for worker photos via `@capacitor/camera`).
Google Play is more lenient. Both stores require a privacy policy URL.
