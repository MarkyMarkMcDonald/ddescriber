package com.karateca.ddescriber;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.karateca.ddescriber.model.TestFindResult;
import com.karateca.ddescriber.model.TestState;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActionUtil {

  /**
   * Run a read operation.
   *
   * @param action The action to run.
   */
  public static void runReadAction(Runnable action) {
    ApplicationManager.getApplication().runReadAction(action);
  }

  public static Document getDocument(VirtualFile virtualFile) {
    FileDocumentManager instance = FileDocumentManager.getInstance();
    return instance.getDocument(virtualFile);
  }

  /**
   * Include, exclude, or rollback the selected tests.
   *
   * @param project The current project.
   * @param document The document to modify.
   * @param testList The tests that have to change.
   * @param jasmineSyntax Whether to use ddescribe of fdescribe.
   */
  public static void changeTestList(
      Project project,
      final Document document,
      final List<TestFindResult> testList,
      final JasmineSyntax jasmineSyntax) {
    // Change the test from the bottom up to avoid shifting the offsets.
    Collections.sort(testList, new Comparator<TestFindResult>() {
      @Override
      public int compare(TestFindResult left, TestFindResult right) {
        return right.getEndOffset() - left.getStartOffset();
      }
    });

    runWriteActionInsideCommand(project, new Runnable() {
      @Override
      public void run() {
        for (TestFindResult testFindResult : testList) {
          changeSelectedLine(document, testFindResult, jasmineSyntax);
        }
      }
    });
  }

  /**
   * Run a write operation within a command.
   *
   * @param project The current project.
   * @param action The action to run.
   */
  private static void runWriteActionInsideCommand(Project project, final Runnable action) {
    CommandProcessor.getInstance().executeCommand(project, new Runnable() {
      @Override
      public void run() {
        ApplicationManager.getApplication().runWriteAction(action);
      }
    }, "Add / Remove tests", null);
  }

  /**
   * Perform the replace for the selected line. It will add or remove a "d" from describe() and an
   * "i" form it().
   * @param document The document you want to change.
   * @param test The line that has to change.
   * @param jasmineSyntax Whether to use ddescribe of fdescribe.
   */
  private static void changeSelectedLine(Document document,
      TestFindResult test,
      JasmineSyntax jasmineSyntax) {
    String newText = getReplaceStringValue(test, jasmineSyntax);

    int start = test.getStartOffset();
    int end = test.getEndOffset();

    document.replaceString(start, end, newText);
  }

  private static String getReplaceStringValue(TestFindResult test,
      JasmineSyntax jasmineSyntax) {
    TestState newState = test.getPendingChangeState();

    if (newState == TestState.Excluded) {
      return test.isDescribe() ? "xdescribe(" : "xit(";
    }

    if (newState == TestState.Included) {
      return test.isDescribe() ?
          jasmineSyntax.getIncludedDescribe() + "(" :
          jasmineSyntax.getIncludedit() + "(";
    }

    // Rollback to original state.
    return test.isDescribe() ? "describe(" : "it(";
  }
}
