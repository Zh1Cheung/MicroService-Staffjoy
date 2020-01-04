package xyz.staffjoy.bot.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.staffjoy.account.dto.AccountDto;
import xyz.staffjoy.bot.BotConstant;
import xyz.staffjoy.bot.dto.*;
import xyz.staffjoy.common.api.ResultCode;
import xyz.staffjoy.common.auth.AuthConstant;
import xyz.staffjoy.common.error.ServiceException;
import xyz.staffjoy.company.client.CompanyClient;
import xyz.staffjoy.company.dto.*;

import javax.json.Json;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/*
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 11:01 2019/12/28
 *
 */

@SuppressWarnings("Duplicates")
@Service
public class AlertService {
    static final ILogger logger = SLoggerFactory.getLogger(AlertService.class);

    @Autowired
    private CompanyClient companyClient;

    @Autowired
    private HelperService helperService;

    @Autowired
    private SentryClient sentryClient;

    /**
     * 新班次提醒
     *
     * @param req
     */
    public void alertNewShift(AlertNewShiftRequest req) {
        String companyId = req.getNewShift().getCompanyId();
        String teamId = req.getNewShift().getTeamId();
        // 账户信息
        AccountDto account = helperService.getAccountById(req.getUserId());

        // 调度首选项
        DispatchPreference dispatchPreference = helperService.getPreferredDispatch(account);
        // 无
        if (dispatchPreference == DispatchPreference.DISPATCH_UNAVAILABLE) {
            return;
        }

        CompanyDto companyDto = helperService.getCompanyById(companyId);
        TeamDto teamDto = this.getTeamByCompanyIdAndTeamId(companyId, teamId);

        String newShiftMsg = this.printShiftSmsMsg(req.getNewShift(), teamDto.getTimezone());
        String jobName = this.getJobName(companyId, teamId, req.getNewShift().getJobId());

        if (!StringUtils.isEmpty(jobName)) {
            jobName = " " + jobName;
        }

        String greet = HelperService.getGreet(account.getName());
        String companyName = companyDto.getName();

        // email
        if (dispatchPreference == DispatchPreference.DISPATCH_EMAIL) {
            String htmlBody = String.format(BotConstant.ALERT_NEW_SHIFT_EMAIL_TEMPLATE,
                    greet, companyName, jobName, newShiftMsg);
            String subject = "New Shift Alert";
            String email = account.getEmail();
            String name = account.getName();

            helperService.sendMail(email, name, subject, htmlBody);
        } else { // sms

            String templateParam = Json.createObjectBuilder()
                    .add("greet", greet)
                    .add("company_name", companyName)
                    .add("job_name", jobName)
                    .add("shift_msg", newShiftMsg)
                    .build()
                    .toString();
            String phoneNumber = account.getPhoneNumber();

            // TODO crate sms template on aliyun then update constant
//        String msg = String.format("%s Your %s manager just published a new%s shift for you: \n%s",
////                greet, company.getName(), jobName, newShiftMsg);
            helperService.sendSms(phoneNumber, BotConstant.ALERT_NEW_SHIFT_SMS_TEMPLATE_CODE, templateParam);
        }
    }

