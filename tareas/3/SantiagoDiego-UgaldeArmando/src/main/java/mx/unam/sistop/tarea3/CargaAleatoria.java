package mx.unam.sistop.tarea3;

import lombok.Value;
import mx.unam.sistop.tarea3.utilidades.UtilidadesNumericas;

import java.util.ArrayList;
import java.util.List;

@Value
public class CargaAleatoria {
    private static final int MAX_TIEMPO_DE_EJECUCION_POR_PROCESO = 10;
    /**
     * Debe ser menor a 26, ya que se utilizan letras para nombrarlos.
     */
    private static final int TOTAL_PROCESOS = 10;

    List<Proceso> procesos;

    public static CargaAleatoria generar() {
        List<Proceso> procesosGenerados = new ArrayList<>();

        int tiempoTotal = 0, tiempoDeLlegadaAnterior = 0;
        char c = 'A';
        for (int i = 0; i < TOTAL_PROCESOS; i++, c++) {
            int tiempoDeLlegada = tiempoTotal <= 1 ?
                    0 : UtilidadesNumericas.obtenerEnteroInclusivo(tiempoDeLlegadaAnterior, tiempoTotal - 1);
            tiempoDeLlegadaAnterior = tiempoDeLlegada;

            int tiempoDeEjecucion = UtilidadesNumericas.obtenerEnteroPositivoAleatorio(MAX_TIEMPO_DE_EJECUCION_POR_PROCESO);
            tiempoTotal += tiempoDeEjecucion;

            Proceso proceso = new Proceso(tiempoDeLlegada, tiempoDeEjecucion, String.valueOf(c));

            procesosGenerados.add(proceso);
        }

        return new CargaAleatoria(procesosGenerados);
    }

    public int getTiempoTotalDeEjecucion() {
        return procesos.stream()
                .mapToInt(Proceso::getTiempoDeEjecucion)
                .reduce(0, Integer::sum);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        for (Proceso proceso : procesos) {
            stringBuilder.append(proceso).append("\n");
        }

        stringBuilder.append("Tiempo total: ").append(getTiempoTotalDeEjecucion());
        return stringBuilder.toString();
    }
}
