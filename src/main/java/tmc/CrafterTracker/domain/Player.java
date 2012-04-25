package tmc.CrafterTracker.domain;

import com.google.gson.annotations.Expose;
import org.joda.time.DateTime;

public class Player {
    @Expose private String username;
    @Expose private DateTime joinedOn;
    @Expose private DateTime lastLogin;

}
