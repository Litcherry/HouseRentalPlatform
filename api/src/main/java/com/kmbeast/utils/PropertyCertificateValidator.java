package com.kmbeast.utils;

import java.util.regex.Pattern;

/**
 * 房产证号合法性校验工具类
 * 支持旧版《房屋所有权证》和新版《不动产权证书》两种格式
 */
public class PropertyCertificateValidator {

    /**
     * 旧版房产证号正则表达式
     * 格式：X房权证XXXX字第XXXXXXXXXX号
     * 示例：京房权证朝字第1234567890号
     * 粤房权证穗字第202412345678号
     */
    private static final Pattern OLD_PATTERN = Pattern.compile(
            "^[\u4e00-\u9fa5]房权证[\u4e00-\u9fa5A-Z0-9]{1,6}字第\\d{10,20}号$"
    );

    /**
     * 新版不动产权证书号正则表达式
     * 格式：28位编码 XXXX(区划代码6位) XXXXXX(地籍区/子区8位) XXXX(宗地特征码4位) F(用途1位) XXXXXXXX(顺序号9位)
     * 第1-6位：行政区划代码（数字）
     * 第7-14位：地籍区/地籍子区代码（数字）
     * 第15-18位：宗地特征码（数字或字母）
     * 第19位：用途代码，F代表房产
     * 第20-28位：顺序号（数字）
     */
    private static final Pattern NEW_PATTERN = Pattern.compile(
            "^\\d{6}\\d{8}[A-Z0-9]{4}F\\d{9}$"
    );

    /**
     * 省份简称列表（用于旧版房产证号校验）
     */
    private static final String[] PROVINCE_SHORT_NAMES = {
            "京", "沪", "津", "渝", "冀", "晋", "蒙", "辽", "吉", "黑",
            "苏", "浙", "皖", "闽", "赣", "鲁", "豫", "鄂", "湘", "粤",
            "桂", "琼", "川", "贵", "云", "藏", "陕", "甘", "青", "宁", "新"
    };

    /**
     * 验证房产证号是否合法
     *
     * @param certificateNo 房产证号
     * @return true-合法，false-不合法
     */
    public static boolean validate(String certificateNo) {
        if (certificateNo == null || certificateNo.trim().isEmpty()) {
            return false;
        }

        // 去除空格
        certificateNo = certificateNo.trim();

        // 先尝试新版格式校验
        if (certificateNo.length() == 28 && isNewFormat(certificateNo)) {
            return validateNewFormat(certificateNo);
        }

        // 再尝试旧版格式校验
        if (isOldFormat(certificateNo)) {
            return validateOldFormat(certificateNo);
        }

        return false;
    }

    /**
     * 判断是否为新版格式（28位数字+字母）
     */
    private static boolean isNewFormat(String certificateNo) {
        return certificateNo.length() == 28 && NEW_PATTERN.matcher(certificateNo).matches();
    }

    /**
     * 判断是否为旧版格式
     */
    private static boolean isOldFormat(String certificateNo) {
        return OLD_PATTERN.matcher(certificateNo).matches();
    }

    /**
     * 校验新版不动产权证书号（28位）
     *
     * @param certificateNo 房产证号
     * @return true-合法，false-不合法
     */
    private static boolean validateNewFormat(String certificateNo) {
        if (certificateNo.length() != 28) {
            return false;
        }

        // 使用正则表达式校验
        if (!NEW_PATTERN.matcher(certificateNo).matches()) {
            return false;
        }

        // 校验行政区划代码（第1-6位，必须是有效的行政区划代码）
        String regionCode = certificateNo.substring(0, 6);
        if (!isValidRegionCode(regionCode)) {
            return false;
        }

        // 校验用途代码（第19位必须是F，在字符串中索引为18）
        char purposeCode = certificateNo.charAt(18);
        if (purposeCode != 'F' && purposeCode != 'f') {
            return false;
        }

        return true;
    }

    /**
     * 校验旧版房屋所有权证号
     *
     * @param certificateNo 房产证号
     * @return true-合法，false-不合法
     */
    private static boolean validateOldFormat(String certificateNo) {
        // 基本格式已通过正则表达式校验
        // 进一步校验省份简称
        if (certificateNo.length() > 0) {
            String firstChar = certificateNo.substring(0, 1);
            // 检查是否为有效的省份简称
            boolean isValidProvince = false;
            for (String province : PROVINCE_SHORT_NAMES) {
                if (province.equals(firstChar)) {
                    isValidProvince = true;
                    break;
                }
            }
            if (!isValidProvince) {
                return false;
            }
        }

        // 提取数字部分（顺序号）进行校验
        // 格式：X房权证XXXX字第XXXXXXXXXX号
        // 找到"字第"和"号"之间的数字
        int startIndex = certificateNo.indexOf("字第");
        int endIndex = certificateNo.indexOf("号");
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex + 2) {
            String numberPart = certificateNo.substring(startIndex + 2, endIndex);
            // 顺序号应该在10-20位之间
            if (numberPart.length() < 10 || numberPart.length() > 20) {
                return false;
            }
            // 必须全部是数字
            if (!Pattern.matches("\\d+", numberPart)) {
                return false;
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * 校验行政区划代码（6位数字）
     * 基本校验：前2位为省份代码（11-65, 81-83），后4位为市、县代码
     *
     * @param regionCode 行政区划代码
     * @return true-合法，false-不合法
     */
    private static boolean isValidRegionCode(String regionCode) {
        if (regionCode == null || regionCode.length() != 6) {
            return false;
        }

        // 必须是6位数字
        if (!Pattern.matches("\\d{6}", regionCode)) {
            return false;
        }

        // 校验省份代码（前2位）
        String provinceCode = regionCode.substring(0, 2);
        int province = Integer.parseInt(provinceCode);

        // 省份代码范围：11-65, 81-83
        return (province >= 11 && province <= 65) || (province >= 81 && province <= 83);
    }

    /**
     * 判断房产证号格式类型
     *
     * @param certificateNo 房产证号
     * @return "NEW"表示新版，"OLD"表示旧版，"INVALID"表示无效
     */
    public static String getCertificateType(String certificateNo) {
        if (certificateNo == null || certificateNo.trim().isEmpty()) {
            return "INVALID";
        }

        certificateNo = certificateNo.trim();

        if (isNewFormat(certificateNo)) {
            return "NEW";
        } else if (isOldFormat(certificateNo)) {
            return "OLD";
        }

        return "INVALID";
    }

    /**
     * 格式化房产证号（去除空格）
     *
     * @param certificateNo 房产证号
     * @return 格式化后的房产证号
     */
    public static String format(String certificateNo) {
        if (certificateNo == null) {
            return null;
        }
        return certificateNo.trim();
    }

    /**
     * 测试方法 - 仅用于调试
     */
    public static void main(String[] args) {
        String testCert = "粤房权证穗字第202412345678号";
        System.out.println("测试房产证号: " + testCert);
        System.out.println("长度: " + testCert.length());
        System.out.println("是否为新版格式: " + isNewFormat(testCert));
        System.out.println("是否为旧版格式: " + isOldFormat(testCert));
        System.out.println("验证结果: " + validate(testCert));
        System.out.println("类型: " + getCertificateType(testCert));
    }
}
