package com.secmes.secmessaging;

public class JNIWrapper {
    // Used to load the 'secmessaging' library on application startup.
    static {
        System.loadLibrary("secmessaging");
    }
    /**
     * A native method that is implemented by the 'secmessaging' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
