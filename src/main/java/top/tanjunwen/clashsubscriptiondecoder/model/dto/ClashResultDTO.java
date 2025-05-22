package top.tanjunwen.clashsubscriptiondecoder.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
public class ClashResultDTO {
    private Map<String, List<String>> headers;
    private String body;
}
