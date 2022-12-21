import io.netty.buffer.ByteBuf;

public class StatusPingPacketUtils
extends PacketUtils {
    @Override
    public void writeVarInt(ByteBuf byteBuf) {
        StatusPingPacketUtils.writeVarInt(0, byteBuf);
    }

    @Override
    public void read(ByteBuf byteBuf) {
    }

    @Override
    public void write(ByteBuf byteBuf) {
    }
}