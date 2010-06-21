# Ensure that Proguard will do it's thing
-forceprocessing
# We need multiple passes to remove unwanted stuff
-optimizationpasses 10

# The "Exceptions" attribute has to be preserved, so the 
# compiler knows which exceptions methods may throw. The 
# "InnerClasses" attribute (or more precisely, its source 
# name part) has to be preserved too, for any inner classes 
# that can be referenced from outside the library. The javac 
# compiler would be unable to find the inner classes otherwise.
# The "Signature" attribute is required to be able to access 
# generic types when compiling in JDK 5.0 and higher. 
# Finally, we're keeping the "Deprecated" attribute and the 
# attributes for producing useful stack traces. 
-keepattributes Exceptions,InnerClasses,Deprecated

# Stuff all the internal in one package 
-repackageclasses 'com.wayfinder.core.internal'
# Don't do stuff that may work badly on some platforms 
-useuniqueclassmembernames
-dontusemixedcaseclassnames

# Only touch stuff that's located in the *.internal.* packages 
-keep public class !**.internal.** {public protected *;}

# === EXTRA RELEASE INSTRUCTIONS === 

# Remove all invocations of Throwable.printStackTrace(). 
-assumenosideeffects public class java.lang.Throwable {
    public void printStackTrace();
}

# Remove the logging system completely 
# LogFactory 
-assumenosideeffects class com.wayfinder.core.shared.internal.debug.LogFactory {
    public static com.wayfinder.core.shared.internal.debug.Logger getDefaultLogger();
    public static com.wayfinder.core.shared.internal.debug.Logger getLoggerForClass(java.lang.Class);
    public static com.wayfinder.core.shared.internal.debug.Logger getLoggerWithLevel(com.wayfinder.pal.debug.Level);
    public static void initLogFrameWork(com.wayfinder.pal.debug.LogHandler, com.wayfinder.pal.debug.Level);
    public static void overrideLoglevelForClass(java.lang.Class, com.wayfinder.pal.debug.Level);
    static void removeClassOverride(java.lang.Class);
    static void resetLogFramework();
}

# Logger classes 
# remove from public class, the package protected subclasses 
# will be redundant 
-assumenosideeffects class com.wayfinder.core.shared.internal.debug.Logger {
    public void com.wayfinder.core.shared.internal.debug.Logger.debug(java.lang.String, java.lang.String);
    public void com.wayfinder.core.shared.internal.debug.Logger.error(java.lang.String, java.lang.String);
    public void com.wayfinder.core.shared.internal.debug.Logger.fatal(java.lang.String, java.lang.String);
    public void com.wayfinder.core.shared.internal.debug.Logger.info(java.lang.String, java.lang.String);
    public void com.wayfinder.core.shared.internal.debug.Logger.logException(com.wayfinder.pal.debug.Level, java.lang.String, java.lang.Throwable);
    public void com.wayfinder.core.shared.internal.debug.Logger.trace(java.lang.String, java.lang.String);
    public void com.wayfinder.core.shared.internal.debug.Logger.warn(java.lang.String, java.lang.String);
    public com.wayfinder.pal.debug.Level com.wayfinder.core.shared.internal.debug.Logger.getLogLevel();
    public boolean com.wayfinder.core.shared.internal.debug.Logger.isDebug();
    public boolean com.wayfinder.core.shared.internal.debug.Logger.isError();
    public boolean com.wayfinder.core.shared.internal.debug.Logger.isFatal();
    public boolean com.wayfinder.core.shared.internal.debug.Logger.isInfo();
    public boolean com.wayfinder.core.shared.internal.debug.Logger.isLoggable(com.wayfinder.pal.debug.Level);
    public boolean com.wayfinder.core.shared.internal.debug.Logger.isTrace();
    public boolean com.wayfinder.core.shared.internal.debug.Logger.isWarn();
}
