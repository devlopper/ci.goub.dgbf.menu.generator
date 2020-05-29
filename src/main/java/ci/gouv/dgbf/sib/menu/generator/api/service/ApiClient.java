/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ci.gouv.dgbf.sib.menu.generator.api.service;

import java.util.concurrent.TimeUnit;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class ApiClient {
    
    private static Client client;
    
    private Client getClient(){
        if(null != client)
            return client;
        
        ClientBuilder clientBuilder = ClientBuilder.newBuilder();
        clientBuilder.connectTimeout(5, TimeUnit.SECONDS);
        clientBuilder.readTimeout(5, TimeUnit.SECONDS);
        client = clientBuilder.build();
        return client;
    }
        
    private Response getResponse(String target,MediaType mediaType){
        return getClient().target(target)
                    .request(mediaType).get();
    }
       
     public String getResource(@NotNull String target,MediaType mediaType){  
        return getResponse(target,mediaType).readEntity(String.class);
    }
}
