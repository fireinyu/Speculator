package engine;


import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import kotlin.jvm.functions.Function3;

public class Util {

    public static class Pair<X, Y> {

        public static <X, Y> Pair<X, Y> create (X first, Y second) {
            return new Pair<>(first, second);
        }
        public X first;
        public Y second;

        private Pair(X first, Y second) {
            this.first = first;
            this.second = second;
        }
    }

    @SuppressWarnings("unchecked")
    public static <S extends Number, T extends Number> T convertNumber (S input, T templateVal) {
        Class<T> template = (Class<T>) templateVal.getClass();
        T output;
        if (template.equals(Double.class)) {
            output = (T) (Double) input.doubleValue();
        } else if (template.equals(Float.class)) {
            output = (T) (Float) input.floatValue();
        } else if (template.equals(Long.class)) {
            output = (T) (Long) input.longValue();

        } else if (template.equals(Integer.class)) {
            output = (T) (Integer) input.intValue();
        } else {
            throw new ClassCastException("only primitive wrapper classes supported");
        }
        return output;
    }

    public static <S, T, R> Stream<R> combine (Stream<S> s1, Stream<T> s2, BiFunction<? super S, ? super T, ? extends R> combiner) {
        Iterator<T> s2Iter = s2.iterator();
        return s1.map(s -> combiner.apply(s, s2Iter.next()));
    }

    public static <S, T, U, R> Stream<R> combine (Stream<S> s1, Stream<T> s2, Stream<U> s3, Function3<S, T, U, R> combiner) {
        Iterator<T> s2Iter = s2.iterator();
        Iterator<U> s3Iter = s3.iterator();
        return s1.map(s -> combiner.invoke(s, s2Iter.next(), s3Iter.next()));
    }
}