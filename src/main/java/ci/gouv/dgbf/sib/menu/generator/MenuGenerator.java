package ci.gouv.dgbf.sib.menu.generator;

import ci.gouv.dgbf.sib.menu.generator.api.service.MenuGeneratorPortailApiService;
import ci.gouv.dgbf.sib.menu.generator.domain.MenuTab;
import ci.gouv.dgbf.sib.menu.generator.dto.MenuDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

@ApplicationScoped
@Getter
@Setter
public class MenuGenerator {

    private static final Logger LOG = Logger.getLogger(MenuGenerator.class.getName());

    @Inject
    MenuGeneratorPortailApiService portailApiService;

    String portailUrl = "http://siib.dgbf.ci";
    List<MenuDTO> menus;

    public MenuGenerator() {

    }

    public List<MenuTab> generateServiceMenu(String serviceCode) {

        List<MenuTab> tabMenus = new ArrayList<>();

        menus = portailApiService.findMenusByServiceCode(serviceCode);
        if (!menus.isEmpty()) {

            menus.sort((MenuDTO m1, MenuDTO m2) -> m1.getPosition() - m2.getPosition());

            
            List<MenuDTO> firstMenuTabMenus = menus.stream().filter(m -> null == m.getMenuParentUuid() && !m.isAbstrait()).collect(Collectors.toList());
            if(!firstMenuTabMenus.isEmpty()){
            MenuTab firstMenuTab = new MenuTab();
            firstMenuTab.setIcon("fa fa-fw fa-home");
            firstMenuTab.setTitle("Menu principal");
            firstMenuTab.setMenuModel(buildFirstTabMenuModel(firstMenuTabMenus));
            tabMenus.add(firstMenuTab);
            }

            menus.stream().filter(m -> null == m.getMenuParentUuid() && m.isAbstrait()).forEach(m -> {

                MenuTab tabMenu = new MenuTab();
                tabMenu.setIcon(m.getIcon());
                tabMenu.setTitle(m.getName());
                MenuModel model = new DefaultMenuModel();
                menus.stream().filter(sbm -> m.getUuid().equals(sbm.getMenuParentUuid())).forEach(mn -> {
                    model.addElement(buildMenuElement(mn));
                });
                tabMenu.setMenuModel(model);
                tabMenus.add(tabMenu);

            });
        }

        return tabMenus;

    }

    private MenuModel buildFirstTabMenuModel(List<MenuDTO> firstTabMenus) {

        MenuModel model = new DefaultMenuModel();

//        DefaultMenuItem accueilMenuItem = new DefaultMenuItem("Accueil");
//        accueilMenuItem.setIcon("fa fa-fw fa-home");
//        accueilMenuItem.setCommand("/protected/user/pages/accueil");
//        accueilMenuItem.setAjax(false);
//        accueilMenuItem.setStyleClass("menu-link");
//        model.addElement(accueilMenuItem);

        firstTabMenus.forEach(m -> {
            model.addElement(buildMenuItemFromMenu(m));
        });

//        DefaultMenuItem menuItem = new DefaultMenuItem("Retour au portail");
//        menuItem.setIcon("fa fa-fw fa-windows");
//        menuItem.setUrl(portailUrl);
//        menuItem.setAjax(false);
//        menuItem.setStyleClass("menu-link");
//        model.addElement(menuItem);

        return model;
    }

    private MenuElement buildMenuElement(MenuDTO m) {

        if (m.isAbstrait()) {

            DefaultSubMenu menuItem = new DefaultSubMenu(m.getName());
            menuItem.setIcon(m.getIcon());
            List<MenuDTO> menuSubmenus = menus.stream().filter(mn -> m.getUuid().equals(mn.getMenuParentUuid())).collect(Collectors.toList());

            menuSubmenus.sort((MenuDTO m1, MenuDTO m2) -> m1.getPosition() - m2.getPosition());
            menuSubmenus.forEach(mn -> {
                menuItem.addElement(buildMenuElement(mn));
            });
            menuItem.setStyleClass("menu-link");

            return menuItem;

        } else {
            return buildMenuItemFromMenu(m);
        }
    }

    private DefaultMenuItem buildMenuItemFromMenu(MenuDTO m) {
        DefaultMenuItem menuItem = new DefaultMenuItem(m.getName());
        menuItem.setIcon(m.getIcon());
        menuItem.setCommand(m.getUrl());
        menuItem.setAjax(false);
        menuItem.setStyleClass("menu-link");
        return menuItem;
    }

}
