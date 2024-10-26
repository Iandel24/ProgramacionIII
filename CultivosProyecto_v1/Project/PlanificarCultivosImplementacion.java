package Project;

import Lib.Cultivo;
import Lib.Coordenada;
import Lib.CultivoSeleccionado;
import Lib.PlanificarCultivos;

import java.util.ArrayList;
import java.util.List;

public class PlanificarCultivosImplementacion implements PlanificarCultivos {

    private double[][] riesgosGlobal;
    private double maxWinnings; // Máximas ganancias
    private List<CultivoSeleccionado> bestPlan; // Mejor plan

    // Verifica si se puede plantar el cultivo en el área dada
    private boolean canPlaceCrop(int row, int col, int rows, int cols) {
        return (row + col) <= 10; // Verifica que la suma de filas y columnas no exceda 10
    }

    // Calcula el riesgo promedio para un área dada
    private double calculateAverageRisk(double[][] risks, int topLeftX, int topLeftY, int rows, int cols) {
        double totalRisk = 0.0;
        int count = 0;
        for (int i = topLeftX; i < topLeftX + rows; i++) {
            for (int j = topLeftY; j < topLeftY + cols; j++) {
                totalRisk += risks[i][j];
                count++;
            }
        }
        return totalRisk / count; // Devuelve el riesgo promedio
    }

    // Función que implementa la lógica de backtracking
    private void backtrack(List<Cultivo> cultivos, boolean[] used, int currentIndex, String temporada,
                           double currentWinnings, List<CultivoSeleccionado> currentPlan) {

        // Si se consideraron todos los cultivos, actualiza las máximas ganancias y el mejor plan
        if (currentIndex >= cultivos.size()) {
            if (currentWinnings > maxWinnings) {
                maxWinnings = currentWinnings;
                bestPlan = new ArrayList<>(currentPlan);
            }
            return;
        }

        Cultivo currentCultivo = cultivos.get(currentIndex);

        // Opción de no plantar el cultivo actual
        backtrack(cultivos, used, currentIndex + 1, temporada, currentWinnings, currentPlan);

        // Considera plantar el cultivo actual si es la temporada correcta y cumple con las restricciones
        if (currentCultivo.getSeason().equals(temporada) && canPlaceCrop(currentCultivo.getRows(), currentCultivo.getCols())) {
            for (int i = 0; i <= 10 - currentCultivo.getRows(); i++) {
                for (int j = 0; j <= 10 - currentCultivo.getCols(); j++) {
                    if (!used[currentIndex] || currentCultivo.isMultipleAllowed()) {
                        Coordenada topLeft = new Coordenada(i, j);
                        Coordenada bottomRight = new Coordenada(i + currentCultivo.getRows() - 1, j + currentCultivo.getCols() - 1);
                        double avgRisk = calculateAverageRisk(riesgosGlobal, i, j, currentCultivo.getRows(), currentCultivo.getCols());
                        double moneyUsed = 10 * currentCultivo.getRows() * currentCultivo.getCols();

                        CultivoSeleccionado seleccionado = new CultivoSeleccionado(currentCultivo, topLeft, bottomRight, moneyUsed, avgRisk);
                        currentPlan.add(seleccionado);
                        used[currentIndex] = true; // Marca el cultivo como usado

                        backtrack(cultivos, used, currentIndex, temporada, currentWinnings + currentCultivo.getProfit(), currentPlan);

                        currentPlan.remove(currentPlan.size() - 1);
                        used[currentIndex] = false; // Desmarca el cultivo
                    }
                }
            }
        }

        // Pasa al siguiente cultivo
        backtrack(cultivos, used, currentIndex + 1, temporada, currentWinnings, currentPlan);
    }

    // Implementación principal del mét odo obtenerPlanificacion
    @Override
    public List<CultivoSeleccionado> obtenerPlanificacion(List<Cultivo> cultivosDisponibles, double[][] riesgos, String temporada) {
        riesgosGlobal = riesgos;
        maxWinnings = 0;
        bestPlan = new ArrayList<>();
        boolean[] used = new boolean[cultivosDisponibles.size()];

        // Inicializa el algoritmo de backtracking
        backtrack(cultivosDisponibles, used, 0, temporada, 0.0, new ArrayList<>());

        return bestPlan;
    }
}