    public void alertNewShifts(AlertNewShiftsRequest req) {
        // 班次列表
        List<ShiftDto> shiftDtos = req.getNewShifts();

        if (shiftDtos.size() == 0) {
            throw new ServiceException(ResultCode.PARAM_MISS, "empty shifts list in request");
        }
        // 更改的所有班次只对应一个company和team
        String companyId = shiftDtos.get(0).getCompanyId();
        String teamId = shiftDtos.get(0).getTeamId();
        // 账户信息
        AccountDto account = helperService.getAccountById(req.getUserId());
        // 同单个调度一样
        DispatchPreference dispatchPreference = helperService.getPreferredDispatch(account);

        if (dispatchPreference == DispatchPreference.DISPATCH_UNAVAILABLE) {
            return;
        }

        CompanyDto companyDto = helperService.getCompanyById(companyId);
        TeamDto teamDto = this.getTeamByCompanyIdAndTeamId(companyId, teamId);
        // 所有班次消息
        StringBuilder newShiftsMsg = new StringBuilder();
        String separator = (dispatchPreference == DispatchPreference.DISPATCH_SMS) ? "\n" : "<br/><br/>";
        for (ShiftDto shiftDto : shiftDtos) {
            String newShiftMsg = this.printShiftSmsMsg(shiftDto, teamDto.getTimezone());

            String jobName = this.getJobName(companyId, teamId, shiftDto.getJobId());

            if (!StringUtils.isEmpty(jobName)) {
                jobName = " " + jobName;
            }

            newShiftsMsg.append(String.format("%s%s%s", newShiftMsg, jobName, separator));
        }

        String greet = HelperService.getGreet(account.getName());
        String companyName = companyDto.getName();
        int numberOfShifts = shiftDtos.size();

        // Email
        if (dispatchPreference == DispatchPreference.DISPATCH_EMAIL) {
            String htmlBody = String.format(BotConstant.ALERT_NEW_SHIFTS_EMAIL_TEMPLATE,
                    greet, companyName, numberOfShifts, newShiftsMsg.toString());
            String subject = "New Shifts Alert";
            String email = account.getEmail();
            String name = account.getName();

            helperService.sendMail(email, name, subject, htmlBody);
        } else { // sms
            String templateParam = Json.createObjectBuilder()
                    .add("greet", greet)
                    .add("company_name", companyName)
                    .add("shifts_size", numberOfShifts)
                    .add("shifts_msg", newShiftsMsg.toString())
                    .build()
                    .toString();
            String phoneNumber = account.getPhoneNumber();

            // TODO crate sms template on aliyun then update constant
//        String msg = String.format("%s Your %s manager just published %d new shifts that you are working: \n%s",
//                greet, companyDto.getName(), shifts.size(), newShiftsMsg);

            helperService.sendSms(phoneNumber, BotConstant.ALERT_NEW_SHIFTS_SMS_TEMPLATE_CODE, templateParam);
        }
    }

    /**
     * 移除班次提醒
     *
     * @param req
     */
    public void alertRemovedShift(AlertRemovedShiftRequest req) {
        String companyId = req.getOldShift().getCompanyId();
        String teamId = req.getOldShift().getTeamId();

        AccountDto account = helperService.getAccountById(req.getUserId());

        DispatchPreference dispatchPreference = helperService.getPreferredDispatch(account);
        if (dispatchPreference == DispatchPreference.DISPATCH_UNAVAILABLE) {
            return;
        }

        CompanyDto companyDto = helperService.getCompanyById(companyId);
        TeamDto teamDto = this.getTeamByCompanyIdAndTeamId(companyId, teamId);

        WorkerShiftListRequest workerShiftListRequest = WorkerShiftListRequest.builder()
                .companyId(companyId)
                .teamId(teamId)
                .workerId(req.getUserId())
                .shiftStartAfter(Instant.now())
                .shiftStartBefore(Instant.now().plus(BotConstant.SHIFT_WINDOW, ChronoUnit.DAYS))
                .build();
        ShiftList shiftList = this.listWorkerShifts(workerShiftListRequest);

        String newShiftsMsg = "";
        String separator = (dispatchPreference == DispatchPreference.DISPATCH_SMS) ? "\n" : "<br/><br/>";
        for (ShiftDto shiftDto : shiftList.getShifts()) {
            String newShiftMsg = this.printShiftSmsMsg(shiftDto, teamDto.getTimezone());
            newShiftsMsg += String.format("%s%s", newShiftMsg, separator);
        }

        String greet = HelperService.getGreet(account.getName());
        String companyName = companyDto.getName();

        if (dispatchPreference == DispatchPreference.DISPATCH_EMAIL) {
            String htmlBody = String.format(BotConstant.ALERT_REMOVED_SHIFT_EMAIL_TEMPLATE,
                    greet, companyName, newShiftsMsg);
            String subject = "Removed Shift Alert";
            String email = account.getEmail();
            String name = account.getName();

            helperService.sendMail(email, name, subject, htmlBody);
        } else {
            // TODO crate sms template on aliyun then update constant
//        String msg = String.format("%s Your %s manager just removed you from a shift, so you are no longer working on it. Here is your new schedule: \n%s",
//                greet, company.getName(), newShiftsMsg);
            String templateParam = Json.createObjectBuilder()
                    .add("greet", greet)
                    .add("company_name", companyDto.getName())
                    .add("shifts_msg", newShiftsMsg)
                    .build()
                    .toString();
            String phoneNumber = account.getPhoneNumber();

            helperService.sendSms(phoneNumber, BotConstant.ALERT_REMOVED_SHIFT_SMS_TEMPLATE_CODE, templateParam);
        }
    }

