package org.vaadin.marcus.practicalai;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.LumoUtility;

@Layout
public class MainLayout extends AppLayout {

    public MainLayout() {
        var head = new HorizontalLayout();
        head.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        head.add(new DrawerToggle());
        head.add(new H2("Enterprise AI patterns"){{addClassNames(LumoUtility.FontSize.LARGE);}});

        addToNavbar(head);

        var sideBar = new VerticalLayout();

        MenuConfiguration.getMenuEntries().forEach(menuEntry -> {
            sideBar.add(new RouterLink(menuEntry.title(), menuEntry.menuClass()));
        });

        addToDrawer(sideBar);
    }
}
