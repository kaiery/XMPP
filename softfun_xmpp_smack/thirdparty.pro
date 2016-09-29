-keep class com.parse.*{ *; }
-dontwarn com.parse.**
-dontwarn com.squareup.picasso.**

#butterknife
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * {
    @butterknife.* <fields>;
}
-keepclasseswithmembernames class * {
    @butterknife.* <methods>;
}

#Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }

#photoview
-keep class uk.co.senab.photoview.** { *; }

#org.apache.mina
-keep class org.apache.** { *; }
-dontwarn org.apache.**
-keepattributes
-keep class com.squareup.okhttp.** { *; }
-keep class com.squareup.** { *; }
-dontwarn okio.**


-keep class com.nostra13.** { *; }

-keep class com.nineoldandroids.** { *; }

-keep class com.soundcloud.** { *; }

-keep class com.opentok.** { *; }

-keep class org.igniterealtime.** { *; }
-keep class org.jivesoftware.** { *; }

-keep class com.aigestudio.wheelpicker.** { *; }

-keep class cn.edu.zafu.coreprogress.** { *; }

-keep class me.nereo.multi_image_selector.** { *; }

-keep class fr.castorflex.** { *; }

#RTC
-keep class org.webrtc.** {*;}
-keep class de.tavendo.autobahn.** {*;}