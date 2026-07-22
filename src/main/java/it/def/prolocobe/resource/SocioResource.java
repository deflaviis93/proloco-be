package it.def.prolocobe.resource;

import it.def.prolocobe.dto.SocioDto;
import it.def.prolocobe.service.SocioService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/soci")
public class SocioResource {

    private final SocioService socioService;

    protected SocioResource() {
        this(null);
    }

    @Inject
    public SocioResource(SocioService socioService) {
        this.socioService = socioService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SocioDto> list() {
        return socioService.findAll();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SocioDto create(SocioDto socioDto) {
        return socioService.create(socioDto);
    }
}
