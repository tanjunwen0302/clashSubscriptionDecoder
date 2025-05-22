package top.tanjunwen.clashsubscriptiondecoder.web;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.tanjunwen.clashsubscriptiondecoder.model.dto.ClashResultDTO;
import top.tanjunwen.clashsubscriptiondecoder.service.SubscribeToConvertService;

import java.io.IOException;
import java.io.PrintWriter;

@RestController
@RequestMapping("/subscribeToConvert")
public class SubscribeToConvertController {
    @Resource
    private SubscribeToConvertService subscribeToConvertService;

    @GetMapping("/clash")
    public void clash(@RequestParam("targetUrl") String targetUrl,@RequestParam("index")Integer index, HttpServletResponse response) throws IOException {
        ClashResultDTO clashSubscribe = subscribeToConvertService.getClashSubscribe(targetUrl);
        subscribeToConvertService.clashDataReplacement(clashSubscribe,index);
        clashSubscribe.getHeaders().forEach((k,v)->response.setHeader(k,v.get(0)));
        PrintWriter writer = response.getWriter();
        writer.write(clashSubscribe.getBody());
        writer.flush();
    }
}
