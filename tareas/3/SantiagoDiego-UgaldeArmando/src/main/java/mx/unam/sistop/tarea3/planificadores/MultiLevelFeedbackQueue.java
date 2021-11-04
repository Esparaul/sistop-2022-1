package mx.unam.sistop.tarea3.planificadores;

import mx.unam.sistop.tarea3.CargaAleatoria;
import mx.unam.sistop.tarea3.Proceso;
import mx.unam.sistop.tarea3.Resultado;

import java.util.*;

public class MultiLevelFeedbackQueue {
    private static final int MAX_COLAS = 10;

    public static Resultado simular(CargaAleatoria cargaAleatoria) {
        Map<Proceso, Integer> tiemposDeFinalizacion = new HashMap<>();
        Map<Proceso, Integer> tiemposDeEjecucionRestantes = new HashMap<>();
        for (Proceso proceso : cargaAleatoria.getProcesos())
            tiemposDeEjecucionRestantes.put(proceso, proceso.getTiempoDeEjecucion());

        // Crear lista con listas de todos los procesos que llegan en determinado tiempo.
        List<List<Proceso>> llegadas = Planificadores.obtenerListaDeLlegadas(cargaAleatoria);

        StringBuilder representacion = new StringBuilder();

        // Inicializar colas
        List<Cola> colas = new ArrayList<>();
        for (int i = 0; i < MAX_COLAS; i++) colas.add(new Cola(i + 1));

        Cola primera = colas.get(0);
        int tiempoTotal = cargaAleatoria.getTiempoTotalDeEjecucion();
        int tiempo = 0;

        // Cada unidad de tiempo se ejecuta un proceso que se encuentre en la primera cola no vacía.
        // Antes de eso, se agregan los procesos que llegan en ese tiempo.
        while (tiempo < tiempoTotal) {
            List<Proceso> llegan = llegadas.get(tiempo);
            for (Proceso proceso : llegan)
                primera.agregarProceso(proceso);

            int primeraNoVaciaIndice = Cola.encontrarPrimeraNoVacia(colas);
            Cola primeraNoVacia = colas.get(primeraNoVaciaIndice);
            Proceso aEjecutar = primeraNoVacia.sacarProceso();

            representacion.append(aEjecutar.getId());
            tiemposDeEjecucionRestantes.compute(aEjecutar, (k, v) -> v - 1);
            primeraNoVacia.decrementarTiempoDeEjecucionRestante(aEjecutar);

            // El proceso terminó su ejecución, se guarda su tiempo de finalización y se limpia de la cola en la que se
            // encontraba
            if (tiemposDeEjecucionRestantes.get(aEjecutar) == 0) {
                primeraNoVacia.limpiarProceso(aEjecutar);
                tiemposDeFinalizacion.put(aEjecutar, tiempo);
                tiempo++;
                continue;
            }

            // Si ya gastó su quantum y no es la última cola, se pasa a la siguiente.
            if (primeraNoVacia.obtenerTiempoDeEjecucionRestanteEnCola(aEjecutar) == 0 && primeraNoVaciaIndice < MAX_COLAS - 1) {
                primeraNoVacia.limpiarProceso(aEjecutar);
                colas.get(primeraNoVaciaIndice + 1).agregarProceso(aEjecutar);
            } else primeraNoVacia.regresarProceso(aEjecutar);

            tiempo++;
        }
        assert Cola.encontrarPrimeraNoVacia(colas) == -1;

        return Planificadores.obtenerResultado(cargaAleatoria, tiemposDeFinalizacion, representacion.toString());
    }


    public static class Cola {
        private final int quantum;
        private final Deque<Proceso> procesos = new ArrayDeque<>();
        private final Map<Proceso, Integer> tiempoDeEjecucionRestanteEnCola = new HashMap<>();

        public Cola(int quantum) {
            this.quantum = quantum;
        }

        public static int encontrarPrimeraNoVacia(List<Cola> lista) {
            for (int i = 0; i < lista.size(); i++) {
                Cola cola = lista.get(i);
                if (!cola.estaVacia()) return i;
            }
            return -1;
        }

        public int obtenerTiempoDeEjecucionRestanteEnCola(Proceso proceso) {
            return tiempoDeEjecucionRestanteEnCola.get(proceso);
        }

        public void decrementarTiempoDeEjecucionRestante(Proceso proceso) {
            tiempoDeEjecucionRestanteEnCola.compute(proceso, (k, v) -> v - 1);
        }

        public void agregarProceso(Proceso proceso) {
            tiempoDeEjecucionRestanteEnCola.put(proceso, quantum);
            procesos.addLast(proceso);
        }

        public Proceso sacarProceso() {
            return procesos.pollFirst();
        }

        public void limpiarProceso(Proceso proceso) {
            tiempoDeEjecucionRestanteEnCola.remove(proceso);
        }

        public void regresarProceso(Proceso proceso) {
            procesos.addFirst(proceso);
        }

        public boolean estaVacia() {
            return procesos.isEmpty();
        }
    }
}
