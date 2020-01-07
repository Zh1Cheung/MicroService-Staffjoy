package xyz.staffjoy.common.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 *
 * Service是Staffjoy上的一个应用程序，运行在一个子域上
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 11:11 2019/12/29
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {
    private int security; // Public, Authenticated, or Admin
    private boolean restrictDev; // 如果为true，则在stage和prod中禁止服务
    private String backendDomain;  // 要查询的后端服务
    private boolean noCacheHtml; // 如果为true，则为HTML响应插入一个头，告诉浏览器不要缓存HTML
}
