package cloud.ohiyou.vo;

import java.util.List;

/**
 * @author ohiyou
 * @since 2024/3/1 9:33
 */
public class CrackCaptchaTaskVO {
    private String type;
    private String websiteURL;
    private String captchaId;
    private String geetestApiServerSubdomain;
    private String proxy;
    private List<Cookie> cookies;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWebsiteURL() {
        return websiteURL;
    }

    public void setWebsiteURL(String websiteURL) {
        this.websiteURL = websiteURL;
    }

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getGeetestApiServerSubdomain() {
        return geetestApiServerSubdomain;
    }

    public void setGeetestApiServerSubdomain(String geetestApiServerSubdomain) {
        this.geetestApiServerSubdomain = geetestApiServerSubdomain;
    }

    public String getProxy() {
        return proxy;
    }

    public void setProxy(String proxy) {
        this.proxy = proxy;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }
}
