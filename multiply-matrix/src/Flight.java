import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class Flight {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition pistaDisponivel = lock.newCondition();
    private static int pistasLivres;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Digite o número de aviões que irão sair:");
        int numeroAvioesSaindo = scanner.nextInt();

        System.out.println("Digite o número de aviões que irão chegar:");
        int numeroAvioesChegando = scanner.nextInt();

        int[] temposSaida = new int[numeroAvioesSaindo];
        int[] temposChegada = new int[numeroAvioesChegando];

        // Povoando tempos de saída
        for(int i = 0; i < numeroAvioesSaindo; i++) {
            System.out.println("Tempo para avião de saída " + (i+1) + ":");
            temposSaida[i] = scanner.nextInt();
        }

        // Povoando tempos de chegada
        for(int i = 0; i < numeroAvioesChegando; i++) {
            System.out.println("Tempo para avião de chegada " + (i+1) + ":");
            temposChegada[i] = scanner.nextInt();
        }

        System.out.println("Digite o número de pistas disponíveis:");
        pistasLivres = scanner.nextInt();
        scanner.close();

        // Criar threads para aviões de saída
        for(int i = 0; i < numeroAvioesSaindo; i++) {
            final int aviaoId = i + 1;
            new Thread(() -> {
                try {
                    Thread.sleep(temposSaida[i]);
                    decolar(aviaoId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
}