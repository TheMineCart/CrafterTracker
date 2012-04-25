package tmc.CrafterTracker.domain;

import com.google.gson.annotations.Expose;
import org.joda.time.DateTime;

public class Session {
    @Expose String username;
    @Expose Integer blocksBroken;
    @Expose Integer blocksPlaced;
    @Expose DateTime connectedAt;
    @Expose DateTime disconnectedAt;
    @Expose DateTime lastAfkAt;
    @Expose Integer secondsAfk;

}
