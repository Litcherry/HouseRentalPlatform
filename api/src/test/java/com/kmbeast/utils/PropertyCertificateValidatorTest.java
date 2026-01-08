package com.kmbeast.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class PropertyCertificateValidatorTest {

    @Test
    public void testNewFormat() {
        // Valid 28-digit number
        // 110101 (District) + 12345678 (Cadastral) + 0001 (Feature) + F (Usage) + 123456789 (Seq)
        String validNew = "110101123456780001F123456789";
        assertTrue("Should accept valid 28-digit new format", 
            PropertyCertificateValidator.validate(validNew));
            
        // Invalid length
        String invalidLength = "110101123456780001F12345678"; // 27 digits
        assertFalse("Should reject invalid length", 
            PropertyCertificateValidator.validate(invalidLength));
            
        // Invalid usage code (not F)
        String invalidUsage = "110101123456780001A123456789";
        assertFalse("Should reject invalid usage code", 
            PropertyCertificateValidator.validate(invalidUsage));
    }

    @Test
    public void testOldFormat() {
        // Valid old format examples
        // Format: X房权证XXXX字第XXXXXXXXXX号
        // Regex: ^[\u4e00-\u9fa5]房权证[\u4e00-\u9fa5A-Z0-9]{1,6}字第\d{10,20}号$
        
        String validOld1 = "京房权证朝字第1234567890号";
        assertTrue("Should accept valid old format: " + validOld1, 
            PropertyCertificateValidator.validate(validOld1));
            
        String validOld2 = "苏房权证宁字第2023000001号";
        assertTrue("Should accept valid old format: " + validOld2, 
            PropertyCertificateValidator.validate(validOld2));
            
        // Invalid old format
        String invalidOld1 = "京房权证字第1234567890号"; // Missing district part
        assertFalse("Should reject missing district part", 
            PropertyCertificateValidator.validate(invalidOld1));
            
        String invalidOld2 = "京房权证朝字第号"; // Missing number
        assertFalse("Should reject missing number", 
            PropertyCertificateValidator.validate(invalidOld2));
    }
    
    @Test
    public void testNullOrEmpty() {
        assertFalse("Should reject null", PropertyCertificateValidator.validate(null));
        assertFalse("Should reject empty string", PropertyCertificateValidator.validate(""));
        assertFalse("Should reject whitespace", PropertyCertificateValidator.validate("   "));
    }
}
