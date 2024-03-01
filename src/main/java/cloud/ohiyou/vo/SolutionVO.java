package cloud.ohiyou.vo;

/**
 * @author ohiyou
 * @since 2024/3/1 11:30
 */
public class SolutionVO {
    private String captchaId;
    private String captchaOutPut;
    private String genTime;
    private String lotNumber;
    private String passToken;
    private String riskType;

    public String getCaptchaId() {
        return captchaId;
    }

    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }

    public String getCaptchaOutPut() {
        return captchaOutPut;
    }

    public void setCaptchaOutPut(String captchaOutPut) {
        this.captchaOutPut = captchaOutPut;
    }

    public String getGenTime() {
        return genTime;
    }

    public void setGenTime(String genTime) {
        this.genTime = genTime;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getPassToken() {
        return passToken;
    }

    public void setPassToken(String passToken) {
        this.passToken = passToken;
    }

    public String getRiskType() {
        return riskType;
    }

    public void setRiskType(String riskType) {
        this.riskType = riskType;
    }
}
