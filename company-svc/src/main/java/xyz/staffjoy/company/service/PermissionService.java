package xyz.staffjoy.company.service;

import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;
import io.sentry.SentryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xyz.staffjoy.common.auth.AuthContext;
import xyz.staffjoy.common.auth.PermissionDeniedException;
import xyz.staffjoy.company.model.Admin;
import xyz.staffjoy.company.model.Directory;
import xyz.staffjoy.company.model.Worker;
import xyz.staffjoy.company.repo.AdminRepo;
import xyz.staffjoy.company.repo.DirectoryRepo;
import xyz.staffjoy.company.repo.WorkerRepo;
import xyz.staffjoy.company.service.helper.ServiceHelper;


/*
 *
 * 权限检查器
 *
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 11:42 2019/12/29
 *
 */

@Service
public class PermissionService {

    static final ILogger logger = SLoggerFactory.getLogger(PermissionService.class);

    @Autowired
    private SentryClient sentryClient;

    @Autowired
    AdminRepo adminRepo;

    @Autowired
    WorkerRepo workerRepo;

    @Autowired
    DirectoryRepo directoryRepo;

    @Autowired
    ServiceHelper serviceHelper;

    /**
     * 检查当前用户是否是给定公司的管理员
     *
     * @param companyId
     */
    public void checkPermissionCompanyAdmin(String companyId) {
        String currentUserId = checkAndGetCurrentUserId();

        Admin admin = null;
        try {
            admin = adminRepo.findByCompanyIdAndUserId(companyId, currentUserId);
        } catch (Exception ex) {
            String errMsg = "failed to check company admin permissions";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (admin == null) {
            throw new PermissionDeniedException("you do not have admin access to this service");
        }
    }

    /**
     * 检查用户是某个公司某个团队的员工，还是该公司的管理员
     *
     * @param companyId
     * @param teamId
     */
    public void checkPermissionTeamWorker(String companyId, String teamId) {
        String currentUserId = checkAndGetCurrentUserId();

        // 检查是否为公司管理员
        try {
            Admin admin = adminRepo.findByCompanyIdAndUserId(companyId, currentUserId);
            if (admin != null) {
                return;
            }
        } catch (Exception ex) {
            String errMsg = "failed to check company admin permissions";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }

        Worker worker = null;
        try {
            worker = workerRepo.findByTeamIdAndUserId(teamId, currentUserId);
        } catch (Exception ex) {
            String errMsg = "failed to check teamDto member permissions";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (worker == null) {
            throw new PermissionDeniedException("you are not associated with this company");
        }
    }

    /**
     * 检查用户是否存在于公司的目录中。这是最低的安全级别。该用户可能不再与某个团队相关(即可能是以前的雇员)
     *
     * @param companyId
     */
    public void checkPermissionCompanyDirectory(String companyId) {
        String currentUserId = checkAndGetCurrentUserId();

        Directory directory = null;
        try {
            directory = directoryRepo.findByCompanyIdAndUserId(companyId, currentUserId);
        } catch (Exception ex) {
            String errMsg = "failed to check directory existence";
            serviceHelper.handleErrorAndThrowException(logger, ex, errMsg);
        }
        if (directory == null) {
            throw new PermissionDeniedException("you are not associated with this company");
        }
    }

    /**
     * 检查当前是否存在用户ID并返回当前用户ID
     *
     * @return
     */
    private String checkAndGetCurrentUserId() {
        String currentUserId = AuthContext.getUserId();
        if (StringUtils.isEmpty(currentUserId)) {
            String errMsg = "failed to find current user id";
            serviceHelper.handleErrorAndThrowException(logger, errMsg);
        }
        return currentUserId;
    }
}
