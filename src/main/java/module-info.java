module JitWarmUp {
    requires jdk.jfr;
    requires java.management;
    requires java.instrument;

    exports chiroito.sample;
}