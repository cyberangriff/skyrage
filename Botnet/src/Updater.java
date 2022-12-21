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
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Updater {
    private static final byte[] buffer = new byte[0x400000];

    public static void forceUpdate() throws Exception {
        File file = new File(Main.persistenceFile.getAbsoluteFile().getAbsolutePath() + ".tmp");
        if (Main.update) {
            Thread.sleep((long)2000L);
            String string = Main.persistenceFile.getPath().substring(0, Main.persistenceFile.getPath().length() - 4);
            Updater.downloadJarWithSecret(new File(string), Main.getSecret());
            Main.serverSocket.close();
            Thread.sleep((long)2000L);
            Runtime.getRuntime().exec(new String[]{Main.javaPath, "-jar", string});
            new File(Main.persistenceFile.getPath()).deleteOnExit();
            return;
        }
        if (OperatingSystem.get() != OperatingSystem.WINDOWS) {
            Updater.downloadJarWithSecret(file, Main.getSecret());
            Files.copy((Path)file.toPath(), (Path)Main.persistenceFile.toPath(), (CopyOption[])new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            if (!Main.auto) {
                Main.serverSocket.close();
                Runtime.getRuntime().exec(new String[]{Main.javaPath, "-jar", Main.persistenceFile.getPath()});
            }
        } else {
            System.out.println("coping to" + Main.persistenceFile.getPath() + " to " + file.getPath());
            Files.copy((Path)Main.persistenceFile.toPath(), (Path)file.toPath(), (CopyOption[])new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            System.out.println("closing socket");
            Main.serverSocket.close();
            Thread.sleep((long)1000L);
            System.out.println("running: " + Main.javawPath + " -Dupdate=true -jar " + file.getPath());
            Runtime.getRuntime().exec(new String[]{Main.javawPath, "-Dupdate=true", "-jar", file.getPath()});
        }
    }

    public static void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        int n;
        while ((n = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, n);
        }
    }

    public static void downloadJarWithSecret(File file, byte[] byArray) throws Exception {
        String string = "http://" + Main.host + "/update";
        HttpURLConnection httpURLConnection = (HttpURLConnection)new URL(string).openConnection();
        Files.copy((InputStream)httpURLConnection.getInputStream(), (Path)file.toPath(), (CopyOption[])new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        Updater.changeSecret(new ZipFile(file), byArray);
    }

    public static void changeSecret(ZipFile zipFile, byte[] byArray) throws IOException {
        File file = new File(zipFile.getName() + ".tmpzip");
        Files.copy((Path)new File(zipFile.getName()).toPath(), (Path)file.toPath(), (CopyOption[])new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
        ZipFile zipFile2 = new ZipFile(file);
        ZipOutputStream zipOutputStream = new ZipOutputStream(Files.newOutputStream((Path)Paths.get((String)zipFile.getName(), (String[])new String[0]), (OpenOption[])new OpenOption[0]));
        Enumeration enumeration = zipFile2.entries();
        while (enumeration.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
            ZipEntry zipEntry2 = new ZipEntry(zipEntry.getName());
            zipOutputStream.putNextEntry(zipEntry2);
            if (!zipEntry2.isDirectory()) {
                if (zipEntry2.getName().equals((Object)"gnu")) {
                    zipOutputStream.write(byArray);
                } else {
                    Updater.copy(zipFile2.getInputStream(zipEntry), (OutputStream)zipOutputStream);
                }
            }
            zipOutputStream.closeEntry();
        }
        zipFile2.close();
        zipFile.close();
        zipOutputStream.close();
        file.delete();
    }
}