package xyz.staffjoy.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmsRequest {
    @NotBlank(message = "Please provide a phone number")
    private String to;
    @NotBlank(message = "Please provide a template code")
    private String templateCode; //短信模板-可在短信控制台中找到
    private String templateParam; // 模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为("{\"name\":\"Tom\", \"code\":\"123\"}");
}
