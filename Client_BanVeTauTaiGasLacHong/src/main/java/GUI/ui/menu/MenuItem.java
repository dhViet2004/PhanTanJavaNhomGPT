package GUI.ui.menu;

import GUI.ui.effect.RippleEffect;
import GUI.ui.swing.shadow.ShadowRenderer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class MenuItem extends JButton {

    public float getAnimate() {
        return animate;
    }

    public void setAnimate(float animate) {
        this.animate = animate;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isSubMenuAble() {
        return subMenuAble;
    }

    public void setSubMenuAble(boolean subMenuAble) {
        this.subMenuAble = subMenuAble;
    }

    public int getSubMenuIndex() {
        return subMenuIndex;
    }

    public void setSubMenuIndex(int subMenuIndex) {
        this.subMenuIndex = subMenuIndex;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    private RippleEffect rippleEffect;
    private BufferedImage shadow;
    private int shadowWidth;
    private int shadowSize = 10;
    private int index;
    private boolean subMenuAble;
    private float animate;
    //  Submenu
    private int subMenuIndex;
    private int length;

    public MenuItem(String name, int index, boolean subMenuAble) {
        super(name);
        this.index = index;
        this.subMenuAble = subMenuAble;
        setContentAreaFilled(false);

        setForeground(new Color(230, 230, 230));
        setHorizontalAlignment(SwingConstants.LEFT);
        setBorder(new EmptyBorder(9, 10, 9, 10));
        setIconTextGap(10);
        rippleEffect = new RippleEffect(this);
        rippleEffect.setRippleColor(new Color(220, 220, 220));
    }

    private void createShadowImage() {
        int widht = getWidth();
        int height = 5;
        BufferedImage img = new BufferedImage(widht, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.BLACK);
        g2.fill(new Rectangle2D.Double(0, 0, widht, height));
        shadow = new ShadowRenderer(shadowSize, 0.2f, Color.BLACK).createShadow(img);
        g2.dispose();
    }

    public void initSubMenu(int subMenuIndex, int length) {
        this.subMenuIndex = subMenuIndex;
        this.length = length;
        setBorder(new EmptyBorder(9, 33, 9, 10));
        setBackground(new Color(52, 73, 94));
        setFont(new Font("Arial", Font.PLAIN, 14));
        setOpaque(true);
    }


    @Override
    protected void paintComponent(Graphics grphcs) {
        // Store original settings
        Font originalFont = getFont();
        Color originalForeground = getForeground();

        // Set appropriate font and color for active state
        if (isActive) {
            setFont(originalFont.deriveFont(Font.BOLD));
            setForeground(new Color(90, 233, 255));
        }

        // Fill the background before calling super.paintComponent
        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isActive) {
            g2.setColor(new Color(52, 73, 94)); // Màu nền khi active
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else if (getModel().isPressed()) {
            g2.setColor(getBackground().darker());
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else if (getModel().isRollover()) {
            g2.setColor(getBackground().brighter());
            g2.setColor(new Color(52, 73, 94)); // Màu xanh rêu nhạt hơn khi hover
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else if (getBackground() != null && isOpaque()) {
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
        g2.dispose();

        // Let superclass draw the text with our temporary settings
        super.paintComponent(grphcs);

        // Restore original settings
        setFont(originalFont);
        setForeground(originalForeground);

        // Continue with the rest of the custom drawing
        g2 = (Graphics2D) grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Vẽ các thành phần khác (shadow, submenu indicator, ripple effect, etc.)
        if (length != 0) {
            g2.setColor(new Color(43, 141, 98));
            if (subMenuIndex == 1) {
                g2.drawImage(shadow, -shadowSize, -20, null);
                g2.drawLine(18, 0, 18, getHeight());
                g2.drawLine(18, getHeight() / 2, 26, getHeight() / 2);
            } else if (subMenuIndex == length - 1) {
                g2.drawImage(shadow, -shadowSize, getHeight() - 6, null);
                g2.drawLine(18, 0, 18, getHeight() / 2);
                g2.drawLine(18, getHeight() / 2, 26, getHeight() / 2);
            } else {
                g2.drawLine(18, 0, 18, getHeight());
                g2.drawLine(18, getHeight() / 2, 26, getHeight() / 2);
            }
        } else if (subMenuAble) {
            g2.setColor(getForeground());
            int arrowWidth = 8;
            int arrowHeight = 4;
            Path2D p = new Path2D.Double();
            p.moveTo(0, arrowHeight * animate);
            p.lineTo(arrowWidth / 2, (1f - animate) * arrowHeight);
            p.lineTo(arrowWidth, arrowHeight * animate);
            g2.translate(getWidth() - arrowWidth - 15, (getHeight() - arrowHeight) / 2);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.draw(p);
        }

        g2.dispose();
        rippleEffect.reder(grphcs, new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
    }
    @Override
    public void setBounds(int i, int i1, int i2, int i3) {
        super.setBounds(i, i1, i2, i3);
        createShadowImage();
    }

    private boolean isActive;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
        repaint(); // Cập nhật giao diện khi trạng thái thay đổi
    }
}
