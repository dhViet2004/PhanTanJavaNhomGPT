package GUI.ui.menu;

import net.miginfocom.swing.MigLayout;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTargetAdapter;

import java.awt.*;

public class MenuAnimation {

    public static void showMenu(Component component, MenuItem item, MigLayout layout, boolean show) {
        int extraSpace=0;
        int baseHeight = component.getPreferredSize().height + extraSpace;
        Animator animator = new Animator(300, new TimingTargetAdapter() {
            @Override
            public void timingEvent(float fraction) {
                float f = show ? fraction : 1f - fraction;
                layout.setComponentConstraints(component, "h " + baseHeight * f + "!");
                item.setAnimate(f);
                component.revalidate();
                item.repaint();
            }
        });
        animator.setResolution(0);
        animator.setAcceleration(.5f);
        animator.setDeceleration(.5f);
        animator.start();
    }
}
