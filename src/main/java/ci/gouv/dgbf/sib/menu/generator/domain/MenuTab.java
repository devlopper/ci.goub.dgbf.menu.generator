/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ci.gouv.dgbf.sib.menu.generator.domain;

import lombok.Getter;
import lombok.Setter;
import org.primefaces.model.menu.MenuModel;

/**
 *
 * @author ndrijk
 */
@Getter @Setter
public class MenuTab {
    
    private String icon;
    private String title;
    private MenuModel menuModel;
    
}
