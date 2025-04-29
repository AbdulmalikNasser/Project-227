import java.util.LinkedList;


public class ProcessQueue {
    private LinkedList<PCB> queue = new LinkedList<>();

    /* Enqueue a PCB. If threads are waiting in dequeue(), notify them, 
     used by jobreader and loader*/
    public synchronized void enqueue(PCB pcb) {
        queue.addLast(pcb);
        // Wake up any thread waiting for an element
        notifyAll();
    }

    public synchronized PCB dequeue() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.removeFirst();
    }

    public synchronized int size() {
        return queue.size();
    }
}
