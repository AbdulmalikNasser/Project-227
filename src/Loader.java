/* Loader thread: Moves PCBs from jobQueue into readyQueue when enough memory is available  Uses low level synchronization. */
public class Loader implements Runnable {
    private ProcessQueue jobQueue;
    private ProcessQueue readyQueue;
    private int freeMemory;
    private final Object memoryLock;

    public Loader(ProcessQueue jobQueue, ProcessQueue readyQueue,
                  int initialMemory, Object memoryLock) {
        this.jobQueue = jobQueue;
        this.readyQueue = readyQueue;
        this.freeMemory = initialMemory;
        this.memoryLock = memoryLock;
    }
    public void run() {
        try {
            while (true) {
                PCB pcb = jobQueue.dequeue();

                synchronized (memoryLock) {
                    // Wait until enough memory is free
                    while (pcb.getMemoryRequired() > freeMemory) {
                        memoryLock.wait();
                    }
                    freeMemory -= pcb.getMemoryRequired();
                }

                pcb.setState(ProcessState.READY);
                readyQueue.enqueue(pcb);
                
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[Loader] Interrupted, terminating.");
        }
    }

    public void freeMemory(int amount) {
        synchronized (memoryLock) {
            freeMemory += amount;
            memoryLock.notifyAll();
        }
    }
}
