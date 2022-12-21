import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class PacketUtils {
    public static void printBullshit(String[] stringArray) {
        System.out.println(622726);
    }

    public static void sendString(String string, ByteBuf byteBuf) {
        if (string.length() > Short.MAX_VALUE) {
            throw new RuntimeException("Cannot send string longer than Short.MAX_VALUE (got " + string.length() + " characters)");
        }
        byte[] byArray = string.getBytes(StandardCharsets.UTF_8);
        PacketUtils.writeVarInt(byArray.length, byteBuf);
        byteBuf.writeBytes(byArray);
    }

    public static String receiveString(ByteBuf byteBuf) {
        return PacketUtils.receiveString(byteBuf, Short.MAX_VALUE);
    }

    public static String receiveString(ByteBuf byteBuf, int n) {
        int n2 = PacketUtils.readVarInt(byteBuf);
        if (n2 > n * 4) {
            throw new RuntimeException("Cannot receive string longer than " + n * 4 + " (got " + n2 + " bytes)");
        }
        byte[] byArray = new byte[n2];
        byteBuf.readBytes(byArray);
        String string = new String(byArray, StandardCharsets.UTF_8);
        if (string.length() > n) {
            throw new RuntimeException("Cannot receive string longer than " + n + " (got " + string.length() + " characters)");
        }
        return string;
    }

    public static void sendByteArray(byte[] byArray, ByteBuf byteBuf) {
        if (byArray.length > Short.MAX_VALUE) {
            throw new RuntimeException("Cannot send byte array longer than Short.MAX_VALUE (got " + byArray.length + " bytes)");
        }
        PacketUtils.writeVarInt(byArray.length, byteBuf);
        byteBuf.writeBytes(byArray);
    }

    public static byte[] readByteArray(ByteBuf byteBuf) {
        byte[] byArray = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(byArray);
        return byArray;
    }

    public static byte[] readAllBytes(ByteBuf byteBuf) {
        return PacketUtils.receiveByteArray(byteBuf, byteBuf.readableBytes());
    }

    public static byte[] receiveByteArray(ByteBuf byteBuf, int n) {
        int n2 = PacketUtils.readVarInt(byteBuf);
        if (n2 > n) {
            throw new RuntimeException("Cannot receive byte array longer than " + n + " (got " + n2 + " bytes)");
        }
        byte[] byArray = new byte[n2];
        byteBuf.readBytes(byArray);
        return byArray;
    }

    public static int[] readVarIntArray(ByteBuf byteBuf) {
        int n = PacketUtils.readVarInt(byteBuf);
        int[] nArray = new int[n];
        for (int i = 0; i < n; ++i) {
            nArray[i] = PacketUtils.readVarInt(byteBuf);
        }
        return nArray;
    }

    public static void writeStringArray(List list, ByteBuf byteBuf) {
        PacketUtils.writeVarInt(list.size(), byteBuf);
        for (Object stringObj : list) {
            String string = (String) stringObj;
            PacketUtils.sendString(string, byteBuf);
        }
    }

    public static List receiveStringArray(ByteBuf byteBuf) {
        int n = PacketUtils.readVarInt(byteBuf);
        ArrayList arrayList = new ArrayList(n);
        for (int i = 0; i < n; ++i) {
            arrayList.add((Object)PacketUtils.receiveString(byteBuf));
        }
        return arrayList;
    }

    public static int readVarInt(ByteBuf byteBuf) {
        return PacketUtils.readVarInt(byteBuf, 5);
    }

    public static int readVarInt(ByteBuf byteBuf, int n) {
        byte by;
        int n2 = 0;
        int n3 = 0;
        do {
            by = byteBuf.readByte();
            n2 |= (by & 0x7F) << n3++ * 7;
            if (n3 <= n) continue;
            throw new RuntimeException("VarInt too big");
        } while ((by & 0x80) == 128);
        return n2;
    }

    public static void writeVarInt(int n, ByteBuf byteBuf) {
        do {
            int n2 = n & 0x7F;
            if ((n >>>= 7) != 0) {
                n2 |= 0x80;
            }
            byteBuf.writeByte(n2);
        } while (n != 0);
    }

    public static int fuckRead(ByteBuf byteBuf) {
        int n = byteBuf.readUnsignedShort();
        int n2 = 0;
        if ((n & 0x8000) != 0) {
            n &= Short.MAX_VALUE;
            n2 = byteBuf.readUnsignedByte();
        }
        return (n2 & 0xFF) << 15 | n;
    }

    public static void fuckWrite(ByteBuf byteBuf, int n) {
        int n2 = n & Short.MAX_VALUE;
        int n3 = (n & 0x7F8000) >> 15;
        if (n3 != 0) {
            n2 |= 0x8000;
        }
        byteBuf.writeShort(n2);
        if (n3 != 0) {
            byteBuf.writeByte(n3);
        }
    }

    public static void writeUUID(UUID uUID, ByteBuf byteBuf) {
        byteBuf.writeLong(uUID.getMostSignificantBits());
        byteBuf.writeLong(uUID.getLeastSignificantBits());
    }

    public static UUID readUUID(ByteBuf byteBuf) {
        return new UUID(byteBuf.readLong(), byteBuf.readLong());
    }

    public static void writeTripleString(StringTriple[] stringTripleArray, ByteBuf byteBuf) {
        if (stringTripleArray == null) {
            PacketUtils.writeVarInt(0, byteBuf);
            return;
        }
        PacketUtils.writeVarInt(stringTripleArray.length, byteBuf);
        for (StringTriple stringTriple : stringTripleArray) {
            PacketUtils.sendString(stringTriple.getString1(), byteBuf);
            PacketUtils.sendString(stringTriple.getString2(), byteBuf);
            if (stringTriple.getString3() != null) {
                byteBuf.writeBoolean(true);
                PacketUtils.sendString(stringTriple.getString3(), byteBuf);
                continue;
            }
            byteBuf.writeBoolean(false);
        }
    }

    public static StringTriple[] readTripleString(ByteBuf byteBuf) {
        StringTriple[] stringTripleArray = new StringTriple[PacketUtils.readVarInt(byteBuf)];
        for (int i = 0; i < stringTripleArray.length; ++i) {
            String string = PacketUtils.receiveString(byteBuf);
            String string2 = PacketUtils.receiveString(byteBuf);
            stringTripleArray[i] = byteBuf.readBoolean() ? new StringTriple(string, string2, PacketUtils.receiveString(byteBuf)) : new StringTriple(string, string2);
        }
        return stringTripleArray;
    }

    public static void writeOneLongTwoByteArrays(OneLongTwoByteArrays oneLongTwoByteArrays, ByteBuf byteBuf) {
        if (oneLongTwoByteArrays != null) {
            byteBuf.writeBoolean(true);
            byteBuf.writeLong(oneLongTwoByteArrays.getLong());
            PacketUtils.sendByteArray(oneLongTwoByteArrays.getByteArray1(), byteBuf);
            PacketUtils.sendByteArray(oneLongTwoByteArrays.getByteArray2(), byteBuf);
        } else {
            byteBuf.writeBoolean(false);
        }
    }

    public static OneLongTwoByteArrays readOneLongTwoByteArrays(ByteBuf byteBuf) {
        if (byteBuf.readBoolean()) {
            return new OneLongTwoByteArrays(byteBuf.readLong(), PacketUtils.readAllBytes(byteBuf), PacketUtils.readAllBytes(byteBuf));
        }
        return null;
    }

    public abstract void writeVarInt(ByteBuf var1);

    public void read(ByteBuf byteBuf) {
        throw new UnsupportedOperationException("Packet must implement read method");
    }

    public void read(ByteBuf byteBuf, MinecraftVersion.TO_CLIENT tO_CLIENT, int n) {
        this.read(byteBuf);
    }

    public void write(ByteBuf byteBuf) {
        throw new UnsupportedOperationException("Packet must implement write method");
    }

    public void writeVersionBullshit(ByteBuf byteBuf, MinecraftVersion.TO_CLIENT tO_CLIENT, int n) {
        this.write(byteBuf);
    }

    public static ByteBuf weirdWrite(ByteBuf byteBuf) {
        ByteBuf byteBuf2 = Unpooled.buffer(byteBuf.readableBytes() + PacketUtils.convertWeird(byteBuf.readableBytes()));
        PacketUtils.writeVarInt(byteBuf.readableBytes(), byteBuf2);
        byteBuf2.writeBytes(byteBuf, byteBuf.readableBytes());
        byteBuf.release();
        return byteBuf2;
    }

    private static int convertWeird(int n) {
        if ((n & 0xFFFFFF80) == 0) {
            return 1;
        }
        if ((n & 0xFFFFC000) == 0) {
            return 2;
        }
        if ((n & 0xFFE00000) == 0) {
            return 3;
        }
        if ((n & 0xF0000000) == 0) {
            return 4;
        }
        return 5;
    }
}