package top.tanjunwen.clashsubscriptiondecoder.service;

import top.tanjunwen.clashsubscriptiondecoder.model.dto.ClashResultDTO;

public interface SubscribeToConvertService {
    ClashResultDTO getClashSubscribe(String url);

    void clashDataReplacement(ClashResultDTO clashResultDTO,Integer index);
}
