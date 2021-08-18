package com.digitalskies.postingapp.utils;

public interface OnEventChanged<T> {

    void onUnhandledContent(T data);
}
