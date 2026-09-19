import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Main {

    static final int POSICION_B = 0;
    static final int LIMITE_IZQUIERDO = -10;
    static final int LIMITE_DERECHO = 10;
    static final int DELTA_H = 1;

    // Simula el entorno y las mediciones sobre el block
    static class EntornoMotor {

        private final int posicionA;

        EntornoMotor(int posicionA) {

            if (posicionA < LIMITE_IZQUIERDO
                    || posicionA > LIMITE_DERECHO) {

                throw new IllegalArgumentException(
                        "La posicion A esta fuera del rango permitido."
                );
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

        /*
         * Representa la estimacion obtenida al comparar
         * el relieve medido con el relieve de referencia.
         */
        int estimarDistancia(int posicion) {
            return Math.abs(posicionA - posicion);
        }
    }

    // Representa cada estado del espacio de busqueda
    static class Nodo {

        int posicion;
        int g;
        int h;
        int f;
        Nodo anterior;

        Nodo(
                int posicion,
                int g,
                int h,
                Nodo anterior) {

            this.posicion = posicion;
            this.g = g;
            this.h = h;
            this.f = g + h;
            this.anterior = anterior;
        }
    }

    // Guarda los resultados de la busqueda
    static class ResultadoBusqueda {

        boolean encontrado;
        List<Nodo> estadosExplorados;
        List<Integer> caminoSolucion;

        ResultadoBusqueda(
                boolean encontrado,
                List<Nodo> estadosExplorados,
                List<Integer> caminoSolucion) {

            this.encontrado = encontrado;
            this.estadosExplorados = estadosExplorados;
            this.caminoSolucion = caminoSolucion;
        }
    }

    // Implementacion del algoritmo A*
    static ResultadoBusqueda buscarConAEstrella(
            EntornoMotor entorno) {

        /*
         * La cola ordena los nodos primero por f(n)
         * y luego por h(n).
         */
        PriorityQueue<Nodo> abiertos =
                new PriorityQueue<>(
                        Comparator
                                .comparingInt(
                                        (Nodo nodo) -> nodo.f
                                )
                                .thenComparingInt(
                                        nodo -> nodo.h
                                )
                );

        Set<Integer> cerrados = new HashSet<>();
        Map<Integer, Integer> mejorCosto = new HashMap<>();
        List<Nodo> explorados = new ArrayList<>();

        Nodo inicial = new Nodo(
                POSICION_B,
                0,
                entorno.estimarDistancia(POSICION_B),
                null
        );

        abiertos.add(inicial);
        mejorCosto.put(POSICION_B, 0);

        System.out.printf(
                "%-12s %-5s %-5s %-5s%n",
                "Posicion",
                "g(n)",
                "h(n)",
                "f(n)"
        );

        while (!abiertos.isEmpty()) {

            Nodo actual = abiertos.remove();

            if (cerrados.contains(actual.posicion)) {
                continue;
            }

            cerrados.add(actual.posicion);
            explorados.add(actual);

            System.out.printf(
                    "%-12s %-5d %-5d %-5d%n",
                    mostrarPosicion(actual.posicion),
                    actual.g,
                    actual.h,
                    actual.f
            );

            // Se comprueba si se encontro A
            if (entorno.esObjetivo(actual.posicion)) {

                return new ResultadoBusqueda(
                        true,
                        explorados,
                        reconstruirCamino(actual)
                );
            }

            // Movimientos posibles sobre la horizontal H
            int[] sucesores = {
                    actual.posicion + DELTA_H,
                    actual.posicion - DELTA_H
            };

            for (int sucesor : sucesores) {

                if (!entorno.esPosicionValida(sucesor)
                        || cerrados.contains(sucesor)) {

                    continue;
                }

                int nuevoG =
                        actual.g + DELTA_H;

                int costoRegistrado =
                        mejorCosto.getOrDefault(
                                sucesor,
                                Integer.MAX_VALUE
                        );

                /*
                 * Se agrega el estado si no fue visitado
                 * o si se encontro un camino de menor costo.
                 */
                if (nuevoG < costoRegistrado) {

                    int nuevoH =
                            entorno.estimarDistancia(sucesor);

                    Nodo nuevoNodo = new Nodo(
                            sucesor,
                            nuevoG,
                            nuevoH,
                            actual
                    );

                    mejorCosto.put(
                            sucesor,
                            nuevoG
                    );

                    abiertos.add(nuevoNodo);
                }
            }
        }

        return new ResultadoBusqueda(
                false,
                explorados,
                Collections.emptyList()
        );
    }

    // Reconstruye el camino desde B hasta A
    static List<Integer> reconstruirCamino(
            Nodo objetivo) {

        List<Integer> camino = new ArrayList<>();
        Nodo actual = objetivo;

        while (actual != null) {

            camino.add(actual.posicion);
            actual = actual.anterior;
        }

        Collections.reverse(camino);

        return camino;
    }

    static String mostrarPosicion(int posicion) {

        if (posicion == POSICION_B) {
            return "B (0)";
        }

        return String.format(
                "%+d cm",
                posicion
        );
    }

    static String mostrarRecorrido(
            List<Integer> posiciones) {

        List<String> resultado = new ArrayList<>();

        for (int posicion : posiciones) {

            resultado.add(
                    mostrarPosicion(posicion)
            );
        }

        return String.join(
                " -> ",
                resultado
        );
    }

    public static void main(String[] args) {

        /*
         * Para este ejemplo, A se encuentra
         * cuatro centimetros a la izquierda de B.
         */
        EntornoMotor entorno =
                new EntornoMotor(-4);

        System.out.println(
                "BUSQUEDA HEURISTICA A ESTRELLA"
        );

        System.out.println(
                "Posicion inicial: B (0 cm)"
        );

        System.out.println(
                "Limites: -10 cm y +10 cm"
        );

        System.out.println(
                "Incremento Delta H: 1 cm"
        );

        System.out.println(
                "Funcion de evaluacion: f(n) = g(n) + h(n)"
        );

        System.out.println();

        ResultadoBusqueda resultado =
                buscarConAEstrella(entorno);

        System.out.println();
        System.out.println("RESULTADO FINAL");

        if (resultado.encontrado) {

            int movimientos =
                    resultado.caminoSolucion.size() - 1;

            System.out.println(
                    "Se encontro el punto de montaje A."
            );

            System.out.println(
                    "Estados examinados: "
                            + resultado.estadosExplorados.size()
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
                    "Movimientos realizados: "
                            + movimientos
            );

            System.out.println(
                    "Costo total: "
                            + movimientos * DELTA_H
                            + " cm"
            );

        } else {

            System.out.println(
                    "No se encontro A dentro del espacio permitido."
            );
        }
    }
}
