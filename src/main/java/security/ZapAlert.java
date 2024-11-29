package security;

import lombok.Data;
import java.util.Map;

@Data
public class ZapAlert {
    private String sourceId;
    private String other;
    private String method;
    private String evidence;
    private String pluginId;
    private String cweId;
    private String confidence;
    private String wascId;
    private String description;
    private String messageId;
    private String inputVector;
    private String url;
    private Map<String, String> tags;
    private String reference;
    private String solution;
    private String alert;
    private String param;
    private String attack;
    private String name;
    private String risk;
    private String id;
    private String alertRef;
}