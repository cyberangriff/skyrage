import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CommandHandler {
    public static final ConcurrentHashMap<Integer, CommandHandler> handlers = new ConcurrentHashMap<>();
    public boolean shouldRun = true;
    private final Command command;
    final int runUntil;
    final int delay;
    final int index;
    private InboundHandler inboundHandler;
    private final ChannelFutureListener futureListener = new ChannelFutureListener(){

        public void operationComplete(ChannelFuture channelFuture) throws Exception {
            if (channelFuture.isSuccess()) {
                CommandHandler.this.command.run(channelFuture.channel());
            } else if (channelFuture.cause() instanceof OutOfMemoryError) {
                CommandHandler.this.shouldRun = false;
                System.gc();
            }
        }
    };

    public CommandHandler(InboundHandler inboundHandler, int n, Command command, int n2, int n3) {
        this.inboundHandler = inboundHandler;
        this.command = command;
        this.runUntil = n2;
        this.delay = n3;
        this.index = n;
        handlers.put(n, this);
        this.setup();
    }

    public void setup() {
        Bootstrap bootstrap = this.init(this.command);
        new Thread(() -> {
            try {
                long l = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis((long)this.runUntil);
                InetSocketAddress inetSocketAddress = this.command.minecraftInfo.getSocketAddress();
                while (this.shouldRun && System.currentTimeMillis() <= l) {
                    bootstrap.connect((SocketAddress)inetSocketAddress).addListener(this.futureListener);
                    if (this.delay <= 0) continue;
                    try {
                        Thread.sleep((long)this.delay);
                    }
                    catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
                handlers.remove((Object)this.index);
            }
            catch (OutOfMemoryError outOfMemoryError) {
                System.gc();
            }
        }).start();
    }

    public Bootstrap init(final Command command) {
        return (Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().channel(NioSocketChannel.class)).group(this.inboundHandler.eventLoopGroup)).option(ChannelOption.TCP_NODELAY, true)).option(ChannelOption.AUTO_READ, false)).handler(new ChannelInitializer(){

            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addFirst("timeout", (ChannelHandler)new ReadTimeoutHandler(15));
                command.init(ch);
                ch.pipeline().addLast(InboundHandler.channelHandler);
            }
        });
    }
}