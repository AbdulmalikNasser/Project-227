import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class JobReader implements Runnable {
    private ProcessQueue jobQueue;
    private String fileName;

   
    public JobReader(ProcessQueue jobQueue, String fileName) {
        this.jobQueue = jobQueue;
        this.fileName = fileName;
    }

    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Expected format: ID:burst:priority;memory
                String[] tokens = line.split("[:;]");
                int id       = Integer.parseInt(tokens[0]);
                int burst    = Integer.parseInt(tokens[1]);
                int priority = Integer.parseInt(tokens[2]);
                int memory   = Integer.parseInt(tokens[3]);

               
                PCB pcb = new PCB(id, burst, priority, memory);
                pcb.setState(ProcessState.READY);

               
                jobQueue.enqueue(pcb);
                System.out.println("[JobReader] Enqueued: " + pcb);
            }
        } catch (IOException e) {
            System.err.println("[JobReader] I/O error reading " + fileName + ": " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("[JobReader] Invalid number in input file: " + e.getMessage());
        }
    }
}
