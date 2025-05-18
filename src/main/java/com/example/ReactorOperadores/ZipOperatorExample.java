package com.example.ReactorOperadores;

import reactor.core.publisher.Flux;

public class ZipOperatorExample {
    public static void main(String[] args) {
        Flux<Integer> flux1 = Flux.just(1, 2, 3);
        Flux<String> flux2 = Flux.just("A", "B", "C");

        Flux.zip(flux1, flux2, (num, letter) -> num + letter)
                .subscribe(System.out::println);
    }
}