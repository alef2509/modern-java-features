/// **Compact Source Files and Instance Main Methods** (JEP 512, final in Java 25).
///
/// No class declaration, no `public static`, no `String[] args`, and `IO.println`
/// instead of `System.out.println`. Run it directly, without compiling:
///
///     java scripts/Hello.java
void main() {
    IO.println("Hello from a compact source file on Java " + Runtime.version().feature());
}
