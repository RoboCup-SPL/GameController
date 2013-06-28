package analyzer;

import common.TotalScaleLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * @author: Michel Bartsch
 * 
 * This window is to be shown to select log-files
 */
@SuppressWarnings("unchecked")
public class GUI extends JFrame implements ListSelectionListener
{
    private final static String TITLE = "Log Analyzer";
    private final static int WINDOW_WIDTH = 600;
    private final static int WINDOW_HEIGHT = 400;
    private final static int DECO_HIGHT = 30;
    private final static int STANDARD_SPACE = 10;
    private final static int ANALYZE_HIGHT = 40;
    private final static int CHECKBOX_WIDTH = 24;
    private final static Color LIST_HIGHLIGHT = new Color(150, 150, 255);
    private final static String ANALYZE = "Analyze";
    
    public final static String HTML = "<html>";
    public final static String HTML_LF = "<br>";
    public final static String HTML_RED = "<font color='red'>";
    public final static String HTML_END = "</font>";
    
    private Games games;
    
    private DefaultListModel list;
    private JList listDisplay;
    private ListSelectionModel selection;
    private JScrollPane scrollArea;
    private JLabel info;
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
        listDisplay.setCellRenderer(new CheckListRenderer());
        listDisplay.addMouseListener(new MouseAdapter()
                {
                    @Override
                    public void mouseClicked(MouseEvent event)
                    {
                        if(event.getPoint().x > CHECKBOX_WIDTH) {
                            return;
                        }
                        JList list = (JList) event.getSource();
                        int index = list.locationToIndex(event.getPoint());
                        CheckListItem item = (CheckListItem)list.getModel().getElementAt(index);
                        item.selected = !item.selected;
                        list.repaint(list.getCellBounds(index, index));
                    }
                });
        selection = listDisplay.getSelectionModel();
        scrollArea = new JScrollPane(listDisplay);
        scrollArea.setPreferredSize(new Dimension((WINDOW_WIDTH-3*STANDARD_SPACE)/2, WINDOW_HEIGHT-2*STANDARD_SPACE-DECO_HIGHT));
        info = new JLabel();
        info.setPreferredSize(new Dimension((WINDOW_WIDTH-3*STANDARD_SPACE)/2, WINDOW_HEIGHT-3*STANDARD_SPACE-ANALYZE_HIGHT-DECO_HIGHT));
        Border paddingBorder = BorderFactory.createEmptyBorder(STANDARD_SPACE/2, STANDARD_SPACE/2, STANDARD_SPACE/2, STANDARD_SPACE/2);
        Border border = BorderFactory.createLineBorder(Color.GRAY);
        info.setBorder(BorderFactory.createCompoundBorder(border, paddingBorder));
        info.setBackground(Color.WHITE);
        info.setOpaque(true);
        info.setVerticalAlignment(SwingConstants.TOP);
        analyze = new JButton(ANALYZE);
        analyze.setPreferredSize(new Dimension((WINDOW_WIDTH-3*STANDARD_SPACE)/2, DECO_HIGHT));
        analyze.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        analyze();
                    }
                }
        );
        layout.add(.03, .03, .45, .94, scrollArea);
        layout.add(.52, .03, .45, .8, info);
        layout.add(.52, .87, .45, .1, analyze);
        
        for(LogInfo log: games.logs) {
            list.addElement(new CheckListItem(log+"", log.isRealLog()));
        }
        selection.addListSelectionListener(this);
        
        setVisible(true);
    }
    
    private void analyze()
    {
        JFileChooser fc = new JFileChooser();
        if(fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = fc.getSelectedFile();
        int i = 0;
        for(LogInfo log: games.logs) {
            if(((CheckListItem)list.getElementAt(i++)).selected) {
                Parser.statistic(log);
            }
        }
        games.toFile(file);
    }

    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        info.setText(games.logs.get(selection.getMinSelectionIndex()).getInfo());
    }
    
    class CheckListItem
    {
        public boolean selected;
        public String label;
        
        public CheckListItem(String label, boolean selected)
        {
            this.label = label;
            this.selected = selected;
        }
    }
    
    class CheckListRenderer extends JCheckBox implements ListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus)
        {
            setEnabled(list.isEnabled());
            setSelected(((CheckListItem)value).selected);
            setFont(list.getFont());
            if(!isSelected) {
                setBackground(list.getBackground());
            } else {
                setBackground(LIST_HIGHLIGHT);
            }
            setForeground(list.getForeground());
            setText(((CheckListItem)value).label);
            return this;
        }
    }
}