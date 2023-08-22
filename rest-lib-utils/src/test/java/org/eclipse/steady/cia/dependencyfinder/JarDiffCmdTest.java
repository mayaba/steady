/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Copyright (c) 2018-2020 SAP SE or an SAP affiliate company and Eclipse Steady contributors
 */
package org.eclipse.steady.cia.dependencyfinder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.eclipse.steady.shared.json.JacksonUtil;
import org.eclipse.steady.shared.json.model.Artifact;
import org.eclipse.steady.shared.json.model.ConstructId;
import org.eclipse.steady.shared.json.model.diff.ClassDiffResult;
import org.eclipse.steady.shared.json.model.diff.JarDiffResult;
import org.junit.Test;

public class JarDiffCmdTest {

  @Test
  public void testDoProcessing() {
    Artifact old_doc = new Artifact("com.ibm.wala", "com.ibm.wala.core", "1.4.3");
    Artifact new_doc = new Artifact("com.ibm.wala", "com.ibm.wala.core", "1.6.2");

    Path old_jar = Paths.get("./src/test/resources/com.ibm.wala.core-1.4.3.jar");
    Path new_jar = Paths.get("./src/test/resources/com.ibm.wala.core-1.6.2.jar");

    JarDiffCmd cmd = new JarDiffCmd(old_doc, old_jar, new_doc, new_jar);

    try {
      final String[] args =
          new String[] {
            "-old", old_jar.toString(), "-new", new_jar.toString(), "-name", "xyz"
          };
      cmd.run(args);
    } catch (Exception e) {
      e.printStackTrace();
    }

    JarDiffResult result = cmd.getResult();

    // Loop over Deleted packages
    String targetPackageName = "com.ibm.wala.util.config";

    Set<ConstructId> deletedPackages = result.getDeletedPackages();
    if (deletedPackages != null) {
      for (ConstructId delPackage : deletedPackages) {
        String delPackageQname = delPackage.getQname();
        if (delPackageQname.equals(targetPackageName)) {
          System.out.println("[-] The package: {" + delPackageQname + "} is removed");
          int matchedCount = 0;

          Set<ConstructId> newPackages = result.getNewPackages();
          if (newPackages != null) {
            System.out.println("[+] Looking for potential alternative packages ...");

            for (ConstructId newPackage : newPackages) {
              String newPackageQname = newPackage.getQname();
              int distance = LevenshteinDistance.getDefaultInstance().apply(delPackageQname, newPackageQname);
              double similarity = 1.0 - (double) distance / Math.max(delPackageQname.length(), newPackageQname.length());
              if (similarity >= 0.8) { // Adjust the threshold as needed
                System.out.println("[-] The package: {" + delPackageQname + "} is potentially replaced with {" + newPackageQname + "}");
                ++matchedCount;
              }
            }

            // "com.ibm.wala.util.config" should only be matched one time (>80%)
            assertEquals(1, matchedCount);

          }
        }
      }
    }



    // Loop over modified classes
    String targetMethodName = "com.ibm.wala.ipa.callgraph.impl.Util.makeZeroCFABuilder(com.ibm.wala.ipa.callgraph.AnalysisOptions, com.ibm.wala.ipa.callgraph.IAnalysisCacheView, com.ibm.wala.ipa.cha.IClassHierarchy, com.ibm.wala.ipa.callgraph.AnalysisScope)";

    for (ClassDiffResult modifiedClass: result.getModifiedClasses()) {

      Collection<ConstructId> removedMethods = modifiedClass.getRemovedMethods();
      if (removedMethods != null) {
        for (ConstructId removedMethod : removedMethods) {
          // If there is a removed method, search in the current class for a different method with the same name (maybe it replaced the removed one)
          if (removedMethod.getQname().equals(targetMethodName)) {
            System.out.println("[-] The method: {" + targetMethodName + "} is removed");
            int matchedCount = 0;

            Collection<ConstructId> newMethods = modifiedClass.getNewMethods();
            if (newMethods != null) {
              System.out.println("[+] Looking for alternative (overloaded) methods ...");
              for (ConstructId newMethod: newMethods) {
                // Check if there is a method with the same visibility (access modifier), and returns the same type (overloaded method)
                if (newMethod.getQname().startsWith(targetMethodName.split("\\(")[0])
                        && newMethod.getAttributes().get("visibility").equals("public")) {
                  System.out.println("[+] Alternative method: " + newMethod.getQname());
                  ++matchedCount;
                }
              }
            }

            // "com.ibm.wala.ipa.callgraph.impl.Util.makeZeroCFABuilder" should have at least one match
            assertTrue(matchedCount > 0);

          }
        }
      }
    }

    // Loop over deleted classes

    // Loop over modified interfaces

    // Loop over deleted interfaces




//    String json = JacksonUtil.asJsonString(result);
//    System.out.println(json);
//    assertTrue(result != null);
  }
}