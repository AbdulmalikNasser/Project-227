
import java.io.*;
import java.util.*;

class Job {
    int processId;
    int burstTime;
    int priority;
    int memory;

    public Job(int processId, int burstTime, int priority, int memory) {
        this.processId = processId;
        this.burstTime = burstTime;
        this.priority = priority;
        this.memory = memory;
    }

    @Override
    public String toString() {
        return "Process ID: " + processId +
                ", Burst Time: " + burstTime + " ms" +
                ", Priority: " + priority +
                ", Memory: " + memory + " MB";
    }
}

public class JobScheduler {
    public static void main(String[] args) {
        String filename = "job.txt";
        List<Job> jobList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Split format: "1:25:4;500"
                String[] parts = line.split("[:;]");
                if (parts.length == 4) {
                    int processId = Integer.parseInt(parts[0]);
                    int burstTime = Integer.parseInt(parts[1]);
                    int priority = Integer.parseInt(parts[2]);
                    int memory = Integer.parseInt(parts[3]);

                    Job job = new Job(processId, burstTime, priority, memory);
                    jobList.add(job);
                }
            }

            System.out.println("Parsed Jobs:");
            for (Job job : jobList) {
                System.out.println(job);
            }

        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in input file.");
        }
    }
}

