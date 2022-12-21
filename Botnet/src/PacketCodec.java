import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.EncoderException;
import java.util.List;

public class PacketCodec
extends ByteToMessageCodec {
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, ByteBuf byteBuf2) throws Exception {
        if (byteBuf.readableBytes() > 65536) {
            throw new EncoderException("Packet too large: " + byteBuf.readableBytes() + " > 65536");
        }
        byteBuf2.writeShort(byteBuf.readableBytes());
        byteBuf2.writeBytes(byteBuf);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {
        if (in.readableBytes() < 2) {
            return;
        }
        int n = in.readerIndex();
        int n2 = in.readUnsignedShort();
        if (in.readableBytes() < n2) {
            in.readerIndex(n);
            return;
        }
        out.add((Object)in.readSlice(n2).retain());
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        encode(channelHandlerContext, (ByteBuf) o, byteBuf);
    }
}