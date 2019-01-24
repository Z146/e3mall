package cn.e3mall.sso.controller;

import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.sso.service.TokenService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @RequestMapping(value = "/user/token/{token}",produces = "application/json;charset=utf-8")
    @ResponseBody
    public String getUserByToken(@PathVariable String token,String callback){
        E3Result e3Result = tokenService.getUserByToken(token);
        if (StringUtils.isNotBlank(callback)) {
            return callback+"("+ JsonUtils.objectToJson(e3Result)+");";
        }
        return JsonUtils.objectToJson(e3Result);
    }
}
