package controller.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;


/**
 * Creates a background image 
 * 
 * @author Andre Muehlenbrock
 */

@SuppressWarnings("serial")
public class BackgroundImage extends ImageIcon{    
    public BackgroundImage(String path, boolean isRight, Color bgColor){
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(path));
            if(isRight){
                img = horizontalflip(img);
            }
            
            int red = (int) ((bgColor.getRed()/(float)255) * 127 + 128);
            int green = (int) ((bgColor.getGreen()/(float)255) * 127 + 128);
            int blue = (int) ((bgColor.getBlue()/(float)255) * 127 + 128);
            
            Color alphaBGColor = new Color(red, green, blue, 64);
            
            Graphics2D g = (Graphics2D)img.getGraphics();
            g.setComposite(AlphaComposite.SrcAtop);
            g.setColor(alphaBGColor);
            g.fillRect(0,0,img.getWidth(), img.getHeight());
            
            setImage(img);
        } catch (IOException e) {
            throw new RuntimeException("BackgroundImage not found: "+path);
        }
        
        
    }
    
    public static BufferedImage horizontalflip(BufferedImage img) {  
        int w = img.getWidth();  
        int h = img.getHeight();  
        BufferedImage dimg = new BufferedImage(w, h, img.getType());  
        Graphics2D g = dimg.createGraphics();  
        g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);  
        g.dispose();  
        return dimg;  
    }
}
