public class JitWarmUpMXBeanImpl implements JitWarmUpMXBean{

    private volatile boolean isWarmedUp = false;

    public void warmedUp() {
        this.isWarmedUp = true;
    }

    @Override
    public boolean isWarmedUp() {
        return this.isWarmedUp;
    }
}