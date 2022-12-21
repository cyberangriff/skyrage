public class OneLongTwoByteArrays {
    private final long _long;
    private final byte[] byteArray1;
    private final byte[] byteArray2;

    public OneLongTwoByteArrays(long l, byte[] byArray, byte[] byArray2) {
        this._long = l;
        this.byteArray1 = byArray;
        this.byteArray2 = byArray2;
    }

    public long getLong() {
        return this._long;
    }

    public byte[] getByteArray1() {
        return this.byteArray1;
    }

    public byte[] getByteArray2() {
        return this.byteArray2;
    }
}