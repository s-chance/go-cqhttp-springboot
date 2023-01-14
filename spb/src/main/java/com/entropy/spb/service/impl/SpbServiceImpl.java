package com.entropy.spb.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.entropy.spb.service.SpbService;
import com.entropy.spb.utils.HttpRequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@Slf4j
public class SpbServiceImpl implements SpbService {
    @Override
    public void SpbEventHandler(HttpServletRequest request) {
        JSONObject jsonParam = this.getJSONParam(request);
        log.info("接收参数为:{}", jsonParam.toString());
        if ("message".equals(jsonParam.getString("post_type"))) {
            String userId = jsonParam.getString("user_id");
            String message = jsonParam.getString("message");
            if ("你好".equals(message)) {
                String url = "http://127.0.0.1:5700/send_private_msg?user_id=" + userId + "&message=你好~";
                String result = HttpRequestUtils.doGet(url);
                log.info("发送成功:==>{}", result);
            }
        }
    }

    public JSONObject getJSONParam(HttpServletRequest request) {
        JSONObject jsonParam = null;

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            jsonParam = JSONObject.parseObject(stringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return jsonParam;
    }
}
