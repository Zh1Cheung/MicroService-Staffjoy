package xyz.staffjoy.web.view;

import lombok.*;

/*
 *
 * 普通Page
 * @Author:Zh1Cheung 945503088@qq.com
 * @Date: 10:17 2019/12/30
 *
 */
@Getter
@Setter
@Builder
public class Page {
    private String title;
    private String description;
    private String templateName;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCssId() {
        return cssId;
    }

    public void setCssId(String cssId) {
        this.cssId = cssId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private String cssId;
    @Builder.Default
    private String version = "3.0";
}
