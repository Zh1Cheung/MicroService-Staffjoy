package xyz.staffjoy.account.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import xyz.staffjoy.account.AccountConstant;
import xyz.staffjoy.account.dto.*;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.validation.Group1;
import xyz.staffjoy.common.validation.PhoneNumber;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;


/*
 *
 * API Client传递服务调用方
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 21:12 2019/12/24
 *
 */


/*
声明接口之后，在代码中通过@Resource注入之后即可使用。
    @FeignClient标签的常用属性如下：
    name：指定FeignClient的名称，如果项目使用了Ribbon，name属性会作为微服务的名称，用于服务发现
    url: url一般用于调试，可以手动指定@FeignClient调用的地址
    decode404:当发生http 404错误时，如果该字段位true，会调用decoder进行解码，否则抛出FeignException
    configuration: Feign配置类，可以自定义Feign的Encoder、Decoder、LogLevel、Contractfallback: 定义容错的处理类，当调用远程接口失败或超时时，会调用对应接口的容错逻辑，
    fallback指定的类必须实现@FeignClient标记的接口
    fallbackFactory: 工厂类，用于生成fallback类示例，通过这个属性我们可以实现每个接口通用的容错逻辑，减少重复的代码
    path: 定义当前FeignClient的统一前缀


 */
@FeignClient(name = AccountConstant.SERVICE_NAME, path = "/v1/account", url = "${staffjoy.account-service-endpoint}")
// TODO Client side validation can be enabled as needed
// @Validated
public interface AccountClient {

    /**
     * 创建新账户
     *
     * @param authz
     * @param request
     * @return
     */
    @PostMapping(path = "/create")
    GenericAccountResponse createAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid CreateAccountRequest request);

    /**
     * 同步用户事件到Intercom客服系统
     *
     * @param request
     * @return
     */
    @PostMapping(path = "/track_event")
    BaseResponse trackEvent(@RequestBody @Valid TrackEventRequest request);

    /**
     * 同步信息到Intercom客户系统
     *
     * @param request
     * @return
     */
    @PostMapping(path = "/sync_user")
    BaseResponse syncUser(@RequestBody @Valid SyncUserRequest request);

    /**
     * 获取现有账户列表（内部使用）
     *
     * @param authz
     * @param offset
     * @param limit
     * @return
     */
    @GetMapping(path = "/list")
    ListAccountResponse listAccounts(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam int offset, @RequestParam @Min(0) int limit);

    /**
     * 获取或创建（如不存在）账户
     *
     * @param request
     * @return
     */
    @PostMapping(path = "/get_or_create")
    GenericAccountResponse getOrCreateAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid GetOrCreateRequest request);

    /**
     * 通过用户id获取已有账户
     *
     * @param authz
     * @param userId
     * @return
     */
    @GetMapping(path = "/get")
    GenericAccountResponse getAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam @NotBlank String userId);

    /**
     * 更新账户
     *
     * @param authz
     * @param newAccount
     * @return
     */
    @PutMapping(path = "/update")
    GenericAccountResponse updateAccount(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid AccountDto newAccount);

    /**
     * 通过用户电话号码获取已有账户
     *
     * @param authz
     * @param phoneNumber
     * @return
     */
    @GetMapping(path = "/get_account_by_phonenumber")
    GenericAccountResponse getAccountByPhonenumber(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestParam @PhoneNumber String phoneNumber);

    /**
     * 更新密码
     *
     * @param authz
     * @param request
     * @return
     */
    @PutMapping(path = "/update_password")
    BaseResponse updatePassword(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid UpdatePasswordRequest request);

    /**
     * 校验密码
     *
     * @param authz
     * @param request
     * @return
     */
    @PostMapping(path = "/verify_password")
    GenericAccountResponse verifyPassword(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid VerifyPasswordRequest request);

    /**
     * 请求密码重置
     *
     * @param authz
     * @param request
     * @return
     */
    @PostMapping(path = "/request_password_reset")
    BaseResponse requestPasswordReset(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid PasswordResetRequest request);

    @PostMapping(path = "/request_email_change")
    BaseResponse requestEmailChange(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid EmailChangeRequest request);

    // ChangeEmail sets an account to active and updates its email. It is
    // used after a user clicks a confirmation link in their email.
    @PostMapping(path = "/change_email")
    BaseResponse changeEmail(@RequestHeader(AuthConstant.AUTHORIZATION_HEADER) String authz, @RequestBody @Valid EmailConfirmation request);
}
