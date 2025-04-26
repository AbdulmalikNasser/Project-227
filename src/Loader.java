//Loader thread: Moves PCBs from jobQueue into readyQueue when enough memory is available 
public class Loader implements Runnable {
    private ProcessQueue jobQueue;
    private ProcessQueue readyQueue;
    private int freeMemory;
    private final Object memoryLock;

    /**
     * jobQueue     queue of newly created PCBs
     * readyQueue   queue for processes ready to run
     * initialMemory total available memory (e.g. 2048 MB)
     * memoryLock    monitor object for memory synchronization
     */
    public Loader(ProcessQueue jobQueue, ProcessQueue readyQueue,
                  int initialMemory, Object memoryLock) {
        this.jobQueue = jobQueue;
        this.readyQueue = readyQueue;
        this.freeMemory = initialMemory;
        this.memoryLock = memoryLock;
    }

    @Override
    public void run() {
        try {
            while (true) {
                // Dequeue a process from the job queue
                PCB pcb = jobQueue.dequeue();

                // System call: allocate memory (blocks if insufficient)
                SysCall.allocateMemory(this, pcb);

                // System call: enqueue into ready queue
                SysCall.enqueueReady(readyQueue, pcb);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            
        }
    }

    // Adjust free memory (positive to free, negative to allocate) and wake waiters.
    
    public void freeMemory(int amount) {
        synchronized (memoryLock) {
            freeMemory += amount;
            memoryLock.notifyAll();
        }
    }
}
