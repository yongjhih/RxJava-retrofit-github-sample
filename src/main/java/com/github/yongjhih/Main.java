package com.github.yongjhih;

import rx.schedulers.*;
import rx.Observable;
import rx.functions.*;
import rx.observables.*;
import rx.util.*;
import rx.internal.operators.*;

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

    interface GitHub {
        @GET("/repos/{owner}/{repo}/contributors")
        //List<Contributor> contributors(
        Observable<List<Contributor>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);
    }

    public static void main(String... args) {
        // Create a very simple REST adapter which points the GitHub API endpoint.
        RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("https://api.github.com")
            .build();

        // Create an instance of our GitHub API interface.
        GitHub github = restAdapter.create(GitHub.class);

        // Fetch and print a list of the contributors to this library.
        /*
        List<Contributor> contributors = github.contributors("ReactiveX", "RxJava");
        for (Contributor contributor : contributors) {
            System.out.println(contributor.login + "\t" + contributor.contributions);
        }
        */
        github.contributors("ReactiveX", "RxJava")
            .flatMap(list -> Observable.from(list))
            .forEach(c -> System.out.println(c.login + "\t" + c.contributions));
    }
}
