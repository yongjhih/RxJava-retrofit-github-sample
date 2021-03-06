package com.github.yongjhih;

import rx.schedulers.*;
import rx.Observable;
import rx.functions.*;
import rx.observables.*;

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
    }
}
