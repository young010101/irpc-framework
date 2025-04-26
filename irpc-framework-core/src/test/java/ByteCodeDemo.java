import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

public class ByteCodeDemo {
    public static void method() {

    }

    public static void method2(int a) {

    }

    public static void main(String[] args) {
//        String
        // contended
//        LongAdder
//        Stream
//        Arrays
        List list = new ArrayList();
        list.parallelStream().forEach(System.out::println);
    }
}
