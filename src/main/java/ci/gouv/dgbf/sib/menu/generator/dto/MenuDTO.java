/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ci.gouv.dgbf.sib.menu.generator.dto;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author jean
 */
@Getter @Setter @EqualsAndHashCode
public class MenuDTO extends BaseDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    
    private String name;
    private String url;
    private String serviceUuid;
    private boolean abstrait;
    private int position;
    private String menuParentUuid;
    private String icon;
    
}
