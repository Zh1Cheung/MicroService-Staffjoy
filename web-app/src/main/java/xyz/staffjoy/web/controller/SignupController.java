package xyz.staffjoy.web.controller;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import xyz.staffjoy.account.client.AccountClient;
import xyz.staffjoy.account.dto.AccountDto;
import xyz.staffjoy.account.dto.GenericAccountResponse;
import xyz.staffjoy.account.dto.CreateAccountRequest;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.web.service.HelperService;
import xyz.staffjoy.web.view.Constant;
import xyz.staffjoy.web.view.PageFactory;

/*
 *
 * 注册
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 11:51 2019/12/30
 *
 */
@Controller
public class SignupController {

    static final String SIGN_UP_REDIRECT_PATH = "redirect:/sign-up";

    static final ILogger logger = SLoggerFactory.getLogger(LoginController.class);

    @Autowired
    private PageFactory pageFactory;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private HelperService helperService;

    @PostMapping(value="/confirm")
    public String signUp(@RequestParam(value = "name", required = false) String name, @RequestParam("email") String email, Model model) {
        if (!StringUtils.hasText(email)) {
            return SIGN_UP_REDIRECT_PATH;
        }

        CreateAccountRequest request = CreateAccountRequest.builder()
                .name(name)
                .email(email)
                .build();

        GenericAccountResponse genericAccountResponse = null;
        try {
            // 创建新账户
            genericAccountResponse = accountClient.createAccount(AuthConstant.AUTHORIZATION_WWW_SERVICE, request);
        } catch (Exception ex) {
            String errMsg = "Failed to create account";
            helperService.logException(logger, ex, errMsg);
            return SIGN_UP_REDIRECT_PATH;
        }

        if (!genericAccountResponse.isSuccess()) {
            helperService.logError(logger, genericAccountResponse.getMessage());
            return SIGN_UP_REDIRECT_PATH;
        }
        // 获取账户
        AccountDto account = genericAccountResponse.getAccount();
        logger.info(String.format("New Account signup - %s", account));

        model.addAttribute(Constant.ATTRIBUTE_NAME_PAGE, pageFactory.buildConfirmPage());
        return Constant.VIEW_CONFIRM;
    }
}
