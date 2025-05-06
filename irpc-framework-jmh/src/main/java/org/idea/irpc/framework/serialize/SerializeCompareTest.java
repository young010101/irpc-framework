package org.idea.irpc.framework.serialize;

import org.idea.irpc.framework.common.User;
import org.idea.irpc.framework.core.serialize.fastjson.FastJsonSerializerFactory;
import org.idea.irpc.framework.core.serialize.kryo.KryoSerializeFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author cyang
 */
public class SerializeCompareTest {
    private static User buildUser() {
        return new User("100", "cyan", "111@111.com", "11111",
                "12", "134971947", "1", "深圳南山区",
                "一些备注", "369733200008236423");
    }


    private static final User USER = buildUser();

    @Benchmark
    public void fastJson() {
        FastJsonSerializerFactory factory = new FastJsonSerializerFactory();
        byte[] serialize = factory.serialize(USER);
        factory.deserialize(serialize, User.class);
    }

    @Benchmark
    public void kryo() {
        KryoSerializeFactory factory = new KryoSerializeFactory();
        byte[] serialize = factory.serialize(USER);
        factory.deserialize(serialize, User.class);
    }


    /// 这是一个使用 JMH（Java Microbenchmark Harness）进行基准测试的 `main` 方法。你提到的参数设置如下：
    ///
    /// ```java
    /// Options opt = new OptionsBuilder()
    ///         .warmupIterations(2)
    ///         .measurementBatchSize(2)
    ///         .forks(1)
    ///         .build();
    /// ```
    ///
    /// 下面逐个解释这些参数的**作用**和**为什么常见地使用它们**：
    ///
    /// ---
    ///
    /// ### 1. `.warmupIterations(2)`
    ///
    /// * **作用**：设置**预热（warm-up）阶段迭代次数**，即在正式测量前先运行 2 次，JVM 可以在这期间进行 JIT 编译、优化等。
    /// * **为什么使用**：
    ///
    ///   * JVM 会在程序运行一段时间后进行优化（如 JIT），如果直接测量一开始的执行性能，可能偏低、不稳定。
    ///   * 2 次是一个非常轻量的预热设置，适合快速实验。
    ///
    /// ---
    ///
    /// ### 2. `.measurementBatchSize(2)`
    ///
    /// * **作用**：**每次测量采样时，会批量执行 2 次 benchmark 方法**，然后将这 2 次合在一起作为一个采样点。
    /// * **为什么使用**：
    ///
    ///   * 减少每次采样误差（如调用时间、调度干扰等）。
    ///   * 注意：这不是常见的测量参数，`measurementIterations` 更常用。如果你没有手动设置 `measurementIterations`，这个参数意义有限。
    ///
    /// ---
    ///
    /// ### 3. `.forks(1)`
    ///
    /// * **作用**：基准测试会 fork（创建新 JVM 实例）来运行测试，避免当前 JVM 的状态干扰结果。
    /// * **为什么使用**：
    ///
    ///   * 防止主程序状态、类加载、GC 状态等影响测量。
    ///   * 设为 `1` 表示 fork 一次。太多会导致测试慢，设为 `1` 是权衡速度与隔离的常见设置。
    ///
    /// ---
    ///
    /// ### 总结建议
    ///
    /// | 参数                          | 建议值   | 原因                                |
    /// | --------------------------- | ----- | --------------------------------- |
    /// | `.warmupIterations(n)`      | 2\~5  | JVM 需要预热，建议设置至少 1 次               |
    /// | `.measurementIterations(n)` | 5\~10 | 建议显式设置，不使用 `measurementBatchSize` |
    /// | `.forks(n)`                 | 1\~3  | fork 1 次基本够用                      |
    ///
    /// ---
    ///
    /// 需要更精准测试的话，你还可以设置：
    ///
    /// ```java
    /// .measurementIterations(5)
    /// .measurementTime(TimeValue.seconds(1))
    /// ```
    ///
    /// 是否需要我为你构建一个更标准的 `JMH` 基准测试模板？
    public static void main(String[] args) throws RunnerException {
        // 2 次 warmup, 2 次 test, 1 个线程
        // warmup是因为 jvm多次执行有优化
        Options opt = new OptionsBuilder()
                .warmupIterations(3)
                .measurementIterations(7)
                .forks(1)
                .build();
        new Runner(opt).run();
    }
}
