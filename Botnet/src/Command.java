import io.netty.channel.Channel;

public abstract class Command {
    public int counter;
    public MinecraftAttackStorage minecraftInfo;

    public Command(String string, int n) {
        this.counter = n;
        this.minecraftInfo = MinecraftAttackStorage.parseIp(string);
    }

    public abstract void run(Channel var1);

    public abstract void init(Channel var1);
}