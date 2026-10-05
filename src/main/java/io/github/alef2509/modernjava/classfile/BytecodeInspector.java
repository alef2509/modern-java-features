package io.github.alef2509.modernjava.classfile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.MethodModel;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AccessFlag;
import java.util.List;

/// **Class-File API** (JEP 484, final in Java 24).
///
/// A JDK-native API to parse, generate and transform `.class` files, tracking the
/// class-file format as it evolves. Frameworks that relied on ASM or ByteBuddy
/// for proxies and instrumentation can use the platform instead.
public final class BytecodeInspector {

    private BytecodeInspector() {
    }

    /// Lists the methods declared in a class, read straight from its bytecode.
    public static List<String> declaredMethods(Class<?> type) throws IOException {
        String resource = "/" + type.getName().replace('.', '/') + ".class";
        try (InputStream in = type.getResourceAsStream(resource)) {
            ClassModel model = ClassFile.of().parse(in.readAllBytes());
            return model.methods().stream()
                    .map(MethodModel::methodName)
                    .map(Object::toString)
                    .toList();
        }
    }

    /// Generates, at runtime, a class equivalent to
    /// `class Answer { static int answer() { return 42; } }`, loads it and calls it.
    public static int generateAndInvoke() throws Throwable {
        ClassDesc answer = ClassDesc.of(BytecodeInspector.class.getPackageName() + ".Answer");
        byte[] bytes = ClassFile.of().build(answer, clazz -> clazz
                .withFlags(AccessFlag.PUBLIC, AccessFlag.FINAL)
                .withMethodBody("answer", MethodTypeDesc.of(ConstantDescs.CD_int),
                        ClassFile.ACC_PUBLIC | ClassFile.ACC_STATIC,
                        code -> code.bipush(42).ireturn()));

        MethodHandles.Lookup lookup = MethodHandles.lookup().defineHiddenClass(bytes, true);
        return (int) lookup.findStatic(lookup.lookupClass(), "answer",
                MethodType.methodType(int.class)).invokeExact();
    }
}
