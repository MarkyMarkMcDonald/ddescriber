package com.karateca.ddescriber.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.intellij.mock.Mock;
import com.intellij.openapi.vfs.VirtualFile;
import com.karateca.ddescriber.BaseTestCase;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * @author andresdom@google.com (Andres Dominguez)
 */
public class JasmineTreeTest extends BaseTestCase {

  private JasmineTree jasmineTree;

  public void setUp() throws Exception {
    super.setUp();
    jasmineTree = new JasmineTree();
  }

  private JasmineFile createJasmineFile(boolean hasTestsMarkedToRun) {
    return createJasmineFile(hasTestsMarkedToRun, null);
  }

  private JasmineFile createJasmineFile(boolean hasTestsMarkedToRun, VirtualFile virtualFile) {
    TreeNode describe = createDescribe();

    describe.setVirtualFile(virtualFile);

    JasmineFile jasmineFile = mock(JasmineFile.class);

    when(jasmineFile.hasTestsMarkedToRun()).thenReturn(hasTestsMarkedToRun);
    when(jasmineFile.getTreeNode()).thenReturn(describe);
    when(jasmineFile.getVirtualFile()).thenReturn(virtualFile);

    return jasmineFile;
  }

  private TreeNode createDescribe() {
    TestFindResult descFindResult = MockFindResult.buildDescribe("d1");

    TreeNode describeNode = new TreeNode(descFindResult);
    describeNode.add(buildIt("it1"));
    describeNode.add(buildIt("it2"));

    return describeNode;
  }

  private TreeNode buildIt(String testText) {
    return new TreeNode(MockFindResult.buildIt(testText));
  }

  public void testShouldDeclareEmptyRoot() {
    assertEquals("Root node", jasmineTree.getRootNode().getUserObject());
  }

  public void testShouldHideRootNodeAfterAddingFiles() {
    // When you add files.
    jasmineTree.addFiles(new ArrayList<JasmineFile>());

    // Then ensure the root is not visible.
    assertFalse(jasmineTree.isRootVisible());
  }

  public void testShouldAddTestsToEmptyTree() {
    // Given a describe with two its.
    JasmineFile jasmineFile = createJasmineFile(false);

    // When you add the jasmine file.
    jasmineTree.addFiles(Arrays.asList(jasmineFile));

    // Then ensure the tree has the new nodes.
    TreeNode rootNode = jasmineTree.getRootNode();
    assertEquals(1, rootNode.getChildCount());

    // Ensure the describe node was added.
    TreeNode describeNode = (TreeNode) rootNode.getFirstChild();
    assertEquals("d1", describeNode.getNodeValue().getTestText());

    // Ensure the describe has two children.
    assertEquals(2, describeNode.getChildCount());
  }

  public void testShouldAddJasmineFileWhenItHasResultsMarkedToRun() {
    // Given a describe with tests marked to run.
    JasmineFile jasmineFile = createJasmineFile(true);

    // When you update the jasmine file.
    jasmineTree.updateFile(jasmineFile);

    // Then ensure the file was added.
    expectRootNodeContainsDescribeWithName("d1");
  }

  public void testShouldNotAddJasmineFileWhenItHasNoTestsMarkedToRun() {
    // Given a test file without marked tests.
    JasmineFile jasmineFile = createJasmineFile(false);

    // When you update the file.
    jasmineTree.updateFile(jasmineFile);

    // Then ensure the file was not added.
    assertEquals(0, jasmineTree.getRootNode().getChildCount());
  }

  public void testShouldRemoveExistingTestFileWhenThereAreNoTestsMarked() {
    VirtualFile virtualFile = new Mock.MyVirtualFile();

    // Given that you are showing a jasmine file.
    jasmineTree.updateFile(createJasmineFile(true, virtualFile));
    assertEquals(1, jasmineTree.getRootNode().getChildCount());

    // When you update the same file
    jasmineTree.updateFile(createJasmineFile(false, virtualFile));

    // Then ensure the node was removed.
    assertEquals(0, jasmineTree.getRootNode().getChildCount());
  }

  public void testShouldUpdateExistingTest() {
    prepareScenarioWithTestFile("jasmineTestBefore.js");

    // Given that you are showing a jasmine file.
    jasmineTree.updateFile(createJasmineFile(true, virtualFile));
    assertEquals(1, jasmineTree.getRootNode().getChildCount());

    // When you update the file.
    JasmineFile updatedFile = new JasmineFileImpl(getProject(), virtualFile);
    updatedFile.buildTreeNodeSync();
    jasmineTree.updateFile(updatedFile);

    // Then ensure the node was updated.
    expectRootNodeContainsDescribeWithName("top describe");
  }

  private void expectRootNodeContainsDescribeWithName(String expectedName) {
    TreeNode rootNode = jasmineTree.getRootNode();
    TreeNode firstChild = (TreeNode) rootNode.getFirstChild();

    assertEquals(1, rootNode.getChildCount());
    assertEquals(expectedName, firstChild.getNodeValue().getTestText());
  }
}
