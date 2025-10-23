package costco.sim.logica;

public class Estadistica {
    private int totalClientesAtendidos;
    private double tiempoTotalEspera;
    private double tiempoTotalPago;
    private double tiempoMaximoEspera;
    private double tiempoMinimoEspera;


    public Estadistica() {
        this.totalClientesAtendidos = 0;
        this.tiempoTotalEspera = 0;
        this.tiempoTotalPago = 0;
        this.tiempoMaximoEspera = 0;
        this.tiempoMinimoEspera = Double.MAX_VALUE;
    }

    public void registrarCliente(Cliente cliente) {
        if (cliente == null || cliente.getEstado() != Estado.FINALIZADO) {
            return;
        }

        totalClientesAtendidos++;

        double tiempoEspera = cliente.getTiempoEspera();
        double tiempoPago = cliente.getTiempoPago();

        // Acumular tiempos (solo necesitamos las sumas, no cada cliente)
        tiempoTotalEspera += tiempoEspera;
        tiempoTotalPago += tiempoPago;

        // Actualizar extremos
        if (tiempoEspera > tiempoMaximoEspera) {
            tiempoMaximoEspera = tiempoEspera;
        }
        if (tiempoEspera < tiempoMinimoEspera && tiempoEspera > 0) {
            tiempoMinimoEspera = tiempoEspera;
        }
    }


    public double getTiempoPromedioEspera() {
        if (totalClientesAtendidos == 0) {
            return 0;
        }
        return tiempoTotalEspera / totalClientesAtendidos;
    }

    public double getTiempoPromedioPago() {
        if (totalClientesAtendidos == 0) {
            return 0;
        }
        return tiempoTotalPago / totalClientesAtendidos;
    }

    public double getTiempoPromedioTotal() {
        return getTiempoPromedioEspera() + getTiempoPromedioPago();
    }

    public double calcularEficiencia(int tiempoTotalSimulacion) {
        if (tiempoTotalSimulacion == 0) {
            return 0;
        }
        return (double) totalClientesAtendidos / tiempoTotalSimulacion;
    }


    public int getTotalClientesAtendidos() {
        return totalClientesAtendidos;
    }

    public double getTiempoTotalEspera() {
        return tiempoTotalEspera;
    }

    public double getTiempoTotalPago() {
        return tiempoTotalPago;
    }

    public double getTiempoMaximoEspera() {
        return tiempoMaximoEspera;
    }

    public double getTiempoMinimoEspera() {
        if (tiempoMinimoEspera == Double.MAX_VALUE) {
            return 0;
        }
        return tiempoMinimoEspera;
    }

    public String generarReporte() {
        StringBuilder sb = new StringBuilder();

        sb.append("       ESTADÍSTICAS DE LA SIMULACIÓN          \n");

        // Estadísticas de clientes
        sb.append("CLIENTES:\n");
        sb.append(String.format("  Total atendidos:         %d\n", totalClientesAtendidos));
        sb.append(String.format("  Tiempo promedio espera:  %.2f min\n", getTiempoPromedioEspera()));
        sb.append(String.format("  Tiempo promedio pago:    %.2f min\n", getTiempoPromedioPago()));
        sb.append(String.format("  Tiempo promedio total:   %.2f min\n", getTiempoPromedioTotal()));
        sb.append(String.format("  Tiempo máximo espera:    %.2f min\n", tiempoMaximoEspera));
        sb.append(String.format("  Tiempo mínimo espera:    %.2f min\n", getTiempoMinimoEspera()));
        sb.append("\n");

        sb.append("-----------------------------------------------\n");

        return sb.toString();
    }

    public String generarReporteCompleto(java.util.List<Caja> cajas, int tiempoTotal) {
        StringBuilder sb = new StringBuilder();

        sb.append(generarReporte());

        // Estadísticas por caja
        sb.append("ESTADÍSTICAS POR CAJA:\n\n");

        int totalClientesCajas = 0;
        int totalTiempoAbierto = 0;
        int cajasUsadas = 0;

        for (Caja caja : cajas) {
            int clientesAtendidos = caja.getClientesAtendidos();
            int tiempoAbierta = caja.calcularTiempoTotalAbierta(tiempoTotal);

            if (clientesAtendidos > 0 || tiempoAbierta > 0) {
                cajasUsadas++;
                totalClientesCajas += clientesAtendidos;
                totalTiempoAbierto += tiempoAbierta;

                sb.append(String.format("  Caja #%-2d:  %3d clientes  |  %3d min abierta",
                        caja.getNumeroCaja(), clientesAtendidos, tiempoAbierta));

                if (clientesAtendidos > 0) {
                    double clientesPorMinuto = (double) clientesAtendidos / tiempoAbierta;
                    sb.append(String.format("  |  %.2f cl/min", clientesPorMinuto));
                }
                sb.append("\n");
            }
        }

        sb.append("\n");
        sb.append(String.format("Total de cajas usadas:     %d\n", cajasUsadas));

        if (cajasUsadas > 0) {
            sb.append(String.format("Promedio clientes/caja:    %.2f\n",
                    (double) totalClientesCajas / cajasUsadas));
            sb.append(String.format("Promedio tiempo abierta:   %.2f min\n",
                    (double) totalTiempoAbierto / cajasUsadas));
        }

        sb.append("\n------------------------------------------------\n");

        return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("Estadisticas[clientes=%d, esperaProm=%.2f, pagoProm=%.2f]",
                totalClientesAtendidos, getTiempoPromedioEspera(), getTiempoPromedioPago());
    }
}