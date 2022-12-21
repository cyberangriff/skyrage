import java.util.Arrays;
import java.util.List;

public class MinecraftVersion {
    public static final int V1_8 = 47;
    public static final int V1_9 = 107;
    public static final int V1_9_1 = 108;
    public static final int V1_9_2 = 109;
    public static final int V1_9_4 = 110;
    public static final int V1_10_2 = 210;
    public static final int V1_11 = 315;
    public static final int V1_11_2 = 316;
    public static final int V1_12 = 335;
    public static final int V1_12_1 = 338;
    public static final int V1_12_2 = 340;
    public static final int V1_13 = 393;
    public static final int V1_13_1 = 401;
    public static final int V1_13_2 = 404;
    public static final int V1_14 = 477;
    public static final int V1_14_1 = 480;
    public static final int V1_14_2 = 485;
    public static final int V1_14_3 = 490;
    public static final int V1_14_4 = 498;
    public static final int V1_15 = 573;
    public static final int V1_15_1 = 575;
    public static final int V1_15_2 = 578;
    public static final int V1_16 = 735;
    public static final int V1_16_1 = 736;
    public static final int V1_16_2 = 751;
    public static final int V1_16_3 = 753;
    public static final int V1_16_5 = 754;
    public static final int V1_17 = 755;
    public static final int V1_17_1 = 756;
    public static final int V1_18 = 757;
    public static final int V1_18_2 = 758;
    public static final int V1_19 = 759;
    public static final List humanReadable = Arrays.asList((Object[])new String[]{"1.8.x", "1.9.x", "1.10.x", "1.11.x", "1.12.x", "1.13.x", "1.14.x", "1.15.x", "1.16.x", "1.17.x", "1.18.x", "1.19.x"});
    public static final List protocolNumbers = Arrays.asList((Object[])new Integer[]{47, 107, 108, 109, 110, 210, 315, 316, 335, 338, 340, 393, 401, 404, 477, 480, 485, 490, 498, 573, 575, 578, 735, 736, 751, 753, 754, 755, 756, 757, 758, 759});

    public static enum TO_CLIENT {
        TO_CLIENT,
        TO_SERVER;

    }
}