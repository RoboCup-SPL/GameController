package bhuman.gui;

import bhuman.message.BHumanMessage;
import bhuman.message.Message;
import bhuman.message.data.Angle;
import bhuman.message.data.Eigen;
import bhuman.message.data.Timestamp;
import bhuman.message.messages.RobotHealth;
import data.SPLStandardMessage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import teamcomm.data.RobotState;
import teamcomm.data.event.RobotStateEvent;
import teamcomm.gui.RobotDetailFrame;

/**
 * Default class for the windows showing detailed information about robots.
 *
 * @author Felix Thielke
 */
public class BHumanDetailFrame extends RobotDetailFrame {

    private static final long serialVersionUID = -6514911326043029354L;

    private final MutableTreeNode rootNode = new DefaultMutableTreeNode("Robot");
    private final MutableTreeNode splNode = new DefaultMutableTreeNode("SPLStandardMessage");
    private final MutableTreeNode bhulksNode = new DefaultMutableTreeNode("BHULKsStandardMessage");
    private final MutableTreeNode bhumanNode = new DefaultMutableTreeNode("BHumanStandardMessage");
    private final MutableTreeNode messagequeueNode = new DefaultMutableTreeNode("MessageQueue");
    private final DefaultTreeModel model = new DefaultTreeModel(rootNode);

    private long baseTimestamp = 0;

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
        rootNode.insert(splNode, 0);
        rootNode.insert(bhulksNode, 1);
        rootNode.insert(bhumanNode, 2);
        rootNode.insert(messagequeueNode, 3);
        final JTree tree = new JTree(rootNode);
        tree.setModel(model);

        final JScrollPane contentPane = new JScrollPane(tree);
        setContentPane(contentPane);
        update(robot);
        pack();
    }

    @Override
    public void robotStateChanged(final RobotStateEvent e) {
        if (isVisible()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    update((RobotState) e.getSource());
                    repaint();
                }
            });
        }
    }

    /**
     * Updates the frame with information of the given robot.
     */
    private void update(final RobotState robot) {
        final SPLStandardMessage msg = robot.getLastMessage();
        if (msg == null) {
            return;
        }

        int index = 0;
        for (final Field field : SPLStandardMessage.class.getFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
                final MutableTreeNode node;
                if (splNode.getChildCount() > index) {
                    node = (MutableTreeNode) splNode.getChildAt(index);
                } else {
                    node = new DefaultMutableTreeNode();
                    splNode.insert(node, index);
                    model.nodesWereInserted(splNode, new int[]{index});
                }
                index++;
                try {
                    updateNode(node, field.getName(), field.get(msg));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                }
                if (field.getName().equals("nominalDataBytes")) {
                    break;
                }
            }
        }

        if (!BHumanMessage.class.isInstance(msg)) {
            return;
        }
        final BHumanMessage bmsg = (BHumanMessage) msg;

        baseTimestamp = bmsg.message.bhulks != null ? bmsg.message.bhulks.timestamp : 0;
        updateNode(bhulksNode, "BHULKsStandardMessage", bmsg.message.bhulks);
        updateNode(bhumanNode, "BHumanStandardMessage", bmsg.message.bhuman);

        if (bmsg.message.queue == null) {
            removeNodes(messagequeueNode);
        } else {
            for (final String name : bmsg.message.queue.getMessageNames()) {
                MutableTreeNode node = null;
                for (final Enumeration en = messagequeueNode.children(); en.hasMoreElements();) {
                    final DefaultMutableTreeNode n = (DefaultMutableTreeNode) en.nextElement();
                    if (n.getUserObject().equals(name)) {
                        node = n;
                    }
                }
                if (node == null) {
                    node = new DefaultMutableTreeNode(name);
                    messagequeueNode.insert(node, messagequeueNode.getChildCount());
                    model.nodesWereInserted(messagequeueNode, new int[]{messagequeueNode.getChildCount() - 1});
                }
                try {
                    final Class<? extends Message> type = Class.forName("bhuman.message.messages." + name).asSubclass(Message.class);
                    if (type != null) {
                        final Message message = bmsg.message.queue.getMessage(type);
                        if (message != null) {
                            updateNode(node, name, message);
                        }
                    }
                } catch (ClassNotFoundException ex) {
                }
            }

            final RobotHealth health = bmsg.message.queue.getCachedMessage(RobotHealth.class);
            if (health != null && !health.robotName.isEmpty()) {
                rootNode.setUserObject(health.robotName);
                setTitle(health.robotName);
            }
        }
    }

    private void updateNode(final MutableTreeNode node, final String name, final Object obj) {
        if (!name.equals(((DefaultMutableTreeNode) node).getUserObject())) {
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
                final MutableTreeNode x, y;
                switch (node.getChildCount()) {
                    case 2:
                        x = (MutableTreeNode) node.getChildAt(0);
                        y = (MutableTreeNode) node.getChildAt(1);
                        break;
                    case 1:
                        x = (MutableTreeNode) node.getChildAt(0);
                        y = new DefaultMutableTreeNode();
                        node.insert(y, 1);
                        model.nodesWereInserted(node, new int[]{1});
                        break;
                    default:
                        x = new DefaultMutableTreeNode();
                        y = new DefaultMutableTreeNode();
                        node.insert(x, 0);
                        node.insert(y, 1);
                        model.nodesWereInserted(node, new int[]{0, 1});
                }
                x.setUserObject("x: " + Eigen.Vector2.class.cast(obj).x);
                y.setUserObject("y: " + Eigen.Vector2.class.cast(obj).y);
            } else if (type.isArray()) {
                if (char.class.isAssignableFrom(type.getComponentType())) {
                    removeNodes(node);
                    node.setUserObject(new LeafNode(name, new String(char[].class.cast(obj))));
                    model.nodeChanged(node);
                } else {
                    for (int i = 0; i < Array.getLength(obj); i++) {
                        final MutableTreeNode n;
                        if (node.getChildCount() > i) {
                            n = (MutableTreeNode) node.getChildAt(i);
                        } else {
                            n = new DefaultMutableTreeNode();
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
                    final MutableTreeNode n;
                    if (node.getChildCount() > i) {
                        n = (MutableTreeNode) node.getChildAt(i);
                    } else {
                        n = new DefaultMutableTreeNode();
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
                    final MutableTreeNode n;
                    if (node.getChildCount() > i) {
                        n = (MutableTreeNode) node.getChildAt(i);
                    } else {
                        n = new DefaultMutableTreeNode();
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
                        final MutableTreeNode n;
                        if (node.getChildCount() > index) {
                            n = (MutableTreeNode) node.getChildAt(index);
                        } else {
                            n = new DefaultMutableTreeNode();
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

    private void removeNodes(final MutableTreeNode parent) {
        removeNodes(parent, 0);
    }

    private void removeNodes(final MutableTreeNode parent, final int startindex) {
        if (parent.getChildCount() > startindex) {
            final int[] indices = new int[parent.getChildCount() - startindex];
            final Object[] nodes = new Object[parent.getChildCount() - startindex];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = i + startindex;
                nodes[i] = parent.getChildAt(i + startindex);
            }
            while (parent.getChildCount() > startindex) {
                parent.remove(startindex);
            }
            model.nodesWereRemoved(parent, indices, nodes);
        }
    }

    @Override
    public void connectionStatusChanged(final RobotStateEvent e
    ) {

    }
}
