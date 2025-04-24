import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Main {
    private static final String fileName = "C:\\Users\\USER01\\Desktop\\CSC227\\job.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Select algorithm: 1=FCFS, 2=RR, 3=Priority, 4=Exit");
            int choice = scanner.nextInt();
            if (choice == 4) {
                System.out.println("Exiting scheduler.");
                break;
            }

           
            ProcessQueue jobQueue   = new ProcessQueue();
            ProcessQueue readyQueue = new ProcessQueue();
            Object memoryLock       = new Object();
            Loader loader           = new Loader(jobQueue, readyQueue, 2048, memoryLock);
            Thread loaderThread     = new Thread(loader, "Loader");
            loaderThread.start();

         
            new JobReader(jobQueue, fileName).run();

            switch (choice) {
                case 1:
                    runFCFS(jobQueue, readyQueue, loader);
                    break;
                case 2:
                    runRR(jobQueue, readyQueue, loader, 7);
                    break;
                case 3:
                    runPriority(jobQueue, readyQueue, loader);
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
                    loaderThread.interrupt();
                    continue;
            }

            
            loaderThread.interrupt();
            System.out.println();
        }
        scanner.close();
    }

    private static void runFCFS(ProcessQueue jobQueue, ProcessQueue readyQueue, Loader loader) {
        System.out.println("--- FCFS Scheduling ---");
        int currentTime = 0;
        int totalProcessed = 0;
        double sumWaiting = 0;
        double sumTurnaround = 0;

        while (jobQueue.size() > 0 || readyQueue.size() > 0) {
            try {
                PCB pcb = readyQueue.dequeue();
                pcb.setState(ProcessState.RUNNING);
                int start = currentTime;
                currentTime += pcb.getBurstTime();
                pcb.setWaitingTime(start);
                pcb.setTurnaroundTime(currentTime);
                System.out.printf("[%d-%d] P%d%n", start, currentTime, pcb.getId());

                sumWaiting    += pcb.getWaitingTime();
                sumTurnaround += pcb.getTurnaroundTime();
                totalProcessed++;

                loader.freeMemory(pcb.getMemoryRequired());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (totalProcessed > 0) {
            System.out.printf("Average waiting time: %.2f ms%n", sumWaiting / totalProcessed);
            System.out.printf("Average turnaround time: %.2f ms%n", sumTurnaround / totalProcessed);
        }
    }

    private static void runRR(ProcessQueue jobQueue, ProcessQueue readyQueue, Loader loader, int quantum) {
        System.out.println("--- Round-Robin Scheduling (quantum=" + quantum + ") ---");
        int currentTime = 0;
        int totalProcessed = 0;
        double sumWaiting = 0;
        double sumTurnaround = 0;
        java.util.Map<Integer,Integer> original = new java.util.HashMap<>();

        while (jobQueue.size() > 0 || readyQueue.size() > 0) {
            try {
                PCB pcb = readyQueue.dequeue();
                pcb.setState(ProcessState.RUNNING);
                int start = currentTime;
                original.putIfAbsent(pcb.getId(), pcb.getBurstTime());
                int exec = Math.min(quantum, pcb.getBurstTime());
                currentTime += exec;
                pcb.setBurstTime(pcb.getBurstTime() - exec);
                if (pcb.getBurstTime() > 0) {
                    pcb.setState(ProcessState.READY);
                    readyQueue.enqueue(pcb);
                } else {
                    int finish = currentTime;
                    int wait = finish - original.get(pcb.getId());
                    pcb.setWaitingTime(wait);
                    pcb.setTurnaroundTime(finish);
                    System.out.printf("[%d-%d] P%d%n", start, finish, pcb.getId());
                    sumWaiting    += wait;
                    sumTurnaround += finish;
                    totalProcessed++;
                    loader.freeMemory(pcb.getMemoryRequired());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (totalProcessed > 0) {
            System.out.printf("Average waiting time: %.2f ms%n", sumWaiting / totalProcessed);
            System.out.printf("Average turnaround time: %.2f ms%n", sumTurnaround / totalProcessed);
        }
    }

    private static void runPriority(ProcessQueue jobQueue, ProcessQueue readyQueue, Loader loader) {
        System.out.println("--- Priority Scheduling ---");
        int currentTime = 0;
        int totalProcessed = 0;
        double sumWaiting = 0;
        double sumTurnaround = 0;

        while (jobQueue.size() > 0 || readyQueue.size() > 0) {
            try {
                PCB selected = readyQueue.dequeue();
                java.util.List<PCB> buf = new java.util.ArrayList<>();
                int n = readyQueue.size();
                for (int i = 0; i < n; i++) {
                    PCB p = readyQueue.dequeue();
                    if (p.getPriority() > selected.getPriority()) {
                        buf.add(selected);
                        selected = p;
                    } else buf.add(p);
                }
                buf.forEach(readyQueue::enqueue);

                int wait = currentTime;
                if (wait > selected.getPriority()) {
                    System.out.printf("[Starved] P%d waited %dms > priority %d%n", selected.getId(), wait, selected.getPriority());
                }
                selected.setState(ProcessState.RUNNING);
                int start = currentTime;
                currentTime += selected.getBurstTime();
                selected.setWaitingTime(wait);
                selected.setTurnaroundTime(currentTime);
                System.out.printf("[%d-%d] P%d%n", start, currentTime, selected.getId());
                sumWaiting    += wait;
                sumTurnaround += selected.getTurnaroundTime();
                totalProcessed++;
                loader.freeMemory(selected.getMemoryRequired());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        if (totalProcessed > 0) {
            System.out.printf("Average waiting time: %.2f ms%n", sumWaiting / totalProcessed);
            System.out.printf("Average turnaround time: %.2f ms%n", sumTurnaround / totalProcessed);
        }
    }
}

