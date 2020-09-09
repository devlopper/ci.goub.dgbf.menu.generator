/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ci.gouv.dgbf.sib.menu.generator.api.service;

import ci.gouv.dgbf.sib.menu.generator.dto.UserPrivilegeDTO;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;


@ApplicationScoped
public class ActeurApiService {
    
    @Inject
    ApiClient apiClientUtils;
    
    private static final Logger LOG = Logger.getLogger(ActeurApiService.class.getName());
    
    public List<UserPrivilegeDTO> getUserPrivilegeByUsername(String username){
        try {
            Gson gson = new Gson();
            String url = "http://mic-acteur-api/api/privilege/privileges-par-acteur-pour-gestion-services?nom_utilisateur=" + URLEncoder.encode(username,"UTF-8");
            //url = "http://10.3.4.17:30055/api/privilege/privileges-par-acteur-pour-gestion-services?nom_utilisateur=" + URLEncoder.encode(username,"UTF-8");
            String json = apiClientUtils.getResource(url,MediaType.APPLICATION_JSON_TYPE);
            return gson.fromJson(json, new TypeToken<List<UserPrivilegeDTO>>(){}.getType());

        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

}
