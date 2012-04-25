package tmc.CrafterTracker.domain;

import com.google.gson.annotations.Expose;
import org.joda.time.DateTime;

public class WarningMessage {
    @Expose String sender;
    @Expose String recipient;
    @Expose String message;
    @Expose DateTime sentAt;
    @Expose DateTime aknowledgedAt;

}
