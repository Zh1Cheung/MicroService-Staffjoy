package xyz.staffjoy.faraday.core.trace;

import org.springframework.http.HttpStatus;

import static xyz.staffjoy.faraday.core.utils.BodyConverter.convertBodyToString;

/*
 *
 * 接收到的响应（状态码、消息体）
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 20:33 2019/12/29
 *
 */
public class ReceivedResponse extends HttpEntity {

    protected HttpStatus status;
    protected byte[] body;

    public HttpStatus getStatus() { return status; }
    protected void setStatus(HttpStatus status) { this.status = status; }

    public String getBodyAsString() { return convertBodyToString(body); }

    public byte[] getBody() { return body; }

    protected void setBody(byte[] body) { this.body = body; }

}
