package costco.sim.logica;

import costco.sim.Simulaciones.Simulacion;
import costco.sim.Simulaciones.SimulacionFilaUnica;
import costco.sim.Simulaciones.SimulacionMultiplesFilas;


public class prueba {

    public static void main(String[] args) {

       // SimulacionFilaUnica sim = new SimulacionFilaUnica();
        SimulacionMultiplesFilas sim= new SimulacionMultiplesFilas();
        //sim.iniciar();
        sim.iniciar();

        long inicio = System.currentTimeMillis();

        // Ejecutar sin límites artificiales
        while (!sim.haTerminado()) {
            sim.avanzarTiempo();

            // Mostrar progreso cada 100 minutos
            if (sim.getTiempoActual() % 100 == 0) {
                System.out.printf("⏰ %s | 👥 %d atendidos | ⏳ %d esperando | 🏪 %d cajas%n",
                        sim.formatearTiempo(sim.getTiempoActual()),
                        sim.getClientesAtendidosActual(),
                        sim.getClientesEsperandoTotal(),
                        sim.getCajasAbiertas());
            }
        }

        long fin = System.currentTimeMillis();
        double segundos = (fin - inicio) / 1000.0;

        System.out.println("\n" + "═".repeat(50));
        System.out.println("✅ SIMULACIÓN COMPLETADA");
        System.out.println("═".repeat(50));

        Estadistica stats = sim.getEstadisticas();

        System.out.println("\n📊 RESULTADOS:");
        System.out.println("   • Tiempo simulado:       " + sim.formatearTiempo(sim.getTiempoActual()));
        System.out.println("   • Tiempo real:           " + String.format("%.2f segundos", segundos));
        System.out.println("   • Clientes atendidos:    " + stats.getTotalClientesAtendidos());
        System.out.println("   • Tiempo espera prom:    " + String.format("%.2f min", stats.getTiempoPromedioEspera()));
        System.out.println("   • Tiempo pago prom:      " + String.format("%.2f min", stats.getTiempoPromedioPago()));
        System.out.println("   • Tiempo total prom:     " + String.format("%.2f min", stats.getTiempoPromedioTotal()));
        System.out.println("   • Cajas usadas:          " + contarCajasUsadas(sim));
        System.out.println();
    }


    private static int contarCajasUsadas(Simulacion sim) {
                int usadas = 0;
                for (int i = 1; i <= 12; i++) {
                    if (sim.getCaja(i).getClientesAtendidos() > 0) {
                        usadas++;
                    }
                }
                return usadas;
    }

}