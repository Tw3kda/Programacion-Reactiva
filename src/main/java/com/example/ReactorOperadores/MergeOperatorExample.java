package com.example.ReactorOperadores;

import reactor.core.publisher.Flux;

public class MergeOperatorExample {
    public static void main(String[] args) {
        Flux<Integer> flux1 = Flux.just(1, 3, 5);
        Flux<Integer> flux2 = Flux.just(2, 4, 6);

        Flux.merge(flux1, flux2)
                .subscribe(System.out::println);
    }
}