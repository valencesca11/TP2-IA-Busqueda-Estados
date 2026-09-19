import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Main {

    // Configuracion del espacio de estados
    static final int POSICION_B = 0;
    static final int LIMITE_IZQUIERDO = -10;
    static final int LIMITE_DERECHO = 10;
    static final int DELTA_H = 1;

    /*
     * Esta clase simula el entorno del robot. El objetivo se guarda como un
     * dato privado para que el algoritmo no conozca directamente donde esta A.
     */
    static class EntornoMotor {
        private final int posicionA;

        EntornoMotor(int posicionA) {
            if (posicionA < LIMITE_IZQUIERDO
                    || posicionA > LIMITE_DERECHO) {
                throw new IllegalArgumentException(
                        "La posicion A esta fuera del rango permitido.");
            }

            this.posicionA = posicionA;
        }

        boolean esObjetivo(int posicion) {
            return posicion == posicionA;
        }

        boolean esPosicionValida(int posicion) {
            return posicion >= LIMITE_IZQUIERDO
                    && posicion <= LIMITE_DERECHO;
        }
    }

    /*
     * Guarda los resultados obtenidos durante la busqueda.
     */
    static class ResultadoBusqueda {
        boolean encontrado;
        List<Integer> estadosExplorados;
        List<Integer> caminoSolucion;

        ResultadoBusqueda(
                boolean encontrado,
                List<Integer> estadosExplorados,
                List<Integer> caminoSolucion) {

            this.encontrado = encontrado;
            this.estadosExplorados = estadosExplorados;
            this.caminoSolucion = caminoSolucion;
        }
    }

    /*
     * Implementacion de la busqueda primero en anchura.
     */
    static ResultadoBusqueda buscarEnAnchura(EntornoMotor entorno) {

        Queue<Integer> abiertos = new ArrayDeque<>();
        Set<Integer> visitados = new HashSet<>();
        Map<Integer, Integer> antecesores = new HashMap<>();
        List<Integer> explorados = new ArrayList<>();

        // Se agrega el estado inicial.
        abiertos.add(POSICION_B);
        visitados.add(POSICION_B);
        antecesores.put(POSICION_B, null);

        while (!abiertos.isEmpty()) {

            int posicionActual = abiertos.remove();
            explorados.add(posicionActual);

            System.out.println(
                    "Se examina: " + mostrarPosicion(posicionActual));

            /*
             * El robot palpa la superficie para comprobar
             * si encontro el punto A.
             */
            if (entorno.esObjetivo(posicionActual)) {

                List<Integer> camino = reconstruirCamino(
                        posicionActual,
                        antecesores
                );

                return new ResultadoBusqueda(
                        true,
                        explorados,
                        camino
                );
            }

            /*
             * Los operadores posibles son avanzar un paso
             * hacia la derecha o hacia la izquierda.
             */
            int[] sucesores = {
                    posicionActual + DELTA_H,
                    posicionActual - DELTA_H
            };

            for (int sucesor : sucesores) {

                if (entorno.esPosicionValida(sucesor)
                        && !visitados.contains(sucesor)) {

                    abiertos.add(sucesor);
                    visitados.add(sucesor);
                    antecesores.put(sucesor, posicionActual);
                }
            }
        }

        /*
         * Si la cola queda vacia, no se encontro el objetivo
         * dentro del espacio permitido.
         */
        return new ResultadoBusqueda(
                false,
                explorados,
                Collections.emptyList()
        );
    }

    /*
     * Reconstruye el camino desde A hasta B utilizando
     * los antecesores almacenados.
     */
    static List<Integer> reconstruirCamino(
            int objetivo,
            Map<Integer, Integer> antecesores) {

        List<Integer> camino = new ArrayList<>();
        Integer posicion = objetivo;

        while (posicion != null) {
            camino.add(posicion);
            posicion = antecesores.get(posicion);
        }

        Collections.reverse(camino);
        return camino;
    }

    /*
     * Calcula la distancia recorrida si el brazo examina
     * fisicamente los estados en el orden de BFS.
     */
    static int calcularDistanciaExploracion(
            List<Integer> explorados) {

        int distancia = 0;

        for (int i = 1; i < explorados.size(); i++) {
            distancia += Math.abs(
                    explorados.get(i)
                            - explorados.get(i - 1)
            );
        }

        return distancia;
    }

    /*
     * Convierte una posicion numerica en un texto comprensible.
     */
    static String mostrarPosicion(int posicion) {

        if (posicion == POSICION_B) {
            return "B (0 cm)";
        }

        String direccion =
                posicion > 0 ? "derecha" : "izquierda";

        return String.format(
                "%+d cm (%s)",
                posicion,
                direccion
        );
    }

    /*
     * Prepara una lista de posiciones para mostrarla
     * en forma de recorrido.
     */
    static String mostrarRecorrido(
            List<Integer> posiciones) {

        List<String> resultado = new ArrayList<>();

        for (int posicion : posiciones) {
            resultado.add(mostrarPosicion(posicion));
        }

        return String.join(" -> ", resultado);
    }

    public static void main(String[] args) {

        /*
         * Para el ejemplo, A se encuentra cuatro centimetros
         * hacia la izquierda de B.
         */
        EntornoMotor entorno = new EntornoMotor(-4);

        System.out.println(
                "BUSQUEDA EXHAUSTIVA PRIMERO EN ANCHURA"
        );

        System.out.println(
                "Posicion inicial: B (0 cm)"
        );

        System.out.println(
                "Limites: -10 cm y +10 cm"
        );

        System.out.println(
                "Incremento Delta H: 1 cm\n"
        );

        ResultadoBusqueda resultado =
                buscarEnAnchura(entorno);

        System.out.println("\nRESULTADO FINAL");

        if (resultado.encontrado) {

            int movimientos =
                    resultado.caminoSolucion.size() - 1;

            int costoCamino =
                    movimientos * DELTA_H;

            int distanciaExploracion =
                    calcularDistanciaExploracion(
                            resultado.estadosExplorados
                    );

            System.out.println(
                    "Se encontro el punto de montaje A."
            );

            System.out.println(
                    "Cantidad de estados examinados: "
                            + resultado.estadosExplorados.size()
            );

            System.out.println(
                    "Orden de exploracion:"
            );

            System.out.println(
                    mostrarRecorrido(
                            resultado.estadosExplorados
                    )
            );

            System.out.println(
                    "Camino solucion:"
            );

            System.out.println(
                    mostrarRecorrido(
                            resultado.caminoSolucion
                    )
            );

            System.out.println(
                    "Movimientos del camino solucion: "
                            + movimientos
            );

            System.out.println(
                    "Costo del camino solucion: "
                            + costoCamino + " cm"
            );

            System.out.println(
                    "Distancia recorrida durante la exploracion: "
                            + distanciaExploracion + " cm"
            );

        } else {

            System.out.println(
                    "No se encontro A dentro del espacio de estados."
            );

            System.out.println(
                    "Cantidad de estados examinados: "
                            + resultado.estadosExplorados.size()
            );
        }
    }
}
