import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordingStream;

import javax.management.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class JitWarmUpAgent {

    private static volatile boolean isClosed = false;

    private final static void start(String agentArgs) {

        // パラメータを取得
        String[] args = agentArgs.split(",");
        Map<String, String> params = new HashMap<>(2);
        for (String arg : args) {
            String[] kv = arg.split("=");
            if (kv.length == 2) {
                String key = kv[0];
                String value = kv[1];
                params.put(key, value);
            } else {
                System.out.println("パラメータの指定が不適切です：" + arg);
            }
        }

        final int threshold = Integer.parseInt(params.getOrDefault("threshold", "2000"));
        final int expectCompileLevel = Integer.parseInt(params.getOrDefault("compileLevel", "4"));
        final String mxbeanName = params.getOrDefault("mxbeanName", "com.example:type=JitWarmUp");

        try {
            // MXBeanの作成と登録
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(mxbeanName);
            JitWarmUpMXBeanImpl mxbean = new JitWarmUpMXBeanImpl();
            mbs.registerMBean(mxbean, objectName);

            // JIT コンパイルの監視
            RecordingStream recordingStream = new RecordingStream();
            recordingStream.enable("jdk.Compilation").withThreshold(Duration.ZERO);

            AtomicInteger compiledMethodNum = new AtomicInteger(0);

            Consumer<RecordedEvent> compilation = e -> {
                if (e.getShort("compileLevel") >= expectCompileLevel) {
                    int methodNum = compiledMethodNum.incrementAndGet();
                    if (methodNum >= threshold) {
                        if (!isClosed) {
                            synchronized (recordingStream) {
                                if (!isClosed) {
                                    System.out.println("コンパイルレベル " + expectCompileLevel + "で閾値を超える数の " + methodNum + " メソッドがコンパイルされました");
                                    mxbean.warmedUp();
                                    recordingStream.close();
                                    isClosed = true;
                                }
                            }
                        }
                    }
                }
            };
            recordingStream.onEvent("jdk.Compilation", compilation);

            recordingStream.startAsync();

            Runtime.getRuntime().addShutdownHook(new Thread("JitWarmUpShutdownHook") {
                public void run() {
                    if (!isClosed) {
                        recordingStream.close();
                    }
                }
            });
        } catch (MalformedObjectNameException | NotCompliantMBeanException | InstanceAlreadyExistsException | MBeanRegistrationException e) {
            e.printStackTrace();
        }

    }

    // コマンドラインから起動する場合
    public static void premain(String agentArgs, Instrumentation inst) {
        start(agentArgs);
    }

    // 稼働時に起動する場合
    public static void agentmain(String agentArgs, Instrumentation inst) {
        start(agentArgs);
    }
}
