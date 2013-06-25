package analyzer;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * @author: Michel Bartsch
 * 
 * This window is to be shown to select log-files
 */
public class GUI extends JFrame
{
    private final static String TITLE = "Log Analyzer";
    private final static int WINDOW_WIDTH = 600;
    private final static int WINDOW_HEIGHT = 400;
    private final static int STANDARD_SPACE = 10;
    
    private DefaultListModel listModel;
    private JList list;
    private JScrollPane scrollArea;
    
    public GUI()
    {
        super(TITLE);
        Dimension desktop = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((desktop.width-WINDOW_WIDTH)/2, (desktop.height-WINDOW_HEIGHT)/2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER, 0, STANDARD_SPACE));
        setResizable(false);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        
        listModel = new DefaultListModel();
        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scrollArea = new JScrollPane(list);
        scrollArea.setPreferredSize(new Dimension((WINDOW_WIDTH-3*STANDARD_SPACE)/2, WINDOW_HEIGHT-2*STANDARD_SPACE));
        add(scrollArea);
        
        //debug
        listModel.addElement("test");
        
        setVisible(true);
    }
}