    public void alertRemovedShifts(AlertRemovedShiftsRequest req) {
        List<ShiftDto> shiftDtos = req.getOldShifts();
        if (shiftDtos.size() == 0) {
            throw new ServiceException(ResultCode.PARAM_MISS, "empty shifts list in request");
        }

        String companyId = shiftDtos.get(0).getCompanyId();
        String teamId = shiftDtos.get(0).getTeamId();
        // 账户信息
        AccountDto account = helperService.getAccountById(req.getUserId());
        DispatchPreference dispatchPreference = helperService.getPreferredDispatch(account);

        if (dispatchPreference == DispatchPreference.DISPATCH_UNAVAILABLE) {
            return;
        }

        CompanyDto companyDto = helperService.getCompanyById(companyId);
        TeamDto teamDto = this.getTeamByCompanyIdAndTeamId(companyId, teamId);

        // 雇员班次请求列表构建
        WorkerShiftListRequest workerShiftListRequest = WorkerShiftListRequest.builder()
                .companyId(companyId)
                .teamId(teamId)
                .workerId(req.getUserId())
                .shiftStartAfter(Instant.now())
                .shiftStartBefore(Instant.now().plus(BotConstant.SHIFT_WINDOW, ChronoUnit.DAYS))
                .build();
        // 班次列表
        ShiftList shiftList = this.listWorkerShifts(workerShiftListRequest);

        String newShiftsMsg = "";
        String separator = (dispatchPreference == DispatchPreference.DISPATCH_SMS) ? "\n" : "<br/><br/>";
        for (ShiftDto shiftDto : shiftList.getShifts()) {
            String newShiftMsg = this.printShiftSmsMsg(shiftDto, teamDto.getTimezone());
            newShiftsMsg += String.format("%s%s", newShiftMsg, separator);
        }

        String greet = HelperService.getGreet(account.getName());
        String companyName = companyDto.getName();
        int numberOfShifts = shiftDtos.size();

        if (dispatchPreference == DispatchPreference.DISPATCH_EMAIL) {
            String htmlBody = String.format(BotConstant.ALERT_REMOVED_SHIFTS_EMAIL_TEMPLATE,
                    greet, companyName, numberOfShifts, newShiftsMsg);
            String subject = "Removed Shifts Alert";
            String email = account.getEmail();
            String name = account.getName();

            helperService.sendMail(email, name, subject, htmlBody);
        } else { // sms

            // TODO create sms template then update code
//        String msg = String.format("%s Your %s manager just removed %d of your shifts so you are no longer working on it. \n Your new shifts are: \n%s",
//                greet, companyDto.getName(), shifts.size() newShiftsMsg);
            String templateParam = Json.createObjectBuilder()
                    .add("greet", greet)
                    .add("company_name", companyName)
                    .add("shifts_size", numberOfShifts)
                    .add("shifts_msg", newShiftsMsg)
                    .build()
                    .toString();
            String phoneNumber = account.getPhoneNumber();

            helperService.sendSms(phoneNumber, BotConstant.ALERT_REMOVED_SHIFTS_SMS_TEMPLATE_CODE, templateParam);
        }
    }

    /**
     * 更改班次提醒
     *
     * @param req
     */
    public void alertChangedShift(AlertChangedShiftRequest req) {
        String companyId = req.getOldShift().getCompanyId();
        String teamId = req.getOldShift().getTeamId();

        AccountDto account = helperService.getAccountById(req.getUserId());
        DispatchPreference dispatchPreference = helperService.getPreferredDispatch(account);

        if (dispatchPreference == DispatchPreference.DISPATCH_UNAVAILABLE) {
            return;
        }
        CompanyDto companyDto = helperService.getCompanyById(companyId);
        TeamDto teamDto = this.getTeamByCompanyIdAndTeamId(companyId, teamId);

        String oldShiftMsg = this.printShiftSmsMsg(req.getOldShift(), teamDto.getTimezone());
        String oldJobName = this.getJobName(companyId, teamId, req.getNewShift().getJobId());

        if (!StringUtils.isEmpty(oldJobName)) {
            oldShiftMsg += String.format(" (%s)", oldJobName);
        }

        String newShiftMsg = this.printShiftSmsMsg(req.getNewShift(), teamDto.getTimezone());
        String newJobName = this.getJobName(companyId, teamId, req.getOldShift().getJobId());

        if (!StringUtils.isEmpty(newJobName)) {
            newShiftMsg += String.format(" (%s)", newJobName);
        }

        String greet = HelperService.getGreet(account.getName());
        String companyName = companyDto.getName();

        if (dispatchPreference == DispatchPreference.DISPATCH_EMAIL) {
            String htmlBody = String.format(BotConstant.ALERT_CHANGED_SHIFT_EMAIL_TEMPLATE,
                    greet, companyName, oldShiftMsg, newShiftMsg);
            String subject = "Changed Shift Alert";
            String email = account.getEmail();
            String name = account.getName();

            helperService.sendMail(email, name, subject, htmlBody);
        } else { // sms

            // TODO create sms template on aliyun then update constant
//        String msg = String.format("%s Your %s manager just changed your shift: \nOld: %s\nNew:%s",
//                greet, companyDto.getName(), oldShiftMsg, newShiftMsg);
            String templateParam = Json.createObjectBuilder()
                    .add("greet", greet)
                    .add("company_name", companyName)
                    .add("old_shift_msg", oldShiftMsg)
                    .add("new_shift_msg", newShiftMsg)
                    .build()
                    .toString();
            String phoneNumber = account.getPhoneNumber();

            helperService.sendSms(phoneNumber, BotConstant.ALERT_CHANGED_SHIFT_SMS_TEMPLATE_CODE, templateParam);
        }
    }

