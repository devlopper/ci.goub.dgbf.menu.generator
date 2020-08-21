package ci.gouv.dgbf.sib.menu.generator;

import ci.gouv.dgbf.sib.menu.generator.api.service.ActeurApiService;
import ci.gouv.dgbf.sib.menu.generator.api.service.MenuGeneratorPortailApiService;
import ci.gouv.dgbf.sib.menu.generator.domain.MenuTab;
import ci.gouv.dgbf.sib.menu.generator.dto.MenuDTO;
import ci.gouv.dgbf.sib.menu.generator.dto.UserPrivilegeDTO;

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
    private static final String ACCOUNT_SERVICE_CODE = "SIIBC-MYOWNER";

    @Inject
    MenuGeneratorPortailApiService portailApiService;

    @Inject
    ActeurApiService acteurApiService;

    String portailUrl = "http://siib.dgbf.ci";

    public MenuGenerator() {

    }

    public List<MenuTab> generateServiceMenu(String serviceCode, String username) {

        List<MenuTab> tabMenus = new ArrayList<>();

        List<UserPrivilegeDTO> privileges = acteurApiService.getUserPrivilegeByUsername(username);
        List<MenuDTO> menus = portailApiService.findMenusByServiceCode(serviceCode);

        menus = menus
                .parallelStream().filter(m -> privileges.parallelStream()
                        .anyMatch(p -> p.getIdentifier().equalsIgnoreCase(m.getUuid())))
                .collect(Collectors.toList());

        menus.sort((MenuDTO m1, MenuDTO m2) -> m1.getPosition() - m2.getPosition());
        List<MenuDTO> noParentMenus = menus.stream()
            .filter(m -> null == m.getMenuParentUuid() && !m.isAbstrait()).collect(Collectors.toList());

        tabMenus.add(buildMenuPrincipal(noParentMenus));

        if (!menus.isEmpty()) {
            List<MenuDTO> firstLevelMenus = menus.stream().filter(m -> null == m.getMenuParentUuid() && m.isAbstrait()).collect(Collectors.toList());
            tabMenus.addAll(buildMenu(firstLevelMenus, menus));
        }

        return tabMenus;

    }

    public List<MenuTab> generateAccountMenu() {

        MenuTab menuTab = new MenuTab();
        List<MenuDTO> menus = portailApiService.findMenusByServiceCode(ACCOUNT_SERVICE_CODE);
        menus.sort((MenuDTO m1, MenuDTO m2) -> m1.getPosition() - m2.getPosition());

        MenuModel model = new DefaultMenuModel();
        menus.forEach(m -> { model.addElement(buildMenuItemFromMenu(m)); });

        menuTab.setMenuModel(model);
        return List.of(menuTab);

    }

    public MenuTab buildMenuPrincipal(List<MenuDTO> firstMenuTabMenus){
        MenuTab menuTab = new MenuTab();
        menuTab.setIcon("fa fa-fw fa-home");
        menuTab.setTitle("Menu principal");
        menuTab.setMenuModel(buildFirstTabMenuModel(firstMenuTabMenus));
        return menuTab;
    }

    public List<MenuTab> buildMenu(final List<MenuDTO> firstLevelMenus, final List<MenuDTO> menus){
        List<MenuTab> tabMenus = new ArrayList<>();
        firstLevelMenus.forEach(m -> {

            MenuTab tabMenu = new MenuTab();
            MenuModel model = new DefaultMenuModel();

            tabMenu.setIcon(m.getIcon());
            tabMenu.setTitle(m.getName());
            
            getMenuSubmenus(m,menus).forEach(mn -> {
                model.addElement(buildMenuElement(mn, menus));
            });
            tabMenu.setMenuModel(model);
            tabMenus.add(tabMenu);
        });
        return tabMenus;
    }

    public List<MenuDTO> getMenuSubmenus(MenuDTO menu, List<MenuDTO> menus){
        return menus.stream().filter(sbm -> menu.getUuid().equals(sbm.getMenuParentUuid())).collect(Collectors.toList());
    }

    private MenuModel buildFirstTabMenuModel(List<MenuDTO> firstTabMenus) {
        MenuModel model = new DefaultMenuModel();
        model.addElement(buildMenuItemFromMenu(new MenuDTO("1","Accueil","/protected/user/pages/accueil.xhtml?faces-redirect=true","","fa fa-home",false,0,"0")));
        firstTabMenus.forEach(m -> { model.addElement(buildMenuItemFromMenu(m)); });
        model.addElement(buildMenuItemFromMenu(new MenuDTO("10","Retour au portail","https://siibtest.dgbf.ci","","fa fa-reply",false,1,"0")));
        return model;
    }

    private MenuElement buildMenuElement(MenuDTO m, List<MenuDTO> menus) {

        if (m.isAbstrait()) {

            DefaultSubMenu menuItem = new DefaultSubMenu(m.getName());
            menuItem.setIcon(m.getIcon());
            List<MenuDTO> menuSubmenus = menus.stream().filter(mn -> m.getUuid().equals(mn.getMenuParentUuid()))
                    .collect(Collectors.toList());

            menuSubmenus.sort((MenuDTO m1, MenuDTO m2) -> m1.getPosition() - m2.getPosition());
            menuSubmenus.forEach(mn -> {
                menuItem.addElement(buildMenuElement(mn, menus));
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
        menuItem.setUrl(m.getUrl());
        menuItem.setAjax(false);
        menuItem.setStyleClass("menu-link");
        return menuItem;
    }

}
