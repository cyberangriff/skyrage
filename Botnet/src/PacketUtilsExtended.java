import io.netty.buffer.ByteBuf;

/*
 * Duplicate member names - consider using --renamedupmembers true
 */
public class PacketUtilsExtended
extends PacketUtils {
    private int a;
    private String aString;
    private int b;
    private int c;

    public PacketUtilsExtended(int n, String string, int n2, int n3) {
        this.a = n;
        this.aString = string;
        this.b = n2;
        this.c = n3;
    }

    @Override
    public void writeVarInt(ByteBuf byteBuf) {
        PacketUtilsExtended.writeVarInt(0, byteBuf);
    }

    @Override
    public void read(ByteBuf byteBuf) {
        this.a = PacketUtilsExtended.readVarInt(byteBuf);
        this.aString = PacketUtilsExtended.receiveString(byteBuf, 255);
        this.b = byteBuf.readUnsignedShort();
        this.c = PacketUtilsExtended.readVarInt(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf) {
        PacketUtilsExtended.writeVarInt(this.a, byteBuf);
        PacketUtilsExtended.sendString(this.aString, byteBuf);
        byteBuf.writeShort(this.b);
        PacketUtilsExtended.writeVarInt(this.c, byteBuf);
    }
}