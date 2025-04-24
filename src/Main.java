import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Main {
    private static ProcessQueue jobQueue   = new ProcessQueue();
    private static ProcessQueue readyQueue = new ProcessQueue();
    private static final Object memoryLock = new Object();
    private static Loader loader;

    public static void main(String[] args) {
        // 1. Start loader thread (manages memory + readyQueue)
        loader = new Loader(jobQueue, readyQueue, 2048, memoryLock);
        new Thread(loader, "Loader").start();

        // 2. Start reader thread (fills jobQueue)
        new Thread(new JobReader(jobQueue,"C:\\Users\\USER01\\Desktop\\CSC227\\job.txt"),"JobReader").start();

        // 3. Prompt user for scheduling choice
        Scanner scanner = new Scanner(System.in);
        int choice=1;
        System.out.println("Select algorithm: 1=FCFS, 2=RR, 3=Priority");
        choice = scanner.nextInt();
        scanner.close();

        // 4. Dispatch scheduler
        switch (choice) {
            case 1:
                runFCFS();
                break;
            case 2:
                runRR(7);
                break;
            case 3:
                runPriority();
                break;
            default:
                System.out.println("Invalid choice, exiting.");
                System.exit(1);
        }
    }

    /**
     * First-Come-First-Serve: take each PCB and run it to completion.
     */
    private static void runFCFS() {
    	 System.out.println("--- FCFS Scheduling ---");
         int currentTime = 0;
         int totalProcessed = 0;
         double sumWaiting = 0;
         double sumTurnaround = 0;

         while (jobQueue.size() > 0 || readyQueue.size() > 0) {
             try {
                 PCB pcb = readyQueue.dequeue();
                 pcb.setState(ProcessState.RUNNING);
                 int startTime = currentTime;
                 currentTime += pcb.getBurstTime();
                 pcb.setWaitingTime(startTime);
                 pcb.setTurnaroundTime(currentTime);
                 System.out.printf("[%d-%d] P%d\n", startTime, currentTime, pcb.getId());

                 sumWaiting    += pcb.getWaitingTime();
                 sumTurnaround += pcb.getTurnaroundTime();
                 totalProcessed++;

                 loader.freeMemory(pcb.getMemoryRequired());
             } catch (InterruptedException e) {
                 Thread.currentThread().interrupt();
                 System.out.println("runFCFS interrupted.");
                 break;
             }
         }

         if (totalProcessed > 0) {
             System.out.printf("Average waiting time: %.2f ms\n", sumWaiting / totalProcessed);
             System.out.printf("Average turnaround time: %.2f ms\n", sumTurnaround / totalProcessed);
         }
     }


    /**
     * Round-Robin: run each PCB for up to quantum ms, re-enqueue if not done.
     */
    private static void runRR(int quantum) {
    	  System.out.println("--- Round-Robin Scheduling (quantum=" + quantum + ") ---");
          int currentTime = 0;
          int totalProcessed = 0;
          double sumWaiting = 0;
          double sumTurnaround = 0;
          Map<Integer, Integer> originalBursts = new HashMap<>();

          while (jobQueue.size() > 0 || readyQueue.size() > 0) {
              try {
                  PCB pcb = readyQueue.dequeue();
                  pcb.setState(ProcessState.RUNNING);
                  int startTime = currentTime;

                  // Record original burst if first encounter
                  if (!originalBursts.containsKey(pcb.getId())) {
                      originalBursts.put(pcb.getId(), pcb.getBurstTime());
                  }

                  int execTime = Math.min(quantum, pcb.getBurstTime());
                  currentTime += execTime;
                  pcb.setBurstTime(pcb.getBurstTime() - execTime);

                  if (pcb.getBurstTime() > 0) {
                      pcb.setState(ProcessState.READY);
                      readyQueue.enqueue(pcb);
                  } else {
                      int finishTime = currentTime;
                      int original = originalBursts.get(pcb.getId());
                      int waitingTime = finishTime - original;
                      pcb.setWaitingTime(waitingTime);
                      pcb.setTurnaroundTime(finishTime);
                      System.out.printf("[%d-%d] P%d%n", startTime, finishTime, pcb.getId());

                      sumWaiting    += waitingTime;
                      sumTurnaround += finishTime;
                      totalProcessed++;
                      loader.freeMemory(pcb.getMemoryRequired());
                  }
              } catch (InterruptedException e) {
                  Thread.currentThread().interrupt();
                  System.out.println("runRR interrupted.");
                  break;
              }
          }

          if (totalProcessed > 0) {
              System.out.printf("Average waiting time: %.2f ms%n", sumWaiting / totalProcessed);
              System.out.printf("Average turnaround time: %.2f ms%n", sumTurnaround / totalProcessed);
          }
      }
    /**
     * Priority scheduling with starvation detection (wait > priority).
     */
    private static void runPriority() {
    	System.out.println("--- Priority Scheduling ---");
        int currentTime = 0;
        int totalProcessed = 0;
        double sumWaiting = 0;
        double sumTurnaround = 0;

        while (jobQueue.size() > 0 || readyQueue.size() > 0) {
            try {
                // Select the highest-priority PCB
                PCB selected = readyQueue.dequeue();
                List<PCB> buffer = new ArrayList<>();
                int toScan = readyQueue.size();
                for (int i = 0; i < toScan; i++) {
                    PCB p = readyQueue.dequeue();
                    if (p.getPriority() > selected.getPriority()) {
                        buffer.add(selected);
                        selected = p;
                    } else {
                        buffer.add(p);
                    }
                }
                // Re-enqueue the others
                for (PCB p : buffer) {
                    readyQueue.enqueue(p);
                }

                // Check starvation (wait since time 0)
                int waiting = currentTime;
                if (waiting > selected.getPriority()) {
                    System.out.printf("[Starved] P%d waited %dms > priority %d%n",
                                      selected.getId(), waiting, selected.getPriority());
                }

                // Run the selected PCB
                selected.setState(ProcessState.RUNNING);
                int startTime = currentTime;
                currentTime += selected.getBurstTime();
                selected.setWaitingTime(waiting);
                selected.setTurnaroundTime(currentTime);
                System.out.printf("[%d-%d] P%d%n", startTime, currentTime, selected.getId());

                sumWaiting    += selected.getWaitingTime();
                sumTurnaround += selected.getTurnaroundTime();
                totalProcessed++;
                loader.freeMemory(selected.getMemoryRequired());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("runPriority interrupted.");
                break;
            }
        }

        if (totalProcessed > 0) {
            System.out.printf("Average waiting time: %.2f ms%n", sumWaiting / totalProcessed);
            System.out.printf("Average turnaround time: %.2f ms%n", sumTurnaround / totalProcessed);
        }
        // TODO: scan readyQueue for highest-priority PCB, simulate, track waiting, detect starvation
    }
}
