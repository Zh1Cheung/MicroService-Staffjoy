package xyz.staffjoy.account.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.staffjoy.account.AccountConstant;
import xyz.staffjoy.account.dto.AccountDto;
import xyz.staffjoy.account.model.Account;
import xyz.staffjoy.account.model.AccountSecret;
import xyz.staffjoy.account.props.AppProps;
import xyz.staffjoy.account.dto.AccountList;
import xyz.staffjoy.account.repo.AccountRepo;
import xyz.staffjoy.account.repo.AccountSecretRepo;
import xyz.staffjoy.account.service.helper.ServiceHelper;
import xyz.staffjoy.common.api.BaseResponse;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auditlog.LogEntry;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.crypto.Sign;
import xyz.staffjoy.common.env.EnvConfig;
import xyz.staffjoy.common.error.ServiceException;
import xyz.staffjoy.common.utils.Helper;
import xyz.staffjoy.mail.client.MailClient;
import xyz.staffjoy.mail.dto.EmailRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.util.stream.Collectors.toList;


@Service
public class AccountService {

    static ILogger logger = SLoggerFactory.getLogger(AccountService.class);


    private AccountRepo accountRepo;

    private AccountSecretRepo accountSecretRepo;

    private AppProps appProps;

    private EnvConfig envConfig;

    private MailClient mailClient;

    private ServiceHelper serviceHelper;

    private PasswordEncoder passwordEncoder;

    // ModelMapper 是一个 Object To Object 的工具
    private ModelMapper modelMapper;

    //把数据从数据库里提出，然后在内存里处理的，再返回数据库的法则。
    @PersistenceContext
    private EntityManager entityManager;

    public AccountService() {
    }

    /**
     * GetOrCreate供其他api内部使用，以便根据用户的电话号码或电子邮件来匹配用户。
     *
     * @param name
     * @param email
     * @param phoneNumber
     * @return
     */
    public AccountDto getOrCreate(String name, String email, String phoneNumber) {
        //检查是否存在用户
        Account existingAccount = null;
        if (StringUtils.hasText(email)) {
            existingAccount = accountRepo.findAccountByEmail(email);
        }
        if (existingAccount == null && StringUtils.hasText(phoneNumber)) {
            existingAccount = accountRepo.findAccountByPhoneNumber(phoneNumber);
        }

        if (existingAccount != null) {
            return this.convertToDto(existingAccount);
        }
        //创建用户
        return this.create(name, email, phoneNumber);
    }

