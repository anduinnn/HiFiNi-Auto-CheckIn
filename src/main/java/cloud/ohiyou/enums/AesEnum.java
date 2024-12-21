package cloud.ohiyou.enums;

import jdk.nashorn.internal.objects.annotations.Getter;

public enum AesEnum {
    /**
     * AES加密
     */
    AES_OFB("0","AES-OFB"),
    AES_CTR("5","AES-CTR"),
    AES_CBC("27","AES_CBC"),
    AES_GCM("20","AES_GCM"),;
    String code;
    String AES_MODE;

    public String getCode() {
        return code;
    }

    public String getAES_MODE() {
        return AES_MODE;
    }

    AesEnum(String code, String AES_MODE) {
        this.code = code;
        this.AES_MODE = AES_MODE;
    }
}
