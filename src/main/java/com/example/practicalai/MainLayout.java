package com.example.practicalai;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.theme.lumo.Lumo;
import com.vaadin.flow.theme.lumo.LumoUtility;

import java.util.Locale;

@Layout
public class MainLayout extends AppLayout {

    public MainLayout() {
        UI.getCurrent().setLocale(Locale.ENGLISH);

        var head = new HorizontalLayout();
        head.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        head.add(new DrawerToggle());
        head.add(new H2("Practical AI in Java"){{addClassNames(LumoUtility.FontSize.LARGE);}});

        addToNavbar(head);

        var sideBar = new VerticalLayout();
        var links = new VerticalLayout();
        links.setMargin(false);

        MenuConfiguration.getMenuEntries().forEach(menuEntry -> {
            links.add(new RouterLink(menuEntry.title(), menuEntry.menuClass()));
        });

        var themeToggle = new Checkbox("Dark theme");

        themeToggle.addValueChangeListener(e -> {
            var js = "document.documentElement.setAttribute('theme', $0)";
            getElement().executeJs(js, e.getValue() ? Lumo.DARK : Lumo.LIGHT);
        });

        sideBar.addAndExpand(links);
        sideBar.add(themeToggle);

        addToDrawer(sideBar);
    }
}
