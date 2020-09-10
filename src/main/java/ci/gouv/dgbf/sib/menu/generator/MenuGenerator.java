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

    private static final String LOGOUT_MENU_CODE = "LOGOUT";

    @Inject
    MenuGeneratorPortailApiService portailApiService;

    @Inject
    ActeurApiService acteurApiService;

    public MenuGenerator() {

    }

    public List<MenuTab> generateServiceMenu(String serviceCode, String username, String contextPath) {

        List<MenuTab> tabMenus = new ArrayList<>();

        List<UserPrivilegeDTO> privilegesf = acteurApiService.getUserPrivilegeByUsername(username);
        final List<UserPrivilegeDTO> privileges = privilegesf == null ? new ArrayList<>() : privilegesf;

        List<MenuDTO> menus = portailApiService.findMenusByServiceCode(serviceCode);

        menus = menus
                .parallelStream().filter(m -> privileges.parallelStream()
                    .anyMatch(p -> p.getIdentifier().equalsIgnoreCase(m.getUuid())))
                .collect(Collectors.toList());

        menus.sort((MenuDTO m1, MenuDTO m2) -> m1.getPosition() - m2.getPosition());
        List<MenuDTO> noParentMenus = menus.stream()
            .filter(m -> null == m.getMenuParentUuid() && !m.isAbstrait()).collect(Collectors.toList());

        tabMenus.add(buildMenuPrincipal(noParentMenus, contextPath));

        if (!menus.isEmpty()) {
            List<MenuDTO> firstLevelMenus = menus.stream().filter(m -> null == m.getMenuParentUuid() && m.isAbstrait()).collect(Collectors.toList());
            tabMenus.addAll(buildMenu(firstLevelMenus, menus, contextPath));
        }

        return tabMenus;

    }

    public List<MenuTab> generateAccountMenu() {

        MenuTab menuTab = new MenuTab();
        List<MenuDTO> menus = portailApiService.findMenusByServiceCode(ACCOUNT_SERVICE_CODE);
        menus.sort((MenuDTO m1, MenuDTO m2) -> m1.getPosition() - m2.getPosition());

        MenuModel model = new DefaultMenuModel();
        menus.forEach(m -> {
            if(LOGOUT_MENU_CODE.equalsIgnoreCase(m.getCode())){
                DefaultMenuItem menuItem = new DefaultMenuItem(m.getName());
                menuItem.setIcon(m.getIcon());
                menuItem.setCommand("#{micBacking.logout()}");
                menuItem.setAjax(false);
                menuItem.setStyleClass("menu-link");
                model.addElement(menuItem);
            }
            else
                model.addElement(buildMenuItemFromMenu(m)); 
        });

        menuTab.setMenuModel(model);
        return List.of(menuTab);

    }

    public MenuTab buildMenuPrincipal(List<MenuDTO> firstMenuTabMenus, String contextPath){
        MenuTab menuTab = new MenuTab();
        menuTab.setIcon("fa fa-fw fa-home");
        menuTab.setTitle("Menu principal");
        menuTab.setMenuModel(buildFirstTabMenuModel(firstMenuTabMenus,contextPath));
        return menuTab;
    }

    public List<MenuTab> buildMenu(final List<MenuDTO> firstLevelMenus, final List<MenuDTO> menus, String contextPath){
        List<MenuTab> tabMenus = new ArrayList<>();
        firstLevelMenus.forEach(m -> {

            MenuTab tabMenu = new MenuTab();
            MenuModel model = new DefaultMenuModel();

            tabMenu.setIcon(m.getIcon());
            tabMenu.setTitle(m.getName());
            
            getMenuSubmenus(m,menus).forEach(mn -> {
                model.addElement(buildMenuElement(mn, menus,contextPath));
            });
            tabMenu.setMenuModel(model);
            tabMenus.add(tabMenu);
        });
        return tabMenus;
    }

    public List<MenuDTO> getMenuSubmenus(MenuDTO menu, List<MenuDTO> menus){
        return menus.stream().filter(sbm -> menu.getUuid().equals(sbm.getMenuParentUuid())).collect(Collectors.toList());
    }

    private MenuModel buildFirstTabMenuModel(List<MenuDTO> firstTabMenus, String contextPath) {
        MenuModel model = new DefaultMenuModel();
        model.addElement(buildMenuItemFromMenu(new MenuDTO("1","Accueil",contextPath + "/protected/user/pages/accueil.xhtml?faces-redirect=true","","fa fa-home",false,0,"0")));
        firstTabMenus.forEach(m -> { model.addElement(buildMenuItemFromMenu(m,contextPath)); });
        model.addElement(buildMenuItemFromMenu(new MenuDTO("10","Retour au portail","/","","fa fa-reply",false,1,"0")));
        return model;
    }

    private MenuElement buildMenuElement(MenuDTO m, List<MenuDTO> menus, String contextPath) {

        if (m.isAbstrait()) {

            DefaultSubMenu menuItem = new DefaultSubMenu(m.getName());
            menuItem.setIcon(m.getIcon());
            List<MenuDTO> menuSubmenus = menus.stream().filter(mn -> m.getUuid().equals(mn.getMenuParentUuid()))
                    .collect(Collectors.toList());

            menuSubmenus.sort((MenuDTO m1, MenuDTO m2) -> m1.getPosition() - m2.getPosition());
            menuSubmenus.forEach(mn -> {
                menuItem.addElement(buildMenuElement(mn, menus, contextPath));
            });
            menuItem.setStyleClass("menu-link");

            return menuItem;

        } else {
            return buildMenuItemFromMenu(m,contextPath);
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

    private DefaultMenuItem buildMenuItemFromMenu(MenuDTO m, String contextPath) {
        DefaultMenuItem menuItem = new DefaultMenuItem(m.getName());

        String realContextPath = "";

        if(m.getContextPath() != null && !m.getContextPath().isBlank()){
            realContextPath = m.getContextPath() + "/";
        }
        else{
            realContextPath = contextPath + "/";
        }

        String url = realContextPath + m.getUrl();
        url = url.replaceAll("//", "/");
        url = !url.startsWith("/") ? "/" + url : url;

        menuItem.setIcon(m.getIcon());
        menuItem.setUrl(url);
        menuItem.setAjax(false);
        menuItem.setStyleClass("menu-link");
        return menuItem;
    }

}
