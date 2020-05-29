/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ci.gouv.dgbf.sib.menu.generator.api.service;

import ci.gouv.dgbf.sib.menu.generator.dto.MenuDTO;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import lombok.Setter;


@ApplicationScoped
public class MenuGeneratorPortailApiService {
    
    @Inject
    ApiClient apiClientUtils;
    
    private static final Logger LOG = Logger.getLogger(MenuGeneratorPortailApiService.class.getName());
    
    public String getPortailUrl(){
        return "https://siib.dgbf.ci";
    }
    
    public List<MenuDTO> findMenusByServiceCode(String serviceCode){
        try {
            Gson gson = new Gson();
            String url = "http://mic-portail-api/sib/portail/api/v1/menus/services?serviceCode=" + URLEncoder.encode(serviceCode,"UTF-8");
            String json = apiClientUtils.getResource(url,MediaType.APPLICATION_JSON_TYPE);
            return gson.fromJson(json, new TypeToken<List<MenuDTO>>(){}.getType());
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
    
    @Getter @Setter
    private class PortailResponse{
        String address;
    }
}
