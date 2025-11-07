package neko.nekoLevel;

public class ExperienceFormatter {
    
    /**
     * 将经验数值格式化为K/W格式显示（当前经验/升级所需经验）
     * 
     * @param currentExperience 当前经验数值
     * @param experienceToNextLevel 升级到下一级所需经验数值
     * @return 格式化后的字符串 (如 "3.8K/3.9K" 或 "1.2W/1.3W")
     */
    public static String formatExperience(long currentExperience, long experienceToNextLevel) {
        String currentFormatted = formatSingleValue(currentExperience);
        String targetFormatted = formatSingleValue(experienceToNextLevel);
        return currentFormatted + "/" + targetFormatted;
    }
    
    /**
     * 将经验数值格式化为K/W格式显示（单个值，用于PlaceholderAPI等不需要对比的场景）
     * 
     * @param experience 经验数值
     * @return 格式化后的字符串
     */
    public static String formatExperience(long experience) {
        String formatted = formatSingleValue(experience);
        return formatted;
    }
    
    /**
     * 格式化单个数值为K或W格式
     * 
     * @param value 要格式化的数值
     * @return 格式化后的字符串
     */
    private static String formatSingleValue(long value) {
        if (value >= 10000) {
            // 处理大于等于10000的数值，使用W单位
            double v = value / 10000.0;
            return formatDouble(v) + "W";
        } else if (value >= 1000) {
            // 处理大于等于1000的数值，使用K单位
            double v = value / 1000.0;
            return formatDouble(v) + "K";
        } else {
            return String.valueOf(value);
        }
    }
    
    /**
     * 格式化小数值，保留一位小数但去掉末尾的0
     * 
     * @param value 要格式化的数值
     * @return 格式化后的字符串
     */
    private static String formatDouble(double value) {
        // 如果是整数，不显示小数点
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        } else {
            // 保留一位小数，但去掉末尾的0
            String formatted = String.format("%.1f", value);
            if (formatted.endsWith("0")) {
                return formatted.substring(0, formatted.length() - 2);
            } else {
                return formatted;
            }
        }
    }
}