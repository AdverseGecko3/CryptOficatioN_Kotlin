# CryptOficatioN_Kotlin

<p align="center" width="100%">
    <img width="33%" src="https://github.com/AdverseGecko3/CryptOficatioN_Kotlin/blob/master/app/src/main/res/drawable-v24/cryptofication_logo_long_white_splash.png">
</p>

This is the Kotlin version of CryptOficatioN, an app that uses CoinGecko API to track the top cryptos!

In this you will be able to check the price, price change percentage in 24h, 7 days sparkline, etc!



But now, let me introduce you. I'm Eric Barrero, and this app is a sample of my knowladge to date. In this app you can find:

**- 100% Kotlin code:** The previous version of CryptOficatioN was 100% Java, but I wanted to learn Kotlin as it is the new Recommended language by Google.

**- MVVM Architecture:** I started to learn clean architecture, and MVVM was my choice.

**- Room:** In order to save favorite cryptos, Room local database is perfect.

**- Coroutines:** To make Asynchronous processes such as doing tasks in the background, making an API call, or getting values in Room, coroutines are the way to go.

**- ViewBinding:** For a better code comprehension, and also a cleaner one, ViewBinding is used to get the views from the XML file.

**- API communication:** This app uses CoinGecko Free API, and thanks to Retrofit, the API communicates to this app via JSON.

**- Splash Screen:** When app starts, a Splash Screen appears (made with layer-list).

**- RecyclerView:** To ensure that the app is efficient when displaying the cryptos, the dynamic list RecyclerView is perfect.

**- Shared Preferences:** As the user can change the currency, the default filters of the crypto list or switch between light mode and dark mode in a Settings fragment, SharedPreferences are perfect to save those values.

**- ItemTouchHelper:** As gestures are common nowadays, this app implements gestures to manipulate the list, like adding/deleting a crypto to favorites.

**- MPAndroidChart:** To give the user a grahic representation of the last 7 days price of a crypto, MPAndroidChart is the perfect library to display the graphs.

**- Glide:** To display crypto icons, Glide is used to load the images URLs provided by CoinGecko API.

**- XML design:** CryptOficatioN uses ConstraintLayout, to ensure that the UI is fully responsive.

**- Notifications:** The user can set the time when he wants to receive the notifications. At that hour a service will call the API, format the data, and send a notification.

**- Monetization:** This app implements Google AdMob, in order to monetize the app.

Currently working on:

**- Testing:** Currently working on the implementation of tests.