    /**
     * 获取班次列表
     *
     * @param workerShiftListRequest
     * @return
     */
    private ShiftList listWorkerShifts(WorkerShiftListRequest workerShiftListRequest) {
        GenericShiftListResponse shiftListResponse = null;
        try {
            // 班次列表基本信息
            shiftListResponse = companyClient.listWorkerShifts(AuthConstant.AUTHORIZATION_BOT_SERVICE, workerShiftListRequest);
        } catch (Exception ex) {
            String errMsg = "fail to list worker shifts";
            logger.error(errMsg, ex);
            sentryClient.sendException(ex);
            throw new ServiceException(errMsg, ex);
        }
        if (!shiftListResponse.isSuccess()) {
            logger.error(shiftListResponse.getMessage());
            sentryClient.sendMessage(shiftListResponse.getMessage());
            throw new ServiceException(shiftListResponse.getMessage());
        }
        // 获取班次列表
        ShiftList shiftList = shiftListResponse.getShiftList();
        return shiftList;
    }

    /**
     * 获取团队信息
     *
     * @param companyId
     * @param teamId
     * @return
     */
    private TeamDto getTeamByCompanyIdAndTeamId(String companyId, String teamId) {
        GenericTeamResponse teamResponse = null;
        try {
            teamResponse = companyClient.getTeam(AuthConstant.AUTHORIZATION_BOT_SERVICE, companyId, teamId);
        } catch (Exception ex) {
            String errMsg = "fail to get team";
            logger.error(errMsg, ex);
            sentryClient.sendException(ex);
            throw new ServiceException(errMsg, ex);
        }
        if (!teamResponse.isSuccess()) {
            logger.error(teamResponse.getMessage());
            sentryClient.sendMessage(teamResponse.getMessage());
            throw new ServiceException(teamResponse.getMessage());
        }
        TeamDto team = teamResponse.getTeam();
        return team;
    }

    private String printShiftSmsMsg(ShiftDto shiftDto, String tz) {
        DateTimeFormatter startTimeFormatter = DateTimeFormatter.ofPattern(BotConstant.SMS_START_TIME_FORMAT)
                .withZone(ZoneId.of(tz));

        DateTimeFormatter endTimeFormatter = DateTimeFormatter.ofPattern(BotConstant.SMS_STOP_TIME_FORMAT)
                .withZone(ZoneId.of(tz));

        String startTime = startTimeFormatter.format(shiftDto.getStart());
        String endTime = endTimeFormatter.format(shiftDto.getStop());

        return String.format(BotConstant.SMS_SHIFT_FORMAT, startTime, endTime);
    }

    private String getJobName(String companyId, String teamId, String jobId) {
        if (StringUtils.isEmpty(jobId)) {
            return "";
        }

        GenericJobResponse jobResponse = null;
        try {
            jobResponse = companyClient.getJob(AuthConstant.AUTHORIZATION_BOT_SERVICE, jobId, companyId, teamId);
        } catch (Exception ex) {
            String errMsg = "fail to get job";
            logger.error(errMsg, ex);
            sentryClient.sendException(ex);
            throw new ServiceException(errMsg, ex);
        }
        if (!jobResponse.isSuccess()) {
            logger.error(jobResponse.getMessage());
            sentryClient.sendMessage(jobResponse.getMessage());
            throw new ServiceException(jobResponse.getMessage());
        }
        JobDto jobDto = jobResponse.getJob();
        return jobDto.getName();
    }
}
