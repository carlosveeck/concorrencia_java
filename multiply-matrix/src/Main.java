import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) {
        // Testar diferentes tamanhos de matriz
        testMatrixMultiplication(50, 50, 50, 100);
        testMatrixMultiplication(500, 600, 600, 500);
        // testMatrixMultiplication(15000, 20000, 2000, 15000); // Descomente para testar matrizes grandes (pode consumir muita memória)
    }

    public static void testMatrixMultiplication(int rowsA, int colsA, int rowsB, int colsB) {
        System.out.println("\nTestando matrizes de tamanho: A[" + rowsA + "x" + colsA + "] * B[" + rowsB + "x" + colsB + "]");

        // Verificar se as matrizes podem ser multiplicadas
        if (colsA != rowsB) {
            System.out.println("Dimensões inválidas para multiplicação de matrizes");
            return;
        }

        // Criar matrizes aleatórias
        double[][] matrixA = createRandomMatrix(rowsA, colsA);
        double[][] matrixB = createRandomMatrix(rowsB, colsB);

        // Multiplicação sequencial
        long startTime = System.currentTimeMillis();
        double[][] resultSequential = multiplySequential(matrixA, matrixB);
        long endTime = System.currentTimeMillis();
        System.out.println("Tempo sequencial: " + (endTime - startTime) + " ms");

        // Multiplicação com threads
        startTime = System.currentTimeMillis();
        double[][] resultThreads = multiplyWithThreads(matrixA, matrixB);
        endTime = System.currentTimeMillis();
        System.out.println("Tempo com threads: " + (endTime - startTime) + " ms");

        // Multiplicação com ExecutorService
        startTime = System.currentTimeMillis();
        double[][] resultExecutor = multiplyWithExecutor(matrixA, matrixB);
        endTime = System.currentTimeMillis();
        System.out.println("Tempo com Executor: " + (endTime - startTime) + " ms");

        // Verificar se os resultados são iguais (apenas para matrizes pequenas)
        if (rowsA <= 100 && colsB <= 100) {
            System.out.println("Resultados iguais (sequencial vs threads): " + matricesEqual(resultSequential, resultThreads));
            System.out.println("Resultados iguais (sequencial vs executor): " + matricesEqual(resultSequential, resultExecutor));
        }
    }

    // Cria uma matriz com valores aleatórios
    public static double[][] createRandomMatrix(int rows, int cols) {
        double[][] matrix = new double[rows][cols];
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = random.nextDouble() * 10;
            }
        }
        return matrix;
    }

    // Multiplicação sequencial de matrizes
    public static double[][] multiplySequential(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;
        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    // Multiplicação com threads (uma thread por linha da matriz resultante)
    public static double[][] multiplyWithThreads(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsB = b[0].length;
        double[][] result = new double[rowsA][colsB];
        Thread[] threads = new Thread[rowsA];

        for (int i = 0; i < rowsA; i++) {
            final int row = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < colsB; j++) {
                    for (int k = 0; k < a[0].length; k++) {
                        result[row][j] += a[row][k] * b[k][j];
                    }
                }
            });
            threads[i].start();
        }

        // Esperar todas as threads terminarem
        try {
            for (Thread thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    // Multiplicação com ExecutorService
    public static double[][] multiplyWithExecutor(double[][] a, double[][] b) {
        int rowsA = a.length;
        int colsB = b[0].length;
        double[][] result = new double[rowsA][colsB];

        // Usar número de processadores disponíveis
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < rowsA; i++) {
            final int row = i;
            executor.execute(() -> {
                for (int j = 0; j < colsB; j++) {
                    for (int k = 0; k < a[0].length; k++) {
                        result[row][j] += a[row][k] * b[k][j];
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    // Compara duas matrizes (apenas para testes)
    public static boolean matricesEqual(double[][] a, double[][] b) {
        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > 0.0001) {
                    return false;
                }
            }
        }
        return true;
    }
}