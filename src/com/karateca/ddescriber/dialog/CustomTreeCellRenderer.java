package com.karateca.ddescriber.dialog;

import com.intellij.openapi.util.IconLoader;
import com.karateca.ddescriber.model.TestFindResult;
import com.karateca.ddescriber.model.TestState;
import com.karateca.ddescriber.model.TreeNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andres Dominguez.
 */
public class CustomTreeCellRenderer extends DefaultTreeCellRenderer {

  private static final Color GREEN_BG_COLOR = new Color(182, 232, 172);
  private static final Color GREEN_FG_COLOR = new Color(57, 194, 70);
  private static final Color RED_BG_COLOR = new Color(232, 117, 107);
  private static final Color RED_FG_COLOR = new Color(194, 41, 39);
  private final Color defaultNonSelColor = getBackgroundNonSelectionColor();
  private final Color defaultBgSelColor = getBackgroundSelectionColor();
  private final Icon itGreenIcon = IconLoader.findIcon("/icons/it-icon.png");
  private final Icon itRedIcon = IconLoader.findIcon("/icons/it-red-icon.png");
  private final Icon itGrayIcon = IconLoader.findIcon("/icons/it-gray-icon.png");
  private final Icon descIcon = IconLoader.findIcon("/icons/desc-icon.png");
  private final boolean showFileName;
  private final Map<TestState, NodeSettings> colorMap = new HashMap<TestState, NodeSettings>();

  private final NodeSettings includeColor = new NodeSettings(GREEN_BG_COLOR, GREEN_FG_COLOR, itGreenIcon);
  private final NodeSettings excludeColor = new NodeSettings(RED_BG_COLOR, RED_FG_COLOR, itRedIcon);
  private final NodeSettings defaultColor = new NodeSettings(defaultNonSelColor, defaultBgSelColor, itGrayIcon);

  private class NodeSettings {
    final Color bgColor;
    final Color fgColor;
    final Icon icon;

    private NodeSettings(Color bgColor, Color fgColor, Icon icon) {
      this.bgColor = bgColor;
      this.fgColor = fgColor;
      this.icon = icon;
    }

    public void paintNode() {
      setBackgroundNonSelectionColor(bgColor);
      setBackgroundSelectionColor(fgColor);
      setIcon(icon);
    }
  }

  public CustomTreeCellRenderer(boolean showFileName) {
    colorMap.put(TestState.Excluded, excludeColor);
    colorMap.put(TestState.Included, includeColor);
    colorMap.put(TestState.NotModified, defaultColor);
    colorMap.put(TestState.RolledBack, defaultColor);

    this.showFileName = showFileName;
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

    // Ignore non test nodes.
    if (!(node.getUserObject() instanceof TestFindResult)) {
      return component;
    }

    TreeNode treeNode = (TreeNode) node;
    TestFindResult findResult = treeNode.getNodeValue();

    getNodeSettings(findResult).paintNode();

    if (findResult.isDescribe()) {
      setIcon(descIcon);
    }

    String name = findResult.toString();
    if (showFileName && treeNode.isTopNode()) {
      name = String.format("%s - [%s]", name, treeNode.getVirtualFile().getName());
    }
    setText(name);

    return component;
  }

  private NodeSettings getNodeSettings(TestFindResult testFindResult) {
    TestState pendingState = testFindResult.getPendingChangeState();
    TestState originalState = testFindResult.getTestState();

    if (pendingState == TestState.RolledBack &&
        (originalState == TestState.Included || originalState == TestState.Excluded)) {
      return colorMap.get(TestState.NotModified);
    }

    if (pendingState == TestState.Included || pendingState == TestState.Excluded) {
      return colorMap.get(pendingState);
    }

    return colorMap.get(originalState);
  }
}
