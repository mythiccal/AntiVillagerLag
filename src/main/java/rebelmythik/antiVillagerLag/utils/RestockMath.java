package rebelmythik.antiVillagerLag.utils;

import java.util.List;

public final class RestockMath {

    public static final long TICKS_PER_DAY = 24000L;

    private RestockMath() {
    }

    public static boolean shouldRestock(long worldTick, long dayTick, long lastRestockTick, List<Long> restockTimes) {
        long beginningOfDay = worldTick - dayTick;
        for (long restockTime : restockTimes) {
            long todayRestock = beginningOfDay + restockTime;
            if (worldTick >= todayRestock && lastRestockTick < todayRestock) {
                return true;
            }
        }
        return false;
    }

    public static long ticksUntilNextRestock(long worldTick, long dayTick, List<Long> restockTimes) {
        long beginningOfDay = worldTick - dayTick;
        long timeTillNextRestock = Long.MAX_VALUE;
        for (long restockTime : restockTimes) {
            long restockTick = beginningOfDay + restockTime;
            if (worldTick < restockTick) {
                timeTillNextRestock = Math.min(timeTillNextRestock, restockTick - worldTick);
            }
        }
        if (timeTillNextRestock == Long.MAX_VALUE) {
            timeTillNextRestock = (TICKS_PER_DAY + beginningOfDay + restockTimes.getFirst()) - worldTick;
        }
        return timeTillNextRestock;
    }
}
