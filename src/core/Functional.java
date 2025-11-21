package core;

@FunctionalInterface
interface Arithmetic {
    double operate(double a, double b);
}

@FunctionalInterface
interface StringOp {
    String apply(String s);
}

@FunctionalInterface
interface NoArg {
    void run();
}

@FunctionalInterface
interface OneArg<T> {
    T apply(T t);
}
