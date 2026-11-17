# The launcher has no reflection-based APIs, so the defaults are enough.
# Keep the Activity entry points: they are referenced from the manifest.
-keep class dev.minimaltv.launcher.MainActivity { *; }
-keep class dev.minimaltv.launcher.SettingsActivity { *; }
-keep class dev.minimaltv.launcher.FeaturedPickerActivity { *; }
