# Ensure that Proguard will do it's thing
-forceprocessing
# We need multiple passes to remove unwanted stuff
-optimizationpasses 10

# Keep attributes to be able to obtain stacktraces
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,
                SourceFile,LineNumberTable,EnclosingMethod

# Stuff all the internal in one package
-repackageclasses 'com.wayfinder.core.internal'

# Don't do stuff that may work badly on some platforms
-useuniqueclassmembernames
-dontusemixedcaseclassnames

# Only touch stuff that's located in the *.internal.* packages
-keep public class !**.internal.** {public protected *;}
