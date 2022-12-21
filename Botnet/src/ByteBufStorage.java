import io.netty.buffer.ByteBuf;

public class ByteBufStorage
extends PacketUtils {
    public String randomString;
    public OneLongTwoByteArrays oneLongTwoByteArrays;

    @Override
    public void writeVarInt(ByteBuf byteBuf) {
        ByteBufStorage.writeVarInt(0, byteBuf);
    }

    @Override
    public void read(ByteBuf byteBuf, MinecraftVersion.TO_CLIENT tO_CLIENT, int n) {
        this.randomString = ByteBufStorage.receiveString(byteBuf, 16);
        if (n >= 759) {
            this.oneLongTwoByteArrays = ByteBufStorage.readOneLongTwoByteArrays(byteBuf);
        }
    }

    @Override
    public void writeVersionBullshit(ByteBuf byteBuf, MinecraftVersion.TO_CLIENT tO_CLIENT, int n) {
        ByteBufStorage.sendString(this.randomString, byteBuf);
        if (n >= 759) {
            ByteBufStorage.writeOneLongTwoByteArrays(this.oneLongTwoByteArrays, byteBuf);
        }
    }
}