package com.example.RxJavaOperadores;

import io.reactivex.Observable;

public class MergeOperatorExample {
    public static void main(String[] args) {
        Observable<Integer> observable1 = Observable.just(1, 3, 5);
        Observable<Integer> observable2 = Observable.just(2, 4, 6);

        Observable.merge(observable1, observable2)
                .subscribe(System.out::println);
    }
}