public class Deps {
    private static final String SUPPORT_LIB_VERSION = "1.0.0";
    private static final String RETROFIT_VERSION = "2.5.1-SNAPSHOT";
    private static final String KOTLIN_COROUTINES_VERSION = "1.1.1";
    private static final String KOIN_VERSION = "2.0.0-rc-2";
    public static final String KOTLIN_VERSION = "1.3.31";

    public static final String kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:" + KOTLIN_VERSION;
    public static final String kotlinCoroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:" + KOTLIN_COROUTINES_VERSION;
    public static final String kotlinCoroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:" + KOTLIN_COROUTINES_VERSION;
    public static final String kotlinSerialization = "org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.10.0";

    public static final String supportAppCompat = "androidx.appcompat:appcompat:" + SUPPORT_LIB_VERSION;
    public static final String supportAnnotation = "androidx.annotation:annotation:" + SUPPORT_LIB_VERSION;
    public static final String supportDesign = "com.google.android.material:material:" + SUPPORT_LIB_VERSION;
    public static final String cardView = "androidx.cardview:cardview:" + SUPPORT_LIB_VERSION;
    public static final String recyclerView = "androidx.recyclerview:recyclerview:1.0.0";
    public static final String constraintLayout = "androidx.constraintlayout:constraintlayout:1.1.2";
    public static final String lifecycleLibrary = "androidx.lifecycle:lifecycle-extensions:2.0.0-rc01";
    public static final String archLifecycleCompiler = "androidx.lifecycle:lifecycle-compiler:2.0.0-rc01";

    public static final String cicerone = "ru.terrakok.cicerone:cicerone:5.0.0";

    public static final String retrofit = "com.squareup.retrofit2:retrofit:" + RETROFIT_VERSION;
    public static final String retrofitGsonConverter = "com.squareup.retrofit2:converter-gson:" + RETROFIT_VERSION;
    public static final String retrofitSerialization = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.3.0";
    public static final String retrofitLogging = "com.squareup.okhttp3:logging-interceptor:3.6.0";

    public static final String rxJava = "io.reactivex.rxjava2:rxjava:2.1.14";
    public static final String rxAndroid = "io.reactivex.rxjava2:rxandroid:2.1.0";
    public static final String rxBinding = "com.jakewharton.rxbinding2:rxbinding:2.1.1";
    public static final String rxValidator = "com.github.whalemare:RxValidator2:1.3";

    public static final String glide = "com.github.bumptech.glide:glide:4.8.0";
    public static final String glideProcessor = "com.github.bumptech.glide:compiler:4.8.0";
    public static final String glideRecyclerViewIntegration = "com.github.bumptech.glide:recyclerview-integration:4.8.0";
    public static final String glideOkHttpIntegration = "com.github.bumptech.glide:okhttp3-integration:4.8.0";

    public static final String timber = "com.jakewharton.timber:timber:4.7.1";

    public static final String koin = "org.koin:koin-android:" + KOIN_VERSION;
    public static final String koinScope = "org.koin:koin-androidx-scope:" + KOIN_VERSION;
    public static final String koinViewModel = "org.koin:koin-androidx-viewmodel:" + KOIN_VERSION;

    public static final String realmFieldNamesGeneration = "dk.ilios:realmfieldnameshelper:1.1.1";

    public static final String mockServer = "com.squareup.okhttp3:mockwebserver:3.13.1";

    public static final String junit = "junit:junit:4.12";
    public static final String espressoCore = "com.android.support.test.espresso:espresso-core:3.0.2";
    public static final String runner = "com.android.support.test:runner:1.0.2";
}
