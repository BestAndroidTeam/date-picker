# Date Picker (with week numbers)


<img src="https://raw.githubusercontent.com/bestandroidteam/date-picker/master/art/main_screenshot.jpg" width="500" />

---

### Gradle Dependency

In your root build.gradle
```gradle
allprojects {
    repositories {
        ...
        mavenCentral()
        maven { url "https://jitpack.io" }
    }
}
```

In your module build.gradle
```gradle
dependencies {
  ...
  implementation 'com.github.bestandroidteam:date-picker:v1.4'
}
```

---

### Why?

Android includes a stock `DatePicker` in its framework, however this widget is very stubborn. It 
does not adapt to different view widths, making it difficult to use in modern UI. This library 
solves for that by creating a custom implementation, written completely in Kotlin.

---

### Basics

It's simple, just add a `DatePicker` to your layout (with the fully qualified package name):

```xml
<com.afollestad.date.DatePicker
    android:id="@+id/datePicker"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    />
```

There are a few basic getters and setters:

```kotlin
val datePicker: DatePicker = // ...

val selectedDate: Calendar? = datePicker.getDate()

datePicker.setDate(
    year = 2019,
    month = Calendar.JUNE,
    selectedDate = 17
)
datePicker.setDate(Calendar.getInstance())
```

---

### Styling

You can configure basic theme properties from your layout:

```xml
<com.afollestad.date.DatePicker
    xmlns:app="http://schemas.android.com/apk/res-auto"
    ...
    app:date_picker_selection_color="?colorAccent"
    app:date_picker_header_background_color="?colorAccent"
    app:date_picker_medium_font="@font/some_medium_font"
    app:date_picker_normal_font="@font/some_normal_font"
    app:date_picker_disabled_background_color="@color/disabled_color"
    app:date_picker_selection_vibrates="true"
    app:date_picker_calendar_horizontal_padding="4dp"
    />
```

*(Note that in order for date_picker_selection_vibrates=true to have an effect, your app needs to 
declare the `VIBRATE` permission in its manifest.)*

---

### Callback

```kotlin
val datePicker: DatePicker = // ...

datePicker.addOnDateChanged { previousDate, newDate->
  // this library provides convenience extensions to Calendar like month, year, and dayOfMonth too.
}

// Removes all callbacks you've added previously with addOnDateChanged(...) 
datePicker.clearOnDateChanged()
```

---

### Min and Max Dates

<img src="https://raw.githubusercontent.com/afollestad/date-picker/master/art/min_max_date.png" width="250" />

```kotlin
val datePicker: DatePicker = // ...

val minDate = datePicker.getMinDate()
datePicker.setMinDate(
  year = 2019,
  month = Calendar.JUNE,
  date = 17
)
datePicker.setMinDate(Calendar.getInstance())

val maxDate = datePicker.getMaxDate()
datePicker.setMaxDate(
  year = 2019,
  month = Calendar.JUNE,
  date = 20
)
datePicker.setMaxDate(Calendar.getInstance())
```
