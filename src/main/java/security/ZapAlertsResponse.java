package security;

import lombok.Data;
import java.util.List;

@Data
public class ZapAlertsResponse {
    private List<ZapAlert> alerts;
}