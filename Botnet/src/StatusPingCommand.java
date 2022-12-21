import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.FastThreadLocal;

public class StatusPingCommand
extends Command {
    private final FastThreadLocal thread = new FastThreadLocal(){

        @Override
        protected ByteBuf initialValue() throws Exception {
            ByteBuf byteBuf = Unpooled.buffer();
            PacketUtilsExtended packetUtilsExtended = new PacketUtilsExtended(StatusPingCommand.this.counter, StatusPingCommand.this.minecraftInfo.getAsciiIp(), StatusPingCommand.this.minecraftInfo.getPort(), 1);
            packetUtilsExtended.writeVarInt(byteBuf);
            packetUtilsExtended.write(byteBuf);
            return PacketUtils.weirdWrite(byteBuf);
        }
    };
    private final FastThreadLocal otherThread = new FastThreadLocal(){

        @Override
        protected ByteBuf initialValue() throws Exception {
            ByteBuf byteBuf = Unpooled.buffer();
            StatusPingPacketUtils statusPingPacketUtils = new StatusPingPacketUtils();
            statusPingPacketUtils.writeVarInt(byteBuf);
            statusPingPacketUtils.write(byteBuf);
            return PacketUtils.weirdWrite(byteBuf);
        }
    };
    private final FastThreadLocal ThirdThread = new FastThreadLocal(){

        @Override
        protected ByteBuf initialValue() throws Exception {
            ByteBuf byteBuf = Unpooled.buffer();
            StatusPingPacketUtilsTwo statusPingPacketUtilsTwo = new StatusPingPacketUtilsTwo(System.currentTimeMillis());
            statusPingPacketUtilsTwo.writeVarInt(byteBuf);
            statusPingPacketUtilsTwo.write(byteBuf);
            return PacketUtils.weirdWrite(byteBuf);
        }
    };

    public StatusPingCommand(String string, int n) {
        super(string, n);
    }

    @Override
    public void run(Channel channel) {
        channel.write(((ByteBuf)this.thread.get()).resetReaderIndex().retain());
        channel.write(((ByteBuf)this.otherThread.get()).resetReaderIndex().retain());
        channel.writeAndFlush(((ByteBuf)this.ThirdThread.get()).resetReaderIndex().retain()).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void init(Channel channel) {
    }
}