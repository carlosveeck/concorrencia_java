import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Barbearia {
    private final int NUM_CADEIRAS_ESPERA;
    private int clientesEsperando = 0;
    private boolean barbeiroDormindo = true;
    private boolean corteEmAndamento = false;

    private final Lock lock = new ReentrantLock();
    private final Condition barbeiroDisponivel = lock.newCondition();
    private final Condition clienteChegou = lock.newCondition();
    private final Condition corteTerminado = lock.newCondition();

    public Barbearia(int numCadeirasEspera) {
        this.NUM_CADEIRAS_ESPERA = numCadeirasEspera;
    }

    public void iniciar() {
        Thread barbeiroThread = new Thread(new Barbeiro());
        barbeiroThread.start();

        // Simulação de clientes chegando
        for (int i = 1; i <= 10; i++) {
            Thread clienteThread = new Thread(new Cliente(i));
            clienteThread.start();
            try {
                Thread.sleep((int) (Math.random() * 2000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class Barbeiro implements Runnable {
        @Override
        public void run() {
            while (true) {
                lock.lock();
                try {
                    while (clientesEsperando == 0) {
                        barbeiroDormindo = true;
                        System.out.println("Barbeiro está dormindo...");
                        clienteChegou.await(); // Dorme até que um cliente chegue
                    }

                    barbeiroDormindo = false;
                    clientesEsperando--;
                    System.out.println("Barbeiro está cortando o cabelo...");
                    corteEmAndamento = true;
                    barbeiroDisponivel.signal(); // Avisa ao cliente que pode sentar

                    // Simula o tempo do corte
                    Thread.sleep(3000);

                    corteEmAndamento = false;
                    corteTerminado.signal(); // Avisa que o corte terminou
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    class Cliente implements Runnable {
        private final int id;

        public Cliente(int id) {
            this.id = id;
        }

        @Override
        public void run() {
            lock.lock();
            try {
                if (clientesEsperando < NUM_CADEIRAS_ESPERA) {
                    clientesEsperando++;
                    System.out.println("Cliente " + id + " chegou e está esperando. (" + clientesEsperando + " esperando)");

                    if (barbeiroDormindo) {
                        clienteChegou.signal(); // Acorda o barbeiro
                    }

                    // Espera até ser atendido
                    while (!corteEmAndamento || clientesEsperando != NUM_CADEIRAS_ESPERA - 1) {
                        barbeiroDisponivel.await();
                    }

                    System.out.println("Cliente " + id + " está tendo o cabelo cortado.");

                    // Espera o corte terminar
                    while (corteEmAndamento) {
                        corteTerminado.await();
                    }

                    System.out.println("Cliente " + id + " saiu com corte novo.");
                } else {
                    System.out.println("Cliente " + id + " saiu porque a barbearia está cheia.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        Barbearia barbearia = new Barbearia(3); // 3 cadeiras de espera
        barbearia.iniciar();
    }
}
