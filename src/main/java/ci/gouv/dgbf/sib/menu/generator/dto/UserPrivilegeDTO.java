package ci.gouv.dgbf.sib.menu.generator.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UserPrivilegeDTO {
    
    private String identifier;
    private String name;
    private String typeAsString;
    private String parentIdentifier;

}