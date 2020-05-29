/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ci.gouv.dgbf.sib.menu.generator.api.service;


import ci.gouv.dgbf.sib.menu.generator.dto.BaseDTO;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author jean
 */

public class BaseApiService<T extends BaseDTO> {
    
    @Inject ApiClient apiClientUtils;
    
    private final Logger LOG = Logger.getLogger("apiService");
    
    protected static String apiBaseTarget;
    protected static Type collectionType;
    protected static Type objectType;
    
    protected @NotNull String getResourceString(@NotNull String target){
        return apiClientUtils.getResource(target,MediaType.APPLICATION_JSON_TYPE);
    }
    
    protected String buildUrl(@NotNull String parameter){
        return buildUrl(parameter,"");
    }
    
    protected String buildUrl(@NotNull String parameter,String pathAddition){
        UriBuilder builder = UriBuilder.fromUri(apiBaseTarget).path(pathAddition).path(parameter);
        String result = builder.build().toString();
        LOG.log(Level.INFO, "-->| BUILD TAGET UA URL: {0}", result);
        return result;
    }
    
    public List<T> toList(@NotNull String json){
        LOG.log(Level.INFO, "--> JSON STRING: {0}", json);
        Gson gson = new Gson();
        try{
            return gson.fromJson(json, this.collectionType);
        }catch(JsonSyntaxException ex){
            LOG.log(Level.INFO,ex.getMessage());
            return Collections.EMPTY_LIST;
        }
    }
    
    public Optional<T> toObject(@NotNull String json){
        LOG.log(Level.INFO, "--> JSON STRING: {0}", json);
        Gson gson = new Gson();
        try{
            return gson.fromJson(json, objectType);
        }catch(JsonSyntaxException ex){
            LOG.log(Level.INFO,ex.getMessage());
            return Optional.empty();
        }
    }
     
    public Optional<T> findById(String id){
        return toObject(getResourceString(buildUrl(id)));
    }
     
    public List<T> findAll(){
        return toList(getResourceString(apiBaseTarget));
    }
    
}
