import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.concurrent.FastThreadLocal;
import java.security.SecureRandom;

public class LoginCommand
extends Command {
    private final FastThreadLocal thread1 = new FastThreadLocal(){

        @Override
        protected ByteBuf initialValue() throws Exception {
            ByteBuf byteBuf = Unpooled.buffer();
            PacketUtilsExtended packetUtilsExtended = new PacketUtilsExtended(LoginCommand.this.counter, LoginCommand.this.minecraftInfo.getAsciiIp(), LoginCommand.this.minecraftInfo.getPort(), 2);
            packetUtilsExtended.writeVarInt(byteBuf);
            packetUtilsExtended.write(byteBuf);
            return PacketUtils.weirdWrite(byteBuf);
        }
    };
    private final FastThreadLocal thread2 = new FastThreadLocal(){

        @Override
        protected ByteBuf initialValue() throws Exception {
            ByteBuf byteBuf = Unpooled.buffer();
            ByteBufStorage byteBufStorage = new ByteBufStorage();
            byteBufStorage.randomString = LoginCommand.this.randomString(16);
            if (LoginCommand.this.counter >= 759) {
                byteBufStorage.oneLongTwoByteArrays = new OneLongTwoByteArrays(Long.MAX_VALUE, new byte[256], new byte[256]);
            }
            byteBufStorage.writeVarInt(byteBuf);
            byteBufStorage.writeVersionBullshit(byteBuf, null, LoginCommand.this.counter);
            return PacketUtils.weirdWrite(byteBuf);
        }
    };
    String dictionary = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_.-";
    SecureRandom random = new SecureRandom();

    String randomString(int n) {
        StringBuilder stringBuilder = new StringBuilder(n);
        for (int i = 0; i < n; ++i) {
            stringBuilder.append(this.dictionary.charAt(this.random.nextInt(this.dictionary.length())));
        }
        return stringBuilder.toString();
    }

    public LoginCommand(String string, int n) {
        super(string, n);
    }

    @Override
    public void run(Channel channel) {
        channel.write(((ByteBuf)this.thread1.get()).resetReaderIndex().retain());
        channel.writeAndFlush(((ByteBuf)this.thread2.get()).resetReaderIndex().retain());
    }

    @Override
    public void init(Channel channel) {
    }
}