package cn.devops.jira.response;


import cn.devops.jira.util.TimeTools;
import com.alibaba.fastjson2.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author cao.jin
 */
public class ResponseHandler {

    public static ResponseResult<?> success(Long startTime, Object object) {
        return new ResponseResult<>(CodeEnum.SUCCESS.getCode(), CodeEnum.SUCCESS.getMsg(), startTime,
                TimeTools.now() - startTime, object);
    }

    public static ResponseResult<?> success(Long startTime) {
        return new ResponseResult<>(CodeEnum.SUCCESS.getCode(), CodeEnum.SUCCESS.getMsg(), startTime,
                TimeTools.now() - startTime, "");
    }

    public static ResponseResult<?> error(Integer code, String msg, Long startTime) {
        return new ResponseResult<>(code, msg, startTime, TimeTools.now() - startTime, "");
    }

    public static ResponseResult<?> error(Long startTime, Object object) {
        return new ResponseResult<>(CodeEnum.ERROR.getCode(), CodeEnum.ERROR.getMsg(), startTime, TimeTools.now() - startTime, object);
    }

    public static ResponseResult<?> error(Integer code, String msg, Long startTime, Object object) {
        return new ResponseResult<>(code, msg, startTime, TimeTools.now() - startTime, object);
    }

    public static ResponseResult<?> error(Integer code, Object object) {
        return new ResponseResult<>(code, "error", TimeTools.now(), 0L, object);
    }

    public static HttpServletResponse response(HttpServletResponse response, int status, ResponseResult<?> result) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);
        PrintWriter out = response.getWriter();
        out.write(JSONObject.toJSONString(result));
        out.flush();
        out.close();
        return response;
    }
}
