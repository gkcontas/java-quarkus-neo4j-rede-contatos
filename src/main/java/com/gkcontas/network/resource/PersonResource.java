package com.gkcontas.network.resource;

import com.gkcontas.network.dto.PathResponse;
import com.gkcontas.network.dto.PersonRequest;
import com.gkcontas.network.dto.PersonResponse;
import com.gkcontas.network.exception.PersonNotFoundException;
import com.gkcontas.network.model.Person;
import com.gkcontas.network.repository.PersonRepository;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

    private final PersonRepository personRepository;

    @Inject
    public PersonResource(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid PersonRequest request) {
        Person person = personRepository.create(request.name());
        return Response.status(Response.Status.CREATED).entity(PersonResponse.of(person)).build();
    }

    @GET
    @Path("/{id}")
    public PersonResponse findById(@PathParam("id") String id) {
        return PersonResponse.of(requirePerson(id));
    }

    @POST
    @Path("/{id}/connections/{otherId}")
    public Response connect(@PathParam("id") String id, @PathParam("otherId") String otherId) {
        if (id.equals(otherId)) {
            throw new IllegalArgumentException("A person cannot be connected to themselves");
        }
        requirePerson(id);
        requirePerson(otherId);
        personRepository.connect(id, otherId);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/connections")
    public List<PersonResponse> directConnections(@PathParam("id") String id) {
        requirePerson(id);
        return personRepository.findDirectConnections(id).stream()
                .map(PersonResponse::of)
                .toList();
    }

    @GET
    @Path("/{id}/connections/second-degree")
    public List<PersonResponse> secondDegreeConnections(@PathParam("id") String id) {
        requirePerson(id);
        return personRepository.findSecondDegreeConnections(id).stream()
                .map(PersonResponse::of)
                .toList();
    }

    @GET
    @Path("/{id}/path/{otherId}")
    public PathResponse shortestPath(@PathParam("id") String id, @PathParam("otherId") String otherId) {
        requirePerson(id);
        requirePerson(otherId);
        List<PersonResponse> people = personRepository.findShortestPath(id, otherId).stream()
                .map(PersonResponse::of)
                .toList();
        return PathResponse.of(people);
    }

    private Person requirePerson(String id) {
        return personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));
    }
}
