# SimpleOSGrid

A trivially-simple UK OS grid reference app for Android, that 
does not rely on any Google services.

Version 0.1a, August 2023

## Why?

There are many grid reference apps on the Google Play store, some free.
However, I run Lineage OS on my cellphone, with no support for Google
infrastructure. Even the free grid reference apps seem to need Google Play
services. Partly, I guess, that's so they can connect to Google's advertising
infrastructure. However, there is a specific location API that uses Google Play
services, and I suppose that's what these apps are using. Google certainly
encourages the use of the Google Play location API over the stock Android API.
Whatever the explanation, not one of these apps works on my de-googled Samsung
phone. 

So I wrote my own. It does only one thing, which is to display the GPS
location in UK OS grid format. The intention is for me to be able to locate
myself on an OS paper map, so the location is only displayed as a six-figure
reference. This grid reference provides a spatial resolution of 100m; GPS 
is usually more precise than that, but the paper maps used by outdoor
sports enthusiasts are not. 

This app is, and will remain, free and open-source. It does not (indeed, can 
not) display any kind of advertising. It's never going to be on the Google
Play store, because it's designed to run on a de-googled device. Ideally,
you'd build it from source, but there is an APK in this repository. If you're
running a de-googled device, you'll know how to install an APK. If you're
running regular Android, there are plenty of better apps to choose from 
anyway.

## Usage

The app has no significant user interface. When started, it will be disabled.
Click the 'Active' button to enable it and display the grid reference from 
GPS. Click 'Active' again to turn GPS updates off. Note that activating
this app does not enable location services on the device -- you'll need 
to do that as well. 

This app is designed to return a single reading, which can be referred to a 
paper map. There's no purpose in leaving GPS updates running in the app 
-- this will only drain the battery.

The fact that location services are turned off on the device won't stop the app
running, but no location will be displayed. The app warns if the Active button
is clicked and, in fact, location is turned off.

The time it takes to get a reading depends on many factors. You probably won't
get a reading at all if you're indoors, and it will be inaccurate if you're
under thick tree cover. The app displays an estimate of the accuracy, along with
the time of the last update. 

## Building from source

You'll need the Android SDK.

Create a file `local.properties` that contains a line that indicates where the
Android SDK is installed. For example:

    sdk.dir=/home/kevin/lib/android_sdk

Then:

    ./gradlew clean build

The output is an apk file 

    ./app/build/outputs/apk/release/app-release-unsigned.apk

You can install this in any of the ways that work for installing APK files.

## Compatibility

This app was originally designed for Android 6. Although I now build it with 
a modern version of the Android SDK, it uses no features later than Android
7 (but see 'Permissions issues' below). I no longer have any old devices to 
test on and, to be frank, I wrote this app specifically for my own purposes.
I would expect it to work -- to some extent -- on most Android devices that
are still available, but I can't guarantee this. 

## Permissions issues

SimpleOSGrid needs "fine location" permission. In the Android versions this app
was originally designed for, the user got prompted to grant permissions when
installing an app. With more recent versions, apps are supposed to direct users
to the relevant settings page. 

So on Android versions 7 and later, SimpleOSGrid will check for permissions, and
raise the 'grant permissions' screen if necessary. The user can deny permissions, 
of course,
and that won't stop the app running -- it will just display an error message.

You might need to click the "Active" button again, the first time you grant
permissions, because the grant takes a little time.

The app will check permissions every time you click the Active button, because
some Android devices quietly withdraw permissions when apps are not used for
a while.

## Notes

So long as the app is not in the 'active' state (and it is displaying
'GPS is stopped'), it should not draw from the battery. From Android 11
(I think) apps need special permissions to use GPS when they are not 
displayed. SimpleOSGrid does not ask for those permissions, and so
it should not use the battery when it is not displayed, whether the app
is in the 'active' state or not.

With Android 10 and earlier, apps are allowed to receive background GPS updates
even when they are not displayed, and without any additional permissions.
Consequently, this app might draw from the battery when it is invisible
in these Android versions. That's why it has a prominent 'Active' button -- 
the app should usually be inactivated when it isn't actually retrieving
a grid reference.

With a clear view of the sky, it usually takes about thirty seconds on
my device for GPS to reach maximum accuracy. If I've travelled a long
way with with the GPS location service turned off, it may take a lot longer
than this. 

It should, I hope, be obvious that this app won't work outside the UK.
It probably does not fail gracefully if the location is outside the
range of the UK OS grid system.

## Legal and author

SimpleOSGrid is copyright (c)2023 Kevin Boone. It makes use of the JCoord
coordinate conversion library, which is (c)2006 Jonathan Stott. Both are
released under the terms of the GNU Public Licence. There is, of course,
no warranty of any kind.

It should go without saying that you shouldn't be out in the hills with only
a cellphone for navigation. Please, please don't rely on this app as your
only means of finding your location -- the mountain rescue services are 
overworked enough already.


