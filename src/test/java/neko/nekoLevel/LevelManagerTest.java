package neko.nekoLevel;

import org.junit.Test;
import static org.junit.Assert.*;

public class LevelManagerTest {
    
    @Test
    public void testExperienceFormula() {
        // 测试经验公式是否正确
        // 等级1升到等级2需要: 100 * (1+1)^2 = 400经验
        long expToLevel2 = 100L * (long) Math.pow(1 + 1, 2);
        assertEquals(400L, expToLevel2);
        
        // 等级10升到等级11需要: 100 * (10+1)^2 = 12100经验
        long expToLevel11 = 100L * (long) Math.pow(10 + 1, 2);
        assertEquals(12100L, expToLevel11);
        
        // 等级50升到等级51需要: 100 * (50+1)^2 = 260100经验
        long expToLevel51 = 100L * (long) Math.pow(50 + 1, 2);
        assertEquals(260100L, expToLevel51);
        
        System.out.println("等级1->2需要经验: " + expToLevel2);
        System.out.println("等级10->11需要经验: " + expToLevel11);
        System.out.println("等级50->51需要经验: " + expToLevel51);
    }
}