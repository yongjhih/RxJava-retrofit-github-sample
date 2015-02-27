package com.github.yongjhih;

import rx.schedulers.*;
import rx.Observable;
import rx.functions.*;
import rx.observables.*;

import java.util.List;
import java.util.ArrayList;

public class Main {
    abstract static class AbsCallback<T> {
        abstract void done(List<T> list);
    }

    static class CallbackUtils {
        interface Callback<T> {
            void done(List<T> list);
        }

        static <T> AbsCallback<T> create(Callback<T> callback) {
            return new AbsCallback<T>() {
                @Override public void done(List<T> list) {
                    callback.done(list);
                }
            };
        }
    }

    public static void test(AbsCallback<String> callback) {
        callback.done(new ArrayList<>());
    }

    public static void main(String... args) {
        test(CallbackUtils.create(list -> System.out.println(list)));
    }

    /*
        this.<String>say(hello())
        this.<Integer>say(hello())
    <T> T hello() {
        return hello((T) null)
    }

    int hello(Integer selector) {
    }

    String hello(String selector) {
    }

    say(String) {
    }

    say(int) {
    }
    */
}
