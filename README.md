# jProcesses2 [![](https://jitpack.io/v/Osiris-Team/jProcesses2.svg)](https://jitpack.io/#Osiris-Team/jProcesses2)
Fork of [JProcesses](https://github.com/profesorfalken/jProcesses) with additional features and enhancements. Get cross-platform process details in Java.
Add this as dependency to your project via [Maven/Gradle/Sbt/Leinigen](https://jitpack.io/#Osiris-Team/jProcesses2/LATEST) (requires Java 7 or higher).

### Features
- Similar to the [Java 9 Process API](https://docs.oracle.com/javase/9/docs/api/java/lang/Process.html), but also runs on Java 8 and 7, and provides more process details.
- Parent/child process details and easy cross-plaftform processes priority changing.
- Easy access to the currently running JVM process.
```java
class Example{
    public static void main(String[] args) {
        ProcessUtils processUtils = new ProcessUtils();
        List<JProcess> list = processUtils.getProcesses();
        for (JProcess process : list) {
            // Collect process details:
            System.out.println(process.name);
            System.out.println(process.pid);
            System.out.println(process.command);
            System.out.println(process.usedMemoryInKB);
            // etc...

            // Parent/Child processes:
            JProcess parent = process.parentProcess;
            List<JProcess> childProcesses = process.childProcesses;

            // Cross-platform priorities and timestamps: 
            JProcessPriority priority = process.getPriority();
            Date timestampStart = process.getTimestampStart();

            // Interact with the process:
            process.changePriority(JProcessPriority.REAL_TIME);
            process.stop();
            process.kill();
        }

        // Easy access to the running JVM process:
        JProcess thisProcess = processUtils.getThisProcess(list);

        // Print parent/child processes in a tree:
        processUtils.printTree(list);
    }
}
```
