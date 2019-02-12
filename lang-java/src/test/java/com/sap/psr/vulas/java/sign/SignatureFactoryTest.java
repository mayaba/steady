package com.sap.psr.vulas.java.sign;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import com.sap.psr.vulas.shared.enums.ConstructType;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.model.ConstructId;


public class SignatureFactoryTest {

    @Test
    public void createSignature() throws Exception {
    	JavaSignatureFactory f = new JavaSignatureFactory();
    	ASTConstructBodySignature s = (ASTConstructBodySignature) f.createSignature(new ConstructId(ProgrammingLanguage.JAVA, ConstructType.METH, "org.apache.catalina.filters.CorsFilter.handleSimpleCORS(HttpServletRequest,HttpServletResponse,FilterChain)"), new File("./src/test/resources/CorsFilter.java"));
    	assertTrue(s.toJson().length()>0);
    	
    }
}
