package bhuman.gui;

import bhuman.message.BHumanMessage;
import bhuman.message.data.Angle;
import bhuman.message.data.Eigen;
import bhuman.message.data.Timestamp;
import data.SPLTeamMessage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import teamcomm.data.RobotState;
import teamcomm.data.event.RobotStateEvent;
import teamcomm.gui.RobotDetailFrame;

/**
 * Class for the windows showing detailed information about robots. It displays
 * all message info in a tree-structure.
 *
 * @author Felix Thielke
 */
public class BHumanDetailFrame extends RobotDetailFrame {

    private static final long serialVersionUID = -6514911326043029354L;

    private final Node rootNode = new Node("BHumanMessage");
    private final DefaultTreeModel model = new DefaultTreeModel(rootNode);

    private long baseTimestamp = 0;

    private static class Node extends DefaultMutableTreeNode {

        public Node() {
        }

        public Node(final Object userObject) {
            super(userObject);
        }

        private static final long serialVersionUID = -3966229572117914445L;

        @Override
        public boolean isLeaf() {
            return super.isLeaf() && LeafNode.class.isInstance(getUserObject());
        }
    }

    private static class LeafNode {

        public final String key;
        public final String value;

        public LeafNode(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return key + ": " + value;
        }

    }

    /**
     * Constructor.
     *
     * @param robot robot to create the frame for
     * @param anchor panel which triggers the frame on doubleclick
     */
    public BHumanDetailFrame(final RobotState robot, final JPanel anchor) {
        super(robot, anchor);
    }

    @Override
    protected void init(final RobotState robot) {
        final JTree tree = new JTree(rootNode);
        tree.setModel(model);
        tree.setSelectionModel(null);
        tree.putClientProperty("JTree.lineStyle", "Horizontal");

        final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(null);
        tree.setCellRenderer(renderer);

        final JScrollPane contentPane = new JScrollPane(tree);
        setContentPane(contentPane);
        update(robot);
        pack();
    }

    @Override
    public void robotStateChanged(final RobotStateEvent e) {
        if (isVisible()) {
            SwingUtilities.invokeLater(() -> {
                update((RobotState) e.getSource());
                repaint();
            });
        }
    }

    /**
     * Updates the frame with information of the given robot.
     */
    private void update(final RobotState robot) {
        final SPLTeamMessage msg = robot.getLastTeamMessage();
        if (!BHumanMessage.class.isInstance(msg)) {
            return;
        }
        final BHumanMessage bmsg = (BHumanMessage) msg;

        baseTimestamp = bmsg.timestamp;
        updateNode(rootNode, "BHumanMessage", bmsg);

    }

    private void updateNode(final Node node, final String name, final Object obj) {
        if (!name.equals(node.getUserObject())) {
            node.setUserObject(name);
            model.nodeChanged(node);
        }
        if (obj == null) {
            removeNodes(node);
        } else {
            final Class<?> type = obj.getClass();
            if (Number.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type) || Character.class.isAssignableFrom(type) || type.isEnum() || String.class.isAssignableFrom(type) || Angle.class.isAssignableFrom(type)) {
                removeNodes(node);
                node.setUserObject(new LeafNode(name, obj.toString()));
                model.nodeChanged(node);
            } else if (Timestamp.class.isAssignableFrom(type)) {
                removeNodes(node);
                node.setUserObject(new LeafNode(name, (baseTimestamp == 0 ? Timestamp.class.cast(obj).timestamp : Timestamp.class.cast(obj).getTimeSince(baseTimestamp)) + "ms"));
                model.nodeChanged(node);
            } else if (Eigen.Vector2.class.isAssignableFrom(type)) {
                removeNodes(node, 2);
                final Node x, y;
                switch (node.getChildCount()) {
                    case 2:
                        x = (Node) node.getChildAt(0);
                        y = (Node) node.getChildAt(1);
                        break;
                    case 1:
                        x = (Node) node.getChildAt(0);
                        y = new Node();
                        node.insert(y, 1);
                        model.nodesWereInserted(node, new int[]{1});
                        break;
                    default:
                        x = new Node();
                        y = new Node();
                        node.insert(x, 0);
                        node.insert(y, 1);
                        model.nodesWereInserted(node, new int[]{0, 1});
                }
                x.setUserObject(new LeafNode("x", Eigen.Vector2.class.cast(obj).x.toString()));
                y.setUserObject(new LeafNode("y", Eigen.Vector2.class.cast(obj).y.toString()));
                model.nodeChanged(x);
                model.nodeChanged(y);
            } else if (type.isArray()) {
                if (char.class.isAssignableFrom(type.getComponentType())) {
                    removeNodes(node);
                    node.setUserObject(new LeafNode(name, new String(char[].class.cast(obj))));
                    model.nodeChanged(node);
                } else {
                    for (int i = 0; i < Array.getLength(obj); i++) {
                        final Node n;
                        if (node.getChildCount() > i) {
                            n = (Node) node.getChildAt(i);
                        } else {
                            n = new Node();
                            node.insert(n, i);
                            model.nodesWereInserted(node, new int[]{i});
                        }
                        updateNode(n, "[" + i + "]", Array.get(obj, i));
                    }
                    removeNodes(node, Array.getLength(obj));
                }
            } else if (List.class.isAssignableFrom(type)) {
                int i = 0;
                for (final Object o : List.class.cast(obj)) {
                    final Node n;
                    if (node.getChildCount() > i) {
                        n = (Node) node.getChildAt(i);
                    } else {
                        n = new Node();
                        node.insert(n, i);
                        model.nodesWereInserted(node, new int[]{i});
                    }
                    updateNode(n, "[" + i + "]", o);
                    i++;
                }
                removeNodes(node, i);
            } else if (Map.class.isAssignableFrom(type)) {
                int i = 0;
                for (final Object entry : Map.class.cast(obj).entrySet()) {
                    final Node n;
                    if (node.getChildCount() > i) {
                        n = (Node) node.getChildAt(i);
                    } else {
                        n = new Node();
                        node.insert(n, i);
                        model.nodesWereInserted(node, new int[]{i});
                    }
                    updateNode(n, Map.Entry.class.cast(entry).getKey().toString(), Map.Entry.class.cast(entry).getValue());
                    i++;
                }
                removeNodes(node, i);
            } else {
                int index = 0;
                for (final Field f : type.getFields()) {
                    if (!Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
                        final Node n;
                        if (node.getChildCount() > index) {
                            n = (Node) node.getChildAt(index);
                        } else {
                            n = new Node();
                            node.insert(n, index);
                            model.nodesWereInserted(node, new int[]{index});
                        }
                        index++;
                        try {
                            updateNode(n, f.getName(), f.get(obj));
                        } catch (IllegalArgumentException | IllegalAccessException ex) {
                        }
                    }
                }
            }
        }
    }

    private void removeNodes(final Node parent) {
        removeNodes(parent, 0);
    }

    private void removeNodes(final Node parent, final int startindex) {
        if (parent.getChildCount() > startindex) {
            final int[] indices = new int[parent.getChildCount() - startindex];
            final Object[] nodes = new Object[parent.getChildCount() - startindex];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = i + startindex;
                nodes[i] = parent.getChildAt(i + startindex);
            }
            if (startindex == 0) {
                parent.removeAllChildren();
            } else {
                while (parent.getChildCount() > startindex) {
                    parent.remove(startindex);
                }
            }
            model.nodesWereRemoved(parent, indices, nodes);
        }
    }

    @Override
    public void connectionStatusChanged(final RobotStateEvent e) {

    }
}
