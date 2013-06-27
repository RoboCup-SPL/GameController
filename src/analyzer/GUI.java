package analyzer;

import common.TotalScaleLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author: Michel Bartsch
 * 
 * This window is to be shown to select log-files
 */
public class GUI extends JFrame implements ListSelectionListener
{
    private final static String TITLE = "Log Analyzer";
    private final static int WINDOW_WIDTH = 600;
    private final static int WINDOW_HEIGHT = 400;
    private final static int DECO_HIGHT = 30;
    private final static int STANDARD_SPACE = 10;
    private final static int ANALYZE_HIGHT = 40;
    private final static String ANALYZE = "Analyze";
    
    private Games games;
    
    private DefaultListModel list;
    private JList listDisplay;
    private ListSelectionModel selection;
    private JScrollPane scrollArea;
    private JTextArea info;
    private JButton analyze;
    
    public GUI(Games games)
    {
        super(TITLE);
        
        this.games = games;
        
        Dimension desktop = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((desktop.width-WINDOW_WIDTH)/2, (desktop.height-WINDOW_HEIGHT)/2);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        TotalScaleLayout layout = new TotalScaleLayout(this);
        setLayout(layout);
        
        list = new DefaultListModel();
        listDisplay = new JList(list);
        listDisplay.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selection = listDisplay.getSelectionModel();
        scrollArea = new JScrollPane(listDisplay);
        scrollArea.setPreferredSize(new Dimension((WINDOW_WIDTH-3*STANDARD_SPACE)/2, WINDOW_HEIGHT-2*STANDARD_SPACE-DECO_HIGHT));
        info = new JTextArea();
        info.setPreferredSize(new Dimension((WINDOW_WIDTH-3*STANDARD_SPACE)/2, WINDOW_HEIGHT-3*STANDARD_SPACE-ANALYZE_HIGHT-DECO_HIGHT));
        info.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        analyze = new JButton(ANALYZE);
        analyze.setPreferredSize(new Dimension((WINDOW_WIDTH-3*STANDARD_SPACE)/2, DECO_HIGHT));
        layout.add(.03, .03, .45, .94, scrollArea);
        layout.add(.52, .03, .45, .8, info);
        layout.add(.52, .87, .45, .1, analyze);
        
        String[] logs = games.getLogs();
        for(String log: logs) {
            list.addElement(log);
        }
        selection.addListSelectionListener(this);
        
        setVisible(true);
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        info.setText(games.logs.get(selection.getMinSelectionIndex()).getInfo());
    }
}