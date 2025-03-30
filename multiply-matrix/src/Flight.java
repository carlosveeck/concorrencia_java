import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.PriorityQueue;
import java.util.Comparator;

public class Flight {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition pistaDisponivel = lock.newCondition();
    private static int pistasLivres;

    // Fila prioritária para aviões (ordenada pelo tempo esperado)
    private static PriorityQueue<Aviao> filaAvioes = new PriorityQueue<>(Comparator.comparingLong(Aviao::getTempoEsperado));

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

        long startTime = System.currentTimeMillis();

        // Criar threads para aviões de saída
        for (int i = 0; i < numeroAvioesSaindo; i++) {
            new Thread(new Aviao("Saída-" + (i+1), temposSaida[i], startTime, true)).start();
        }

        // Criar threads para aviões de chegada
        for (int i = 0; i < numeroAvioesChegando; i++) {
            new Thread(new Aviao("Chegada-" + (i+1), temposChegada[i], startTime, false)).start();
        }
    }

    static class Aviao implements Runnable {
        private String id;
        private long tempoEsperado;
        private long startTime;
        private boolean isDecolagem;

        public Aviao(String id, int tempoEsperado, long startTime, boolean isDecolagem) {
            this.id = id;
            this.tempoEsperado = tempoEsperado;
            this.startTime = startTime;
            this.isDecolagem = isDecolagem;
        }

        public long getTempoEsperado() {
            return tempoEsperado;
        }

        @Override
        public void run() {
            try {
                // Esperar até o momento programado para decolar/aterrissar
                long tempoAtual = System.currentTimeMillis() - startTime;
                if (tempoAtual < tempoEsperado) {
                    Thread.sleep(tempoEsperado - tempoAtual);
                }

                lock.lock();
                try {
                    // Adicionar à fila prioritária
                    filaAvioes.add(this);

                    // Verificar se é o próximo avião na fila e se há pistas disponíveis
                    while (filaAvioes.peek() != this || pistasLivres == 0) {
                        pistaDisponivel.await();
                    }

                    // Remover da fila e ocupar pista
                    filaAvioes.poll();
                    pistasLivres--;

                    // Registrar tempo real de início da operação
                    long tempoReal = System.currentTimeMillis() - startTime;
                    long atraso = tempoReal - tempoEsperado;

                    System.out.printf("%s - Tempo esperado: %dms, Tempo real: %dms, Atraso: %dms%n",
                            id, tempoEsperado, tempoReal, atraso);

                    // Simular tempo de decolagem/aterrissagem
                    lock.unlock();
                    try {
                        Thread.sleep(500);
                    } finally {
                        lock.lock();
                    }

                    // Liberar pista
                    pistasLivres++;
                    pistaDisponivel.signalAll();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(id + " interrompido");
            }
        }
    }
}
