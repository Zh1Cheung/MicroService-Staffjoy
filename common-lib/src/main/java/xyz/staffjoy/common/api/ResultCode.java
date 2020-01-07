package xyz.staffjoy.common.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.servlet.http.HttpServletResponse;


/*
 *
 * 结果代码枚举
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 19:39 2019/12/28
 *
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS("Operation is Successful", HttpServletResponse.SC_OK),

    FAILURE("Biz Exception", HttpServletResponse.SC_BAD_REQUEST),

    UN_AUTHORIZED("Request Unauthorized", HttpServletResponse.SC_UNAUTHORIZED),

    NOT_FOUND("404 Not Found", HttpServletResponse.SC_NOT_FOUND),

    MSG_NOT_READABLE("Message Can't be Read", HttpServletResponse.SC_BAD_REQUEST),

    METHOD_NOT_SUPPORTED("Method Not Supported", HttpServletResponse.SC_METHOD_NOT_ALLOWED),

    MEDIA_TYPE_NOT_SUPPORTED("Media Type Not Supported", HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE),

    REQ_REJECT("Request Rejected", HttpServletResponse.SC_FORBIDDEN),

    INTERNAL_SERVER_ERROR("Internal Server Error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),

    PARAM_MISS("Missing Required Parameter", HttpServletResponse.SC_BAD_REQUEST),

    PARAM_TYPE_ERROR("Parameter Type Mismatch", HttpServletResponse.SC_BAD_REQUEST),

    PARAM_BIND_ERROR("Parameter Binding Error", HttpServletResponse.SC_BAD_REQUEST),

    PARAM_VALID_ERROR("Parameter Validation Error", HttpServletResponse.SC_BAD_REQUEST);

    private String msg;
    private int code;

}
