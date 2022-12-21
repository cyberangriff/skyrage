import io.netty.buffer.ByteBuf;

public class StatusPingPacketUtilsTwo
extends PacketUtils {
    private long a;

    @Override
    public void writeVarInt(ByteBuf byteBuf) {
        StatusPingPacketUtilsTwo.writeVarInt(1, byteBuf);
    }

    public StatusPingPacketUtilsTwo(long l) {
        this.a = l;
    }

    @Override
    public void read(ByteBuf byteBuf) {
        this.a = byteBuf.readLong();
    }

    @Override
    public void write(ByteBuf byteBuf) {
        byteBuf.writeLong(this.a);
    }

    public long a() {
        return this.a;
    }
}