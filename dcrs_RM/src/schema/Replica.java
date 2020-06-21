package schema;

public class Replica {

    private String name;
    private int port;
    private int failureCount;
    private String path;
    private Process process;

    public Replica(String name, int port, String path) {
        this.name = name;
        this.port = port;
        this.path = path;
        this.failureCount = 0;
    }

    public boolean failureCountCritical() {
        return (this.failureCount >= 3);
    }

    public void incrementFailure() {
        this.failureCount++;
    }

    public String getName() {
        return this.name;
    }

    public int getPort() {
        return this.port;
    }

    public Process getProcess() {
        return this.process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public String getPath() {
        return path;
    }

    public void reset() {
        this.process.destroy();
        this.process = null;
        this.failureCount = 0;
    }
}
