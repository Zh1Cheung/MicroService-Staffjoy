package xyz.staffjoy.account.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import xyz.staffjoy.account.TestConfig;
import xyz.staffjoy.account.client.AccountClient;
import xyz.staffjoy.account.dto.*;
import xyz.staffjoy.account.model.Account;
import xyz.staffjoy.account.repo.AccountRepo;
import xyz.staffjoy.account.repo.AccountSecretRepo;
import xyz.staffjoy.bot.client.BotClient;
import xyz.staffjoy.bot.dto.GreetingRequest;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.mail.client.MailClient;
import xyz.staffjoy.mail.dto.EmailRequest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/*
 * 集成测试(HTTP端口调用)
 * 通过AccountClient调用AccountController
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 12:00 2019/12/28
 *
 */

/*

单元测试（模块测试）是开发者编写的一小段代码，用于检验被测代码的一个很小的、很明确的功能是否正确。
一个单元测试是用于判断某个特定条件（或者场景）下某个特定函数的行为。
单元测试的方法必须是公有的（public）、没有返回值的（void）、没有入参的（）

集成测试，也叫组装测试或联合测试。在单元测试的基础上，将所有模块按照设计要求（如根据结构图）组装成为子系统或系统，进行集成测试。
实践表明，一些模块虽然能够单独地工作，但并不能保证连接起来也能正常的工作。一些局部反映不出来的问题，在全局上很可能暴露出来。

---
Junit 基本注解介绍
@BeforeClass 在所有测试方法执行前执行一次，一般在其中写上整体初始化的代码。
@AfterClass 在所有测试方法后执行一次，一般在其中写上销毁和释放资源的代码。
@Before 在每个方法测试前执行，一般用来初始化方法（比如我们在测试别的方法时，类中与其他测试方法共享的值已经被改变，为了保证测试结果的有效性，我们会在@Before注解的方法中重置数据）
@After 在每个测试方法执行后，在方法执行完成后要做的事情。
@Test(timeout = 1000) 测试方法执行超过1000毫秒后算超时，测试将失败。
@Test(expected = Exception.class) 测试方法期望得到的异常类，如果方法执行没有抛出指定的异常，则测试失败。
@Ignore("not ready yet") 执行测试时将忽略掉此方法，如果用于修饰类，则忽略整个类。
@Test 编写一般测试用例用。
@RunWith 在 Junit 中有很多个 Runner，他们负责调用你的测试代码，每一个 Runner 都有各自的特殊功能，你根据需要选择不同的 Runner 来运行你的测试代码。

---

在实际开发中，我们自己写的Controller、Service很可能去调用别的同事或别的项目组写的Service、Mapper，对方可能只写了一个接口，没有实现，这样是没法进行测试的。
Mock的作用：创建一个虚拟的对象替代那些不易构造或不易获取的对象。

---

在Spring cloud应用中，当我们要使用feign客户端时，一般要做以下三件事情 :
    使用注解@EnableFeignClients启用feign客户端；
    使用注解@FeignClient 定义feign客户端 ;
    使用注解@Autowired使用上面所定义feign的客户端

---

测试流程
1.
    Builder()
    GenericXXXResponse
    assertThat()

---

2.
    ArgumentCaptor<XXXRequest> argument = ArgumentCaptor.forClass(XXXRequest.class);
    verify(XXXClient, times(X)).XXX(argument.capture());
    XXXRequest = argument.getAllValues().get(X);
    log.info(XXXRequest.toString());
    assertThat(XXXRequest)

 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@EnableFeignClients(basePackages = {"xyz.staffjoy.account.client"})
@Import(TestConfig.class)
@Slf4j
public class AccountControllerTest {

    @Autowired
    AccountClient accountClient;

    @Autowired
    EnvConfig envConfig;

    // Mock的作用：创建一个虚拟的对象替代那些不易构造或不易获取的对象。
    // 当执行此对象的方法时，返回Mock数据
    @MockBean
    MailClient mailClient;

    @MockBean
    BotClient botClient;

    @Autowired
    private AccountRepo accountRepo;

    @Autowired
    private AccountSecretRepo accountSecretRepo;

    private Account newAccount;

    @Before
    public void setUp() {
        // sanity check
        accountRepo.deleteAll();
        // clear CURRENT_USER_HEADER for testing
        TestConfig.TEST_USER_ID = null;
    }

    /**
     * 改变Email测试
     */
    @Test
    public void testChangeEmail() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();

        // 创建账户
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();
        assertThat(accountDto.isConfirmedAndActive()).isFalse();

        // 改变Email
        String changedEmail = "test123@staffjoy.xyz";
        // Email确认
        EmailConfirmation emailConfirmation = EmailConfirmation.builder()
                .userId(accountDto.getId())
                .email(changedEmail)
                .build();
        BaseResponse baseResponse = accountClient.changeEmail(AuthConstant.AUTHORIZATION_WWW_SERVICE, emailConfirmation);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // 验证Email改变和账户激活
        GetOrCreateRequest getOrCreateRequest = GetOrCreateRequest.builder()
                .email(changedEmail)
                .build();
        genericAccountResponse = accountClient.getOrCreateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, getOrCreateRequest);
        AccountDto foundAccountDto = genericAccountResponse.getAccount();
        assertThat(foundAccountDto.getEmail()).isEqualTo(changedEmail);
        assertThat(foundAccountDto.isConfirmedAndActive()).isTrue();

        // 改变userid——无法找到账户——(baseResponse.isSuccess()).isFalse()
        emailConfirmation = EmailConfirmation.builder()
                .userId("not_existing_id")
                .email(changedEmail)
                .build();
        baseResponse = accountClient.changeEmail(AuthConstant.AUTHORIZATION_WWW_SERVICE, emailConfirmation);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isFalse();
        assertThat(baseResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);
    }

    @Test
    public void testRequestEmailChange() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create one account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // request email change
        String updatedEmail = "test111@staffjoy.xyz";
        EmailChangeRequest emailChangeRequest = EmailChangeRequest.builder()
                .email(updatedEmail)
                .userId(accountDto.getId())
                .build();
        BaseResponse baseResponse = accountClient.requestEmailChange(AuthConstant.AUTHORIZATION_SUPPORT_USER, emailChangeRequest);
        assertThat(baseResponse.isSuccess()).isTrue();

        // capture and verify email sent
        String externalApex = "staffjoy-v2.local";
        String subject = "Confirm Your New Email Address";
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        verify(mailClient, times(2)).send(argument.capture());
        EmailRequest emailRequest = argument.getAllValues().get(1);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(updatedEmail);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>Hi");
    }

    /**
     * 验证密码重置
     */
    @Test
    public void testRequestPasswordReset() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // 创建账户
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // 重置密码请求
        PasswordResetRequest passwordResetRequest = PasswordResetRequest.builder()
                .email(email)
                .build();
        BaseResponse baseResponse = accountClient.requestPasswordReset(AuthConstant.AUTHORIZATION_WWW_SERVICE, passwordResetRequest);
        assertThat(baseResponse.isSuccess()).isTrue();

        // 捕获验证
        String subject = "Activate your Staffjoy account";
        String externalApex = "staffjoy-v2.local";
        // 通过ArgumentCaptor对象的forClass(Class<T> clazz)方法来构建ArgumentCaptor对象。然后便可在验证时对方法的参数进行捕获，最后验证捕获的参数值
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        // 调用两次，一次创建账户，一次密码重置
        // 在verify方法的参数中调用argument.capture()方法来捕获输入的参数
        verify(mailClient, times(2)).send(argument.capture());
        // 之后argument变量中就保存了参数值，可以用argument.getValue()获取
        // 获取第二次调用的参数
        EmailRequest emailRequest = argument.getAllValues().get(1);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div><p>Hi");

        // 激活账户
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // 重置密码请求
        passwordResetRequest = PasswordResetRequest.builder()
                .email(email)
                .build();
        baseResponse = accountClient.requestPasswordReset(AuthConstant.AUTHORIZATION_WWW_SERVICE, passwordResetRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // 捕获验证
        subject = "Reset your Staffjoy password";
        argument = ArgumentCaptor.forClass(EmailRequest.class);
        // 调用三次，一次创建账户，两次密码重置
        verify(mailClient, times(3)).send(argument.capture());
        // 获取第三次调用的参数
        emailRequest = argument.getAllValues().get(2);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/reset/")).isEqualTo(2);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>We received a request to reset the password on your account.");
    }

    @Test
    public void testUpdateAndVerifyPasswordValidation() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create one account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // update password too short
        String password = "pass";
        UpdatePasswordRequest updatePasswordRequest = UpdatePasswordRequest.builder()
                .userId(accountDto.getId())
                .password(password)
                .build();
        BaseResponse baseResponse = accountClient.updatePassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, updatePasswordRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isFalse();
        assertThat(baseResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // update password success
        password = "pass123456";
        updatePasswordRequest = UpdatePasswordRequest.builder()
                .userId(accountDto.getId())
                .password(password)
                .build();
        baseResponse = accountClient.updatePassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, updatePasswordRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify not found
        VerifyPasswordRequest verifyPasswordRequest = VerifyPasswordRequest.builder()
                .password(password)
                .email("test000@staffjoy.xyz")
                .build();
        genericAccountResponse = accountClient.verifyPassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);

        // verify account not active
        verifyPasswordRequest = VerifyPasswordRequest.builder()
                .password(password)
                .email(email)
                .build();
        genericAccountResponse = accountClient.verifyPassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // activate the account
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        assertThat(genericAccountResponse.isSuccess()).isTrue();


        // verify wrong password
        verifyPasswordRequest = VerifyPasswordRequest.builder()
                .password("wrong_password")
                .email(email)
                .build();
        genericAccountResponse = accountClient.verifyPassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.UN_AUTHORIZED);
    }

    @Test
    public void testUpdateAndVerifyPassword() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // activate the account
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // update password
        String password = "pass123456";
        UpdatePasswordRequest updatePasswordRequest = UpdatePasswordRequest.builder()
                .userId(accountDto.getId())
                .password(password)
                .build();
        BaseResponse baseResponse = accountClient.updatePassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, updatePasswordRequest);
        log.info(baseResponse.toString());
        assertThat(baseResponse.isSuccess()).isTrue();

        // verify password
        VerifyPasswordRequest verifyPasswordRequest = VerifyPasswordRequest.builder()
                .password(password)
                .email(accountDto.getEmail())
                .build();
        genericAccountResponse = accountClient.verifyPassword(AuthConstant.AUTHORIZATION_WWW_SERVICE, verifyPasswordRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        assertThat(genericAccountResponse.getAccount()).isEqualTo(accountDto);
    }

    @Test
    public void testUpdateAccountValidation() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());
        when(botClient.sendSmsGreeting(any(GreetingRequest.class))).thenReturn(BaseResponse.builder().message("sms sent").build());

        // 创建第一个账户
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001801235";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // 创建第二个账户
        name = "testAccount002";
        email = "test002@staffjoy.xyz";
        phoneNumber = "18001801236";
        String subject = "Confirm Your New Email Address";
        String externalApex = "staffjoy-v2.local";
        createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // 更新
        String updatedName = "testAccountUpdate";
        accountDto.setName(updatedName);
        accountDto.setPhoneNumber("18001801237");

        // 无userid
        GenericAccountResponse genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.FAILURE);

        // 设置userid
        TestConfig.TEST_USER_ID = accountDto.getId();
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();


        String oldId = accountDto.getId();
        // 无法更新不存在的帐户
        accountDto.setId("not_existing_id");
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.NOT_FOUND);

        // 重置 id
        accountDto.setId(oldId);
        // 无法更新 member since
        Instant oldMemberSince = accountDto.getMemberSince();
        accountDto.setMemberSince(oldMemberSince.minusSeconds(5));
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // 重置 member since
        accountDto.setMemberSince(oldMemberSince);
        // 无法更新已经存在的Email
        String oldEmail = accountDto.getEmail();
        accountDto.setEmail("test001@staffjoy.xyz");
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // 重置 email
        accountDto.setEmail(oldEmail);
        // 无法更新已经存在的phonenumber
        String oldPhoneNumber = accountDto.getPhoneNumber();
        accountDto.setPhoneNumber("18001801235");
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset phone
        accountDto.setPhoneNumber(oldPhoneNumber);
        // user can't activate him/herself
        accountDto.setConfirmedAndActive(true);
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset confirm&active
        accountDto.setConfirmedAndActive(false);
        // user can't change support parameter
        accountDto.setSupport(true);
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset support
        accountDto.setSupport(false);
        // user can't change photo url
        String photoUrl = accountDto.getPhotoUrl();
        accountDto.setPhotoUrl("updated_photo_url");
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isFalse();
        assertThat(genericAccountResponse1.getCode()).isEqualTo(ResultCode.REQ_REJECT);

        // reset photo url
        accountDto.setPhotoUrl(photoUrl);
        // user updated his/her account successfully
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();

        // 用户可以更改Email
        oldEmail = accountDto.getEmail();
        String updatedEmail = "test003@staffjoy.xyz";
        accountDto.setEmail(updatedEmail);
        genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();
        accountDto = genericAccountResponse1.getAccount();
        // Email应还原为原始Email
        assertThat(accountDto.getEmail()).isEqualTo(oldEmail);

        // 验证更新Email后邮件发送请求
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        // 3 times, 2 for account creation, 1 for email update
        verify(mailClient, times(3)).send(argument.capture());
        EmailRequest emailRequest = argument.getAllValues().get(2);
        log.info(emailRequest.toString());
        assertThat(emailRequest.getTo()).isEqualTo(updatedEmail);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(updatedName);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + externalApex + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), updatedName)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div>Hi");
    }

    @Test
    public void testUpdateAccount() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());
        when(botClient.sendSmsGreeting(any(GreetingRequest.class))).thenReturn(BaseResponse.builder().message("sms sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001801236";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // update
        accountDto.setName("testAccountUpdate");
        accountDto.setConfirmedAndActive(true);
        accountDto.setPhoneNumber("18001801237");
        GenericAccountResponse genericAccountResponse1 = accountClient.updateAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, accountDto);
        log.info(genericAccountResponse1.toString());
        assertThat(genericAccountResponse1.isSuccess()).isTrue();
        AccountDto updatedAccountDto = genericAccountResponse1.getAccount();
        assertThat(updatedAccountDto).isEqualTo(accountDto);

        // 捕获验证
        ArgumentCaptor<GreetingRequest> argument = ArgumentCaptor.forClass(GreetingRequest.class);
        verify(botClient, times(1)).sendSmsGreeting(argument.capture());
        GreetingRequest greetingRequest = argument.getValue();
        assertThat(greetingRequest.getUserId()).isEqualTo(accountDto.getId());
    }

    @Test
    public void testGetAccount() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001801236";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();

        // get account fail
        genericAccountResponse = accountClient.getAccount(AuthConstant.AUTHORIZATION_AUTHENTICATED_USER, accountDto.getId());
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.FAILURE);

        // get account success
        genericAccountResponse = accountClient.getAccount(AuthConstant.AUTHORIZATION_WHOAMI_SERVICE, accountDto.getId());
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto gotAccountDto = genericAccountResponse.getAccount();
        assertThat(accountDto).isEqualTo(gotAccountDto);
    }

    @Test
    public void testListAccounts() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        // first account
        String name = "testAccount001";
        String email = "test001@staffjoy.xyz";
        String phoneNumber = "18001801236";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // list account and verify
        ListAccountResponse listAccountResponse = accountClient.listAccounts(AuthConstant.AUTHORIZATION_SUPPORT_USER, 0, 2);
        log.info((listAccountResponse.toString()));
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountList accountList = listAccountResponse.getAccountList();
        assertThat(accountList.getAccounts()).hasSize(1);
        assertThat(accountList.getLimit()).isEqualTo(2);
        assertThat(accountList.getOffset()).isEqualTo(0);

        // second account
        name = "testAccount002";
        email = "test002@staffjoy.xyz";
        phoneNumber = "18001801237";
        createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // list and verify
        listAccountResponse = accountClient.listAccounts(AuthConstant.AUTHORIZATION_SUPPORT_USER, 0, 2);
        log.info(listAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        accountList = listAccountResponse.getAccountList();
        assertThat(accountList.getAccounts()).hasSize(2);
        assertThat(accountList.getLimit()).isEqualTo(2);
        assertThat(accountList.getOffset()).isEqualTo(0);

        // third account
        name = "testAccount003";
        email = "test003@staffjoy.xyz";
        phoneNumber = "18001801238";
        createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // list and verify
        listAccountResponse = accountClient.listAccounts(AuthConstant.AUTHORIZATION_SUPPORT_USER, 1, 2);
        log.info(listAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        accountList = listAccountResponse.getAccountList();
        assertThat(accountList.getAccounts()).hasSize(1);
        assertThat(accountList.getLimit()).isEqualTo(2);
        assertThat(accountList.getOffset()).isEqualTo(1);
    }

    @Test
    public void testCreateAccountValidation() {
        String phoneNumber = "18001801236";
        // empty request
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .build();
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // invalid email
        createAccountRequest = CreateAccountRequest.builder()
                .email("invalid_email")
                .build();
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // invalid phone number
        createAccountRequest = CreateAccountRequest.builder()
                .phoneNumber("invalid_phonenumber")
                .build();
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // invalid auth
        createAccountRequest = CreateAccountRequest.builder()
                .phoneNumber(phoneNumber)
                .build();
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_ANONYMOUS_WEB, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.UN_AUTHORIZED);
    }

    @Test
    public void testGetAccountByPhoneNumber() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // get account by phonenumber
        genericAccountResponse = accountClient.getAccountByPhonenumber(AuthConstant.AUTHORIZATION_SUPPORT_USER, phoneNumber);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();
        assertThat(accountDto.getId()).isNotNull();
        assertThat(accountDto.getName()).isEqualTo(name);
        assertThat(accountDto.getEmail()).isEqualTo(email);
        assertThat(accountDto.getPhotoUrl()).isNotNull();
        assertThat(accountDto.getMemberSince()).isBeforeOrEqualTo(Instant.now());
        assertThat(accountDto.isSupport()).isFalse();
        assertThat(accountDto.isConfirmedAndActive()).isFalse();

        // invalid phone number
        genericAccountResponse = accountClient.getAccountByPhonenumber(AuthConstant.AUTHORIZATION_SUPPORT_USER, "invalid_phonenumber");
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.PARAM_VALID_ERROR);

        // phonenumber not exists
        genericAccountResponse = accountClient.getAccountByPhonenumber(AuthConstant.AUTHORIZATION_SUPPORT_USER, "18001801299");
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.NOT_FOUND);
    }


    @Test
    public void testCreateAccountSuccess() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount";
        String email = "test@staffjoy.xyz";
        String phoneNumber = "18001801236";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account and verify
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        assertThat(genericAccountResponse.isSuccess()).isTrue();
        AccountDto accountDto = genericAccountResponse.getAccount();
        assertThat(accountDto.getId()).isNotNull();
        assertThat(accountDto.getName()).isEqualTo(name);
        assertThat(accountDto.getEmail()).isEqualTo(email);
        assertThat(accountDto.getPhotoUrl()).isNotNull();
        assertThat(accountDto.getMemberSince()).isBeforeOrEqualTo(Instant.now());
        assertThat(accountDto.isSupport()).isFalse();
        assertThat(accountDto.isConfirmedAndActive()).isFalse();

        // capture and verify
        ArgumentCaptor<EmailRequest> argument = ArgumentCaptor.forClass(EmailRequest.class);
        verify(mailClient, times(1)).send(argument.capture());
        EmailRequest emailRequest = argument.getValue();
        assertThat(emailRequest.getTo()).isEqualTo(email);
        assertThat(emailRequest.getSubject()).isEqualTo(subject);
        assertThat(emailRequest.getName()).isEqualTo(name);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), "http://www." + envConfig.getExternalApex() + "/activate/")).isEqualTo(3);
        assertThat(StringUtils.countMatches(emailRequest.getHtmlBody(), name)).isEqualTo(1);
        assertThat(emailRequest.getHtmlBody()).startsWith("<div><p>Hi");
    }

    @Test
    public void testCreateAccountDuplicate() {
        // arrange mock
        when(mailClient.send(any(EmailRequest.class))).thenReturn(BaseResponse.builder().message("email sent").build());

        String name = "testAccount001";
        String email = "test01@staffjoy.xyz";
        String phoneNumber = "18001801236";
        String subject = "Activate your Staffjoy account";
        CreateAccountRequest createAccountRequest = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .phoneNumber(phoneNumber)
                .build();
        // create account
        GenericAccountResponse genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isTrue();

        // email duplicate
        createAccountRequest = CreateAccountRequest.builder()
                .name("testAccount002")
                .email(email)
                .phoneNumber("18001801237")
                .build();
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.FAILURE);

        // phone duplicate
        createAccountRequest = CreateAccountRequest.builder()
                .name("testAccount003")
                .email("test02@staffjoy.xyz")
                .phoneNumber(phoneNumber)
                .build();
        genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, createAccountRequest);
        log.info(genericAccountResponse.toString());
        assertThat(genericAccountResponse.isSuccess()).isFalse();
        assertThat(genericAccountResponse.getCode()).isEqualTo(ResultCode.FAILURE);
    }

    @After
    public void destroy() {
        accountRepo.deleteAll();
    }
}
