import java.net.IDN;
import java.net.InetSocketAddress;
import java.util.Hashtable;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

public class MinecraftAttackStorage {
    private final String ip;
    private final int port;
    private InetSocketAddress address;

    private MinecraftAttackStorage(String string, int n) {
        this.ip = string;
        this.port = n;
        this.address = new InetSocketAddress(this.ip, this.port);
    }

    public String getAsciiIp() {
        try {
            return IDN.toASCII((String)this.ip);
        }
        catch (Exception exception) {
            return "";
        }
    }

    public int getPort() {
        return this.port;
    }

    public InetSocketAddress getSocketAddress() {
        return this.address;
    }

    public static MinecraftAttackStorage parseIp(final String string) {
        if (string == null) {
            return null;
        }
        String[] split = string.split(":");
        if (string.startsWith("[")) {
            final int index = string.indexOf("]");
            if (index > 0) {
                final String substring = string.substring(1, index);
                final String trim = string.substring(index + 1).trim();
                if (trim.startsWith(":")) {
                    split = new String[] { substring, trim.substring(1) };
                }
                else {
                    split = new String[] { substring };
                }
            }
        }
        if (split.length > 2) {
            split = new String[] { string };
        }
        String s = split[0];
        int orDefault = (split.length > 1) ? orDefault(split[1], 25565) : 25565;
        if (orDefault == 25565) {
            final String[] lookupSRVRecord = lookupSRVRecord(s);
            s = lookupSRVRecord[0];
            orDefault = orDefault(lookupSRVRecord[1], 25565);
        }
        return new MinecraftAttackStorage(s, orDefault);
    }

    private static String[] lookupSRVRecord(String string) {
        try {
            Class.forName((String)"com.sun.jndi.dns.DnsContextFactory");
            Hashtable hashtable = new Hashtable();
            hashtable.put((Object)"java.naming.factory.initial", (Object)"com.sun.jndi.dns.DnsContextFactory");
            hashtable.put((Object)"java.naming.provider.url", (Object)"dns:");
            hashtable.put((Object)"com.sun.jndi.dns.timeout.retries", (Object)"1");
            InitialDirContext initialDirContext = new InitialDirContext(hashtable);
            Attributes attributes = initialDirContext.getAttributes("_minecraft._tcp." + string, new String[]{"SRV"});
            String[] stringArray = attributes.get("srv").get().toString().split(" ", 4);
            return new String[]{stringArray[3], stringArray[2]};
        }
        catch (Throwable throwable) {
            return new String[]{string, Integer.toString((int)25565)};
        }
    }

    private static int orDefault(String string, int n) {
        try {
            return Integer.parseInt((String)string.trim());
        }
        catch (Exception exception) {
            return n;
        }
    }
}