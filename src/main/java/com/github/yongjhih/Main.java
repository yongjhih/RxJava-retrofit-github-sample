package com.github.yongjhih;

import rx.schedulers.*;
import rx.Observable;
import rx.functions.*;
import rx.observables.*;
import rx.subjects.*;

import java.util.List;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * ref. https://github.com/square/retrofit/blob/master/samples/github-client/src/main/java/com/example/retrofit/GitHubClient.java
 */
public class Main {
    static class Contributor {
        String login;
        int contributions;
    }

    static class User {
        String name;
    }

    static class Repo {
        String full_name;
    }

    interface GitHub {
        @GET("/repos/{owner}/{repo}/contributors")
        //List<Contributor> contributors(
        Observable<List<Contributor>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);

        @GET("/users/{user}")
        Observable<User> user(
            @Path("user") String user);

        @GET("/users/{user}/starred")
        Observable<List<Repo>> starred(
            @Path("user") String user);
    }

    public static void main(String... args) {
        String token = (args.length > 1) ? args[1] : null;
        System.out.println("token: " + token);
        // Create a very simple REST adapter which points the GitHub API endpoint.
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("https://api.github.com")
            .setRequestInterceptor(request -> {
                if (token != null && !"".equals(token)) {
                    // https://developer.github.com/v3/#authentication
                    request.addHeader("Authorization", "token " + token);
                }
            })
            .build();

        // Create an instance of our GitHub API interface.
        GitHub github = restAdapter.create(GitHub.class);

        // Fetch and print a list of the contributors to this library.
        /* 1.
        github.contributors("ReactiveX", "RxJava")
            .flatMap(list -> Observable.from(list))
            .forEach(c -> System.out.println(c.login + "\t" + c.contributions));
        */

        /* 2.
        github.contributors("ReactiveX", "RxJava")
            .flatMap(list -> Observable.from(list))
            .flatMap(c -> github.user(c.login))
            .filter(user -> user.name != null)
            .forEach(user -> System.out.println(user.name));
        */

        /*
        github.contributors("ReactiveX", "RxJava")
            .flatMap(list -> Observable.from(list))
            .flatMap(c -> github.starred(c.login))
            .flatMap(list -> Observable.from(list))
            .filter(r -> !r.full_name.startsWith("ReactiveX"))
            .groupBy(r -> r.full_name)
            .flatMap(g -> g.count().map(c -> c + "\t" + g.getKey()))
            .toSortedList((a, b) -> b.compareTo(a))
            .flatMap(list -> Observable.from(list))
            .take(8)
            .forEach(System.out::println);
        */

        // Subject v.s. create()
        Observable<String> create = testObservableCreate();
        Observable<String> subject = testObservableSubject();
        create.subscribe(s -> System.out.println("sub: " + s));
        subject.subscribe(s -> System.out.println("sub: " + s));


        // Optional
        // http://sys1yagi.hatenablog.com/entry/2015/01/26/183000

        Optional<String> valueOpt;

        /*
        System.out.println("1");
        valueOpt = Optional.ofNullable("1,2,3");
        valueOpt.subscribe(value -> {
            System.out.println(value);
        });

        System.out.println("2");
        valueOpt = Optional.ofNullable(null);
        valueOpt.subscribe(value -> {
            System.out.println(value);
        });

        System.out.println("3");
        valueOpt = Optional.ofNullable("1,2,3");
        Observable<Integer> length = valueOpt.map(value -> value.split(",").length);
        length.subscribe(value -> {
            System.out.println(value);
        });

        System.out.println("4");
        valueOpt = Observable.merge(Optional.ofNullable("ruby123"), Optional.ofNullable("javaruby"), Optional.ofNullable("rubyabc"));
        Observable<String> filtered = valueOpt.filter(value -> value.startsWith("ruby"));
        filtered.subscribe(value -> {
            System.out.println(value);
        });
        */

        //valueOpt.flatMap(value -> Optional.ofNullable(value + suffix));
        System.out.println(Optional.ofNullable("Andrew Chen").orElse("Unnamed"));
        System.out.println(Optional.ofNullable(null).orElse("Unnamed"));
        System.out.println(Optional.of("Andrew Chen").orElse("Unnamed"));
        A a = new A();
        a.b = Optional.ofNullable(new B());
        System.out.println(Optional.ofNullable(a)
                .map(aa -> "1: " + aa.value)
                .orElse("Unnamed"));
        Optional.ofNullable(a).flatMap(new Func1<A, Optional<B>>() {
            @Override public Optional<B> call(A aa) {
                return aa.b;
            }
        });
        Optional.ofNullable(a).flatMap(aa -> aa.b);
        //Optional.ofNullable(a).flatMap(aa -> aa.b).map(bb -> bb.value).orElse("Unnamed");
        System.out.println(Optional.ofNullable(a).flatMap(new Func1<A, Optional<B>>() {
            @Override public Optional<B> call(A aa) {
                System.out.println(aa.b
                    .map(bb -> "2: " + bb.value)
                    .orElse("3: Unnamed"));
                return aa.b;
            }
        }).map(new Func1<B, String>() {
            @Override public String call(B bb) {
                return bb.value;
            }
        }).orElse("4: Unnamed"));
    }

    public static class A {
        public String value = "A";
        public Optional<B> b;
        public A() {}
    }

    public static class B {
        public String value = "B";
        public B() {}
    }

    public static class Optional<T> {
        Observable<T> obs;

        public Optional(Observable<T> obs) {
            this.obs = obs;
        }

        public static <T> Optional<T> of(T value) {
            if (value == null) {
                throw new NullPointerException();
            } else {
                return new Optional<T>(Observable.just(value));
            }
        }

        public static <T> Optional<T> ofNullable(T value) {
            if (value == null) {
                return new Optional<T>(Observable.empty());
            } else {
                return new Optional<T>(Observable.just(value));
            }
        }

        public T get() {
            return obs.toBlocking().single();
        }

        public T orElse(T defaultValue) {
            return obs.defaultIfEmpty(defaultValue).toBlocking().single();
        }

        public <R> Optional<R> map(Func1<? super T, ? extends R> func) {
            //return Optional.ofNullable(func.call(get()));
            return Optional.ofNullable(func.call(get()));
        }

        /*
        Optional<A>, Optional<B>
            Optional<B> (A a) {
                return a.b;
            }
        */
        public <R> Optional<R> flatMap(Func1<? super T, ? extends Optional<? extends R>> func) {
            //return map(func).get();
            //return new Optional(obs.map(func)); // not works
            //return func.call(this);
            //               ^ Optional<T> cannot be converted to CAP#1
            //return func.call(get());
            //              ^ CAP#1 cannot be converted to Optional<R>
            //return func.call(get());
            //return new Optional(Observable.just(func.call(get())));
            return (Optional<R>) func.call((T) get());
        }
    }

    static Observable<String> testObservableCreate() {
        return Observable.create(sub -> {
            System.out.println("create");
            sub.onNext("create");
            sub.onCompleted();
        });
    }

    static Observable<String> testObservableSubject() {
        Subject<String, String> sub = ReplaySubject.create();
        Observable<String> obs = sub.asObservable();
        new Runnable() {
            @Override public void run() {
                System.out.println("subject");
                sub.onNext("subject");
                sub.onCompleted();
            }
        }.run();
        return obs;
    }
}
