package com.karateca.ddescriber.toolWindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.SpeedSearchComparator;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.karateca.ddescriber.ActionUtil;
import com.karateca.ddescriber.Hierarchy;
import com.karateca.ddescriber.dialog.CustomTreeCellRenderer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.util.List;

/**
 * @author Andres Dominguez.
 */
public class JasmineToolWindow implements ToolWindowFactory {

  private ToolWindow toolWindow;
  private Project project;

  @Override
  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    this.toolWindow = toolWindow;
    this.project = project;
    findAllFilesContainingTests();
  }

  private void findAllFilesContainingTests() {
    ApplicationManager.getApplication().runReadAction(new Runnable() {
      @Override
      public void run() {
        FileIterator fileIterator = new FileIterator(project);
        ProjectRootManager.getInstance(project).getFileIndex().iterateContent(fileIterator);
        List<JasmineFile> jasminFiles = fileIterator.getJasmineFiles();
        showTestsInToolWindow(jasminFiles);
      }
    });
  }

  private void showTestsInToolWindow(List<JasmineFile> jasminFiles) {

    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gridBagConstraintsScrollPane = new GridBagConstraints();
    gridBagConstraintsScrollPane.gridx = 0;
    gridBagConstraintsScrollPane.gridy = 1;
    gridBagConstraintsScrollPane.gridwidth = 10;
    gridBagConstraintsScrollPane.gridheight = 10;
    gridBagConstraintsScrollPane.fill = GridBagConstraints.BOTH;
    gridBagConstraintsScrollPane.anchor = GridBagConstraints.CENTER;
    gridBagConstraintsScrollPane.weightx = 1;
    gridBagConstraintsScrollPane.weighty = 10;
    panel.add(createCenterPanel(jasminFiles), gridBagConstraintsScrollPane);

    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(panel, "", false);
    toolWindow.getContentManager().addContent(content);
  }

  private JComponent createCenterPanel(List<JasmineFile> jasminFiles) {
    // The root node is hidden.
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("All tests");
    Tree tree = new Tree(root);

    for (JasmineFile jasminFile : jasminFiles) {
//      Hierarchy hierarchy = jasminFile.createHierarchy();
//      String fileName = jasminFile.getVirtualFile().getName();
//      DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileName);
//      DefaultMutableTreeNode node = ActionUtil.populateTree(hierarchy.getAllUnitTests());
//      fileNode.add(node);
//
//      root.add(fileNode);
    }

    tree.setCellRenderer(new CustomTreeCellRenderer());

    // Add search, make it case insensitive.
    new TreeSpeedSearch(tree) {
      @Override
      protected boolean compare(String text, String pattern) {
        return super.compare(text.toLowerCase(), pattern.toLowerCase());
      }
    }.setComparator(new SpeedSearchComparator(false));

    JBScrollPane scrollPane = new JBScrollPane(tree);
    tree.expandRow(0);
    tree.setRootVisible(false);

    return scrollPane;
  }

}