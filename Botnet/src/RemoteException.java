import java.io.DataInput;
import java.io.DataOutput;
import java.util.Arrays;
import java.util.Objects;

public class RemoteException {
    private String className;
    private String message;
    private RemoteException causeException;
    private StackTraceElement[] stackTrace;

    public RemoteException(String string, String string2, RemoteException remoteException, StackTraceElement[] stackTraceElementArray) {
        this.className = string;
        this.message = string2;
        this.causeException = remoteException;
        this.stackTrace = stackTraceElementArray;
    }

    public RemoteException(Throwable throwable) {
        this.className = throwable.getClass().getName();
        this.message = throwable.getMessage();
        this.causeException = throwable.getCause() != null ? new RemoteException(throwable.getCause()) : null;
        this.stackTrace = throwable.getStackTrace();
    }

    public void writeRemoteException(DataOutput dataOutput) throws Exception {
        for (RemoteException remoteException = this; remoteException != null; remoteException = remoteException.getCauseException()) {
            dataOutput.writeUTF(remoteException.getClassName());
            dataOutput.writeUTF(remoteException.getMessage());
            dataOutput.writeShort(remoteException.getStackTrace().length);
            for (StackTraceElement stackTraceElement : remoteException.getStackTrace()) {
                dataOutput.writeUTF(stackTraceElement.getClassName());
                dataOutput.writeUTF(stackTraceElement.getMethodName());
                dataOutput.writeBoolean(stackTraceElement.getFileName() != null);
                if (stackTraceElement.getFileName() != null) {
                    dataOutput.writeUTF(stackTraceElement.getFileName());
                }
                dataOutput.writeInt(stackTraceElement.getLineNumber());
            }
            dataOutput.writeBoolean(remoteException.getCauseException() != null);
        }
    }

    public static RemoteException readRemoteException(DataInput dataInput) throws Exception {
        boolean bl;
        RemoteException remoteException = null;
        boolean bl2 = true;
        RemoteException remoteException2 = null;
        do {
            String string = dataInput.readUTF();
            String string2 = dataInput.readUTF();
            StackTraceElement[] stackTraceElementArray = new StackTraceElement[dataInput.readUnsignedShort()];
            for (int i = 0; i < stackTraceElementArray.length; ++i) {
                String string3 = dataInput.readUTF();
                String string4 = dataInput.readUTF();
                String string5 = dataInput.readBoolean() ? dataInput.readUTF() : null;
                int n = dataInput.readInt();
                stackTraceElementArray[i] = new StackTraceElement(string3, string4, string5, n);
            }
            RemoteException remoteException3 = new RemoteException(string, string2, null, stackTraceElementArray);
            if (remoteException2 != null) {
                remoteException2.setCauseException(remoteException3);
            }
            remoteException2 = remoteException3;
            if (!bl2) continue;
            bl2 = false;
            remoteException = remoteException2;
        } while (bl = dataInput.readBoolean());
        return remoteException;
    }

    public String getClassName() {
        return this.className;
    }

    public void setClassName(String string) {
        this.className = string;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String string) {
        this.message = string;
    }

    public RemoteException getCauseException() {
        return this.causeException;
    }

    public void setCauseException(RemoteException remoteException) {
        this.causeException = remoteException;
    }

    public StackTraceElement[] getStackTrace() {
        return this.stackTrace;
    }

    public void setStackTrace(StackTraceElement[] stackTraceElementArray) {
        this.stackTrace = stackTraceElementArray;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        RemoteException remoteException = (RemoteException)o;
        return Objects.equals((Object)this.className, (Object)remoteException.className) && Objects.equals((Object)this.message, (Object)remoteException.message) && Objects.equals((Object)this.causeException, (Object)remoteException.causeException) && Arrays.equals((Object[])this.stackTrace, (Object[])remoteException.stackTrace);
    }

    public int hashCode() {
        int n = Objects.hash((Object[])new Object[]{this.className, this.message, this.causeException});
        n = 31 * n + Arrays.hashCode((Object[])this.stackTrace);
        return n;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.className).append(": ").append(this.message).append(System.lineSeparator());
        for (StackTraceElement stackTraceElement : this.stackTrace) {
            stringBuilder.append("\tat ").append(stackTraceElement.getClassName()).append(".").append(stackTraceElement.getMethodName()).append("(");
            if (stackTraceElement.isNativeMethod()) {
                stringBuilder.append("Native Method");
            } else {
                stringBuilder.append(stackTraceElement.getFileName() == null ? "Unknown Source" : stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber());
            }
            stringBuilder.append(")").append(System.lineSeparator());
        }
        if (this.causeException != null) {
            stringBuilder.append("Caused by: ").append((Object)this.causeException);
        }
        return stringBuilder.toString();
    }
}