    public AccountDto getAccountByPhoneNumber(String phoneNumber) {
        Account account = accountRepo.findAccountByPhoneNumber(phoneNumber);
        if (account == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "User with specified phonenumber not found");
        }
        return this.convertToDto(account);
    }

    public AccountDto create(String name, String email, String phoneNumber) {
        //检查是否存在用户
        if (StringUtils.hasText(email)) {
            Account foundAccount = accountRepo.findAccountByEmail(email);
            if (foundAccount != null) {
                throw new ServiceException("A user with that email already exists. Try a password reset");
            }
        }
        if (StringUtils.hasText(phoneNumber)) {
            Account foundAccount = accountRepo.findAccountByPhoneNumber(phoneNumber);
            if (foundAccount != null) {
                throw new ServiceException("A user with that phonenumber already exists. Try a password reset");
            }
        }
        //创建用户
        if (name == null) {
            name = "";
        }
        if (email == null) {
            email = "";
        }
        if (phoneNumber == null) {
            phoneNumber = "";
        }

        Account account = Account.builder()
                .email(email).name(name).phoneNumber(phoneNumber)
                .build();
        account.setPhotoUrl(Helper.generateGravatarUrl(account.getEmail()));
        account.setMemberSince(Instant.now());

        try {
            accountRepo.save(account);
        } catch (Exception ex) {
            String errMsg = "Could not create user account";
            serviceHelper.handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
        //异步操作
        serviceHelper.syncUserAsync(account.getId());

        if (StringUtils.hasText(email)) {

            String emailName = name;
            if (StringUtils.isEmpty(emailName)) {
                emailName = "there";
            }

            String subject = "Activate your Staffjoy account";
            this.sendEmail(account.getId(), email, emailName, subject, AccountConstant.ACTIVATE_ACCOUNT_TMPL, true);
        }

        // todo - sms onboarding (if worker??)

        //业务审计日志
        //业务审计日志生成后变为结构化日志

        LogEntry auditLog = LogEntry.builder()
                .authorization(AuthContext.getAuthz())
                .currentUserId(AuthContext.getUserId())
                .targetType("account")
                .targetId(account.getId())
                .updatedContents(account.toString())
                .build();

        logger.info("created account", auditLog);

        AccountDto accountDto = this.convertToDto(account);
        return accountDto;
    }

    public AccountList list(int offset, int limit) {
        if (limit <= 0) {
            limit = 10;
        }
        //分页
        Pageable pageRequest = PageRequest.of(offset, limit);
        Page<Account> accountPage = accountRepo.findAll(pageRequest);
        List<AccountDto> accountDtoList = accountPage.getContent().stream().map(account -> convertToDto(account)).collect(toList());

        return AccountList.builder()
                .limit(limit)
                .offset(offset)
                .accounts(accountDtoList)
                .build();
    }

    public AccountDto get(String userId) {
        Account account = accountRepo.findAccountById(userId);
        if (account == null) {
            throw new ServiceException(String.format("User with id %s not found", userId));
        }
        return this.convertToDto(account);
    }

    public AccountDto update(AccountDto newAccountDto) {
        Account newAccount = this.convertToModel(newAccountDto);
        Account existingAccount = accountRepo.findAccountById(newAccount.getId());
        //查询为空
        if (existingAccount == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, String.format("User with id %s not found", newAccount.getId()));
        }
        entityManager.detach(existingAccount);
        //超时
        if (!serviceHelper.isAlmostSameInstant(newAccount.getMemberSince(), existingAccount.getMemberSince())) {
            throw new ServiceException(ResultCode.REQ_REJECT, "You cannot modify the member_since date");
        }
        //user email已经存在
        if (StringUtils.hasText(newAccount.getEmail()) && !newAccount.getEmail().equals(existingAccount.getEmail())) {
            Account foundAccount = accountRepo.findAccountByEmail(newAccount.getEmail());
            if (foundAccount != null) {
                throw new ServiceException(ResultCode.REQ_REJECT, "A user with that email already exists. Try a password reset");
            }
        }
        //user phonenumber已经存在
        if (StringUtils.hasText(newAccount.getPhoneNumber()) && !newAccount.getPhoneNumber().equals(existingAccount.getPhoneNumber())) {
            Account foundAccount = accountRepo.findAccountByPhoneNumber(newAccount.getPhoneNumber());
            if (foundAccount != null) {
                throw new ServiceException(ResultCode.REQ_REJECT, "A user with that phonenumber already exists. Try a password reset");
            }
        }
        //权限不够
        if (AuthConstant.AUTHORIZATION_AUTHENTICATED_USER.equals(AuthContext.getAuthz())) {
            if (!existingAccount.isConfirmedAndActive() && newAccount.isConfirmedAndActive()) {
                throw new ServiceException(ResultCode.REQ_REJECT, "You cannot activate this account");
            }
            if (existingAccount.isSupport() != newAccount.isSupport()) {
                throw new ServiceException(ResultCode.REQ_REJECT, "You cannot change the support parameter");
            }
            if (!existingAccount.getPhotoUrl().equals(newAccount.getPhotoUrl())) {
                throw new ServiceException(ResultCode.REQ_REJECT, "You cannot change the photo through this endpoint (see docs)");
            }
            // User can request email change - not do it :-)
            if (!existingAccount.getEmail().equals(newAccount.getEmail())) {
                this.requestEmailChange(newAccount.getId(), newAccount.getEmail());
                // revert
                newAccount.setEmail(existingAccount.getEmail());
            }
        }

        newAccount.setPhotoUrl(Helper.generateGravatarUrl(newAccount.getEmail()));

        try {
            accountRepo.save(newAccount);
        } catch (Exception ex) {
            String errMsg = "Could not update the user account";
            serviceHelper.handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
        //异步操作
        serviceHelper.syncUserAsync(newAccount.getId());

        LogEntry auditLog = LogEntry.builder()
                .authorization(AuthContext.getAuthz())
                .currentUserId(AuthContext.getUserId())
                .targetType("account")
                .targetId(newAccount.getId())
                .originalContents(existingAccount.toString())
                .updatedContents(newAccount.toString())
                .build();

        logger.info("updated account", auditLog);

        // 如果account激活（activated） 并且 phonenumber被用户更改——send text
        if (newAccount.isConfirmedAndActive() &&
                StringUtils.hasText(newAccount.getPhoneNumber()) &&
                !newAccount.getPhoneNumber().equals(existingAccount.getPhoneNumber())) {
            serviceHelper.sendSmsGreeting(newAccount.getId());
        }

        this.trackEventWithAuthCheck("account_updated");

        AccountDto accountDto = this.convertToDto(newAccount);
        return accountDto;
    }

    public void updatePassword(String userId, String password) {
        String pwHash = passwordEncoder.encode(password);
        int affected = accountSecretRepo.updatePasswordHashById(pwHash, userId);
        // id为空
        if (affected != 1) {
            throw new ServiceException(ResultCode.NOT_FOUND, "user with specified id not found");
        }

        LogEntry auditLog = LogEntry.builder()
                .authorization(AuthContext.getAuthz())
                .currentUserId(AuthContext.getUserId())
                .targetType("account")
                .targetId(userId)
                .build();

        logger.info("updated password", auditLog);

        this.trackEventWithAuthCheck("password_updated");
    }

    /**
     * 验证密码
     *
     * @param email
     * @param password
     * @return
     */
    public AccountDto verifyPassword(String email, String password) {
        AccountSecret accountSecret = accountSecretRepo.findAccountSecretByEmail(email);
        // acount email为空
        if (accountSecret == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "account with specified email not found");
        }
        // 未激活
        if (!accountSecret.isConfirmedAndActive()) {
            throw new ServiceException(ResultCode.REQ_REJECT, "This user has not confirmed their account");
        }
        // 密码为空
        if (StringUtils.isEmpty(accountSecret.getPasswordHash())) {
            throw new ServiceException(ResultCode.REQ_REJECT, "This user has not set up their password");
        }
        // 密码不匹配
        if (!passwordEncoder.matches(password, accountSecret.getPasswordHash())) {
            throw new ServiceException(ResultCode.UN_AUTHORIZED, "Incorrect password");
        }
        // account为空
        Account account = accountRepo.findAccountById(accountSecret.getId());
        if (account == null) {
            throw new ServiceException(String.format("User with id %s not found", accountSecret.getId()));
        }

        // You shall pass
        AccountDto accountDto = this.convertToDto(account);
        return accountDto;
    }

    /**
     * 发送重置密码链接
     *
     * @param email
     */
    public void requestPasswordReset(String email) {
        String newEmail = email.toLowerCase().trim();

        Account account = accountRepo.findAccountByEmail(email);
        if (account == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, "No user with that email exists");
        }

        String subject = "Reset your Staffjoy password";
        boolean activate = false; // reset
        String tmpl = AccountConstant.RESET_PASSWORD_TMPL;
        //已经确认并且激活
        if (!account.isConfirmedAndActive()) {
            activate = true; // activate
            subject = "Activate your Staffjoy account";
            tmpl = AccountConstant.ACTIVATE_ACCOUNT_TMPL;
        }

        this.sendEmail(account.getId(), email, account.getName(), subject, tmpl, activate);
    }

    /**
     * 发送eamil确认链接
     *
     * @param userId
     * @param email
     */
    public void requestEmailChange(String userId, String email) {
        Account account = accountRepo.findAccountById(userId);
        if (account == null) {
            throw new ServiceException(ResultCode.NOT_FOUND, String.format("User with id %s not found", userId));
        }

        String subject = "Confirm Your New Email Address";
        this.sendEmail(account.getId(), email, account.getName(), subject, AccountConstant.CONFIRM_EMAIL_TMPL, true);
    }

    /**
     * 将帐户设置为activated并更新email，它是在用户点击email的确认链接后使用的。
     *
     * @param userId
     * @param email
     */
    public void changeEmailAndActivateAccount(String userId, String email) {

        int affected = accountRepo.updateEmailAndActivateById(email, userId);
        if (affected != 1) {
            throw new ServiceException(ResultCode.NOT_FOUND, "user with specified id not found");
        }
        //异步操作
        serviceHelper.syncUserAsync(userId);

        LogEntry auditLog = LogEntry.builder()
                .authorization(AuthContext.getAuthz())
                .currentUserId(AuthContext.getUserId())
                .targetType("account")
                .targetId(userId)
                .updatedContents(email)
                .build();

        logger.info("changed email", auditLog);

        this.trackEventWithAuthCheck("email_updated");
    }

    void sendEmail(String userId, String email, String name, String subject, String template, boolean activateOrConfirm) {
        String token = null;
        try {
            token = Sign.generateEmailConfirmationToken(userId, email, appProps.getSigningSecret());
        } catch (Exception ex) {
            String errMsg = "Could not create token";
            serviceHelper.handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        String pathFormat = "/activate/%s";
        if (!activateOrConfirm) {
            pathFormat = "/reset/%s";
        }
        String path = String.format(pathFormat, token);
        URI link = null;
        try {
            link = new URI("http", "www." + envConfig.getExternalApex(), path, null);
        } catch (URISyntaxException ex) {
            String errMsg = "Could not create activation url";
            if (!activateOrConfirm) {
                errMsg = "Could not create reset url";
            }
            serviceHelper.handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }

        String htmlBody = null;
        if (activateOrConfirm) { // active or confirm
            htmlBody = String.format(template, name, link.toString(), link.toString(), link.toString());
        } else { // reset
            htmlBody = String.format(template, link.toString(), link.toString());
        }

        EmailRequest emailRequest = EmailRequest.builder()
                .to(email)
                .name(name)
                .subject(subject)
                .htmlBody(htmlBody)
                .build();

        BaseResponse baseResponse = null;
        try {
            baseResponse = mailClient.send(emailRequest);
        } catch (Exception ex) {
            String errMsg = "Unable to send email";
            serviceHelper.handleException(logger, ex, errMsg);
            throw new ServiceException(errMsg, ex);
        }
        if (!baseResponse.isSuccess()) {
            serviceHelper.handleError(logger, baseResponse.getMessage());
            throw new ServiceException(baseResponse.getMessage());
        }
    }


    /**
     * 异步操作 和AccountService不能为同一个bean 所以用serviceHelper调用
     * 异步操作 线程上下文会切换 需要线程上下文拷贝
     *
     * @param userId
     * @param eventName
     */
    public void trackEvent(String userId, String eventName) {
        serviceHelper.trackEventAsync(userId, eventName);
    }

    public void syncUser(String userId) {
        serviceHelper.syncUserAsync(userId);
    }

    private AccountDto convertToDto(Account account) {
        return modelMapper.map(account, AccountDto.class);
    }

    private Account convertToModel(AccountDto accountDto) {
        return modelMapper.map(accountDto, Account.class);
    }

    private void trackEventWithAuthCheck(String eventName) {
        String userId = AuthContext.getUserId();
        if (StringUtils.isEmpty(userId)) {
            // Not an action performed by a normal user
            // (noop - not an view)
            return;
        }

        this.trackEvent(userId, eventName);
    }

}
