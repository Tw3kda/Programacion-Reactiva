package com.example;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Implementacion {
    public static void main(String[] args) {

        // Lista de candidatos que participarán en la votación
        List<String> candidatos = Arrays.asList("Ana", "Luis", "Carlos");

        // Inicializa un mapa con los nombres de los candidatos en mayúscula y votos en 0
        Map<String, Integer> conteoInicial = new HashMap<>();
        for (String c : candidatos) {
            conteoInicial.put(c.toUpperCase(), 0);
        }

        // Variables atómicas para mantener el conteo actualizado de forma segura entre hilos
        AtomicReference<Map<String, Integer>> conteoFinal = new AtomicReference<>(new HashMap<>(conteoInicial));
        AtomicInteger totalVotos = new AtomicInteger(0);

        // === FLUJO DE VOTOS SIMULADOS ===
        Observable<String> votosStream = Observable
                // Genera un evento cada 500ms
                .interval(500, TimeUnit.MILLISECONDS)
                // Genera un nombre aleatorio o un voto inválido ("NULO") con probabilidad del 10%
                .map(i -> {
                    if (new Random().nextInt(10) == 0) return "NULO";
                    return candidatos.get(new Random().nextInt(candidatos.size()));
                })
                // Filtra los votos inválidos
                .filter(nombre -> candidatos.contains(nombre))
                // Convierte los nombres a mayúsculas para coincidir con el mapa
                .map(String::toUpperCase)
                // Toma solo los primeros 40 votos válidos
                .take(40)
                // Combina cada nombre con una marca de tiempo formateada
                .zipWith(Observable.interval(500, TimeUnit.MILLISECONDS),
                        (nombre, i) -> "[" + LocalTime.now().withNano(0) + "] Voto: " + nombre
                );

        // === OBSERVADOR QUE MUESTRA AL LÍDER CADA 5 SEGUNDOS ===
        Observable.interval(5, 5, TimeUnit.SECONDS)
            .observeOn(Schedulers.computation()) // Usa un scheduler adecuado para cómputo
            .subscribe(i -> {
                Map<String, Integer> conteo = conteoFinal.get(); // Obtiene el conteo actual
                Map.Entry<String, Integer> liderEntry = conteo.entrySet().stream()
                        .max(Map.Entry.comparingByValue()) // Busca al candidato con más votos
                        .orElse(null);

                // Imprime al líder actual, si existe
                if (liderEntry != null) {
                    String lider = liderEntry.getKey();
                    int votos = liderEntry.getValue();
                    System.out.println("\n [" + LocalTime.now().withNano(0) + "] Líder actual: " + lider + " con: " + votos + " votos");
                } else {
                    System.out.println("\n [" + LocalTime.now().withNano(0) + "] Líder actual: Nadie");
                }
            });

        // === PROCESAMIENTO DEL FLUJO DE VOTOS ===
        votosStream
                // Acumula el conteo de votos por nombre (cada nombre aparece en la línea del voto)
                .scan(conteoInicial, (conteo, linea) -> {
                    String[] partes = linea.split(" "); // Divide el string para extraer el nombre
                    String nombre = partes[partes.length - 1]; // El nombre está al final de la línea

                    // Incrementa el conteo del candidato correspondiente
                    conteo.put(nombre, conteo.getOrDefault(nombre, 0) + 1);
                    totalVotos.incrementAndGet(); // Incrementa el total de votos válidos

                    // Actualiza la referencia al conteo actual (copiando el mapa)
                    conteoFinal.set(new HashMap<>(conteo));

                    // Muestra el voto procesado
                    System.out.println(linea);

                    return new HashMap<>(conteo); // Devuelve el nuevo conteo
                })
                // Al finalizar el procesamiento de los 40 votos válidos:
                .doOnComplete(() -> {
                    System.out.println("\n Resultados finales:");
                    // Muestra los votos por cada candidato
                    conteoFinal.get().forEach((nombre, cantidad) ->
                            System.out.println("🗳 " + nombre + ": " + cantidad + " votos"));

                    // Muestra el total de votos válidos recibidos
                    System.out.println(" Total de votos válidos: " + totalVotos.get());
                })
                // Bloquea el hilo principal hasta que termine todo el proceso de votación
                .blockingSubscribe();
    }
}
