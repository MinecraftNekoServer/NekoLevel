package neko.nekoLevel;

public class ExperienceCalculator {
    public static void main(String[] args) {
        System.out.println("升级经验需求计算 (修改后的公式: 100 * (等级+1)^2)");
        System.out.println("=====================================");
        
        // 测试不同等级的升级经验需求
        for (int level = 1; level <= 20; level++) {
            long expNeeded = 100L * (long) Math.pow(level + 1, 2);
            System.out.printf("等级 %2d -> %2d 需要经验: %,d%n", level, level + 1, expNeeded);
        }
        
        System.out.println("\n高级等级测试:");
        for (int level = 50; level <= 100; level += 10) {
            long expNeeded = 100L * (long) Math.pow(level + 1, 2);
            System.out.printf("等级 %2d -> %2d 需要经验: %,d%n", level, level + 1, expNeeded);
        }
        
        System.out.println("\n对比原公式 (线性: (等级+1) * 100):");
        System.out.printf("等级 1 -> 2 (原公式): %d, (新公式): %,d%n", (1+1) * 100, 100L * (long) Math.pow(1 + 1, 2));
        System.out.printf("等级 10 -> 11 (原公式): %d, (新公式): %,d%n", (10+1) * 100, 100L * (long) Math.pow(10 + 1, 2));
        System.out.printf("等级 20 -> 21 (原公式): %d, (新公式): %,d%n", (20+1) * 100, 100L * (long) Math.pow(20 + 1, 2));
    }
}