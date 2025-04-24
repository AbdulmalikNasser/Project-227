
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Main {
	  private static final String fileName = "job.txt";
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
    private static void printGanttChart(List<ExecutionLog> logs, String title) {
        System.out.println("\nGantt Chart for " + title + ":");

       
        StringBuilder chart = new StringBuilder();
        chart.append("|");
        for (ExecutionLog log : logs) {
            String label = " P" + log.processId + " ";
            chart.append(centerText(label, 5)).append("|");
        }
        System.out.println(chart);

        StringBuilder times = new StringBuilder();
        for (ExecutionLog log : logs) {
            times.append(String.format("%-5d ", log.startTime));
        }
        times.append(logs.get(logs.size() - 1).endTime);
        System.out.println(times);
    }

    
    private static String centerText(String text, int width) {
        int padding = width - text.length();
        int padStart = padding / 2;
        int padEnd = padding - padStart;
        return " ".repeat(padStart) + text + " ".repeat(padEnd);
    }

    
    private static void printStats(double sumWaiting, double sumTurnaround, int total) {
        System.out.printf("Average waiting time: %.2f ms\n", sumWaiting / total);
        System.out.printf("Average turnaround time: %.2f ms\n", sumTurnaround / total);
    }

    /**
     * First-Come-First-Serve: take each PCB and run it to completion.
     */
    private static void runFCFS(ProcessQueue jobQueue, ProcessQueue readyQueue, Loader loader) {
        System.out.println("--- FCFS Scheduling ---");
        int currentTime = 0;
        int totalProcessed = 0;
        double sumWaiting = 0;
        double sumTurnaround = 0;
        List<ExecutionLog> logs = new ArrayList<>();

        while (jobQueue.size() > 0 || readyQueue.size() > 0) {
            try {
                PCB pcb = readyQueue.dequeue();
                pcb.setState(ProcessState.RUNNING);
                int start = currentTime;
                currentTime += pcb.getBurstTime();
                pcb.setWaitingTime(start);
                pcb.setTurnaroundTime(currentTime);

                logs.add(new ExecutionLog(start, currentTime, pcb.getId()));

                sumWaiting += pcb.getWaitingTime();
                sumTurnaround += pcb.getTurnaroundTime();
                totalProcessed++;

                loader.freeMemory(pcb.getMemoryRequired());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        printGanttChart(logs, "FCFS");
        printStats(sumWaiting, sumTurnaround, totalProcessed);
    }



    /**
     * Round-Robin: run each PCB for up to quantum ms, re-enqueue if not done.
     */
    private static void runRR(ProcessQueue jobQueue, ProcessQueue readyQueue, Loader loader, int quantum) {
        System.out.println("--- Round-Robin Scheduling (quantum=" + quantum + ") ---");
        int currentTime = 0;
        int totalProcessed = 0;
        double sumWaiting = 0;
        double sumTurnaround = 0;
        java.util.Map<Integer,Integer> original = new java.util.HashMap<>();
        List<ExecutionLog> logs = new ArrayList<>();
        
        while (jobQueue.size() > 0 || readyQueue.size() > 0) {
            try {
                PCB pcb = readyQueue.dequeue();
                pcb.setState(ProcessState.RUNNING);
                int start = currentTime;
                original.putIfAbsent(pcb.getId(), pcb.getBurstTime());
                int exec = Math.min(quantum, pcb.getBurstTime());
                currentTime += exec;
                pcb.setBurstTime(pcb.getBurstTime() - exec);
                logs.add(new ExecutionLog(start, currentTime, pcb.getId()));
                if (pcb.getBurstTime() > 0) {
                    pcb.setState(ProcessState.READY);
                    readyQueue.enqueue(pcb);
                } else {
                    int finish = currentTime;
                    int wait = finish - original.get(pcb.getId());
                    pcb.setWaitingTime(wait);
                    pcb.setTurnaroundTime(finish);
                    
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
        printGanttChart(logs, "Round Robin");
        printStats(sumWaiting, sumTurnaround, totalProcessed);
//        if (totalProcessed > 0) {
//            System.out.printf("Average waiting time: %.2f ms%n", sumWaiting / totalProcessed);
//            System.out.printf("Average turnaround time: %.2f ms%n", sumTurnaround / totalProcessed);
//            System.out.printf("A: "+ sumTurnaround);
//        }
    }

    /**
     * Priority scheduling with starvation detection (wait > priority).
     */
    private static void runPriority(ProcessQueue jobQueue, ProcessQueue readyQueue, Loader loader) {
        System.out.println("--- Priority Scheduling ---");
        int currentTime = 0;
        int totalProcessed = 0;
        double sumWaiting = 0;
        double sumTurnaround = 0;
        List<ExecutionLog> logs = new ArrayList<>();
        List<Integer> starvedProcesses = new ArrayList<>();

        while (jobQueue.size() > 0 || readyQueue.size() > 0) {
            try {
                PCB selected = readyQueue.dequeue();
                List<PCB> buf = new ArrayList<>();
                int n = readyQueue.size();
                for (int i = 0; i < n; i++) {
                    PCB p = readyQueue.dequeue();
                    if (p.getPriority() > selected.getPriority()) {
                        buf.add(selected);
                        selected = p;
                    } else {
                        buf.add(p);
                    }
                }
                for (PCB p : buf) readyQueue.enqueue(p);

                int wait = currentTime;
                if (wait > selected.getPriority()) {
                    System.out.printf("[Starved] P%d waited %dms > priority %d%n",
                            selected.getId(), wait, selected.getPriority());
                    starvedProcesses.add(selected.getId());
                }

                selected.setState(ProcessState.RUNNING);
                int start = currentTime;
                currentTime += selected.getBurstTime();
                selected.setWaitingTime(wait);
                selected.setTurnaroundTime(currentTime);

                logs.add(new ExecutionLog(start, currentTime, selected.getId()));

                sumWaiting += wait;
                sumTurnaround += selected.getTurnaroundTime();
                totalProcessed++;
                loader.freeMemory(selected.getMemoryRequired());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        printGanttChart(logs, "Priority");
        printStats(sumWaiting, sumTurnaround, totalProcessed);

        if (!starvedProcesses.isEmpty()) {
            System.out.println("Processes that suffered from starvation:");
            for (int pid : starvedProcesses) {
                System.out.print("P" + pid+"|");
            }
        }
    }

}