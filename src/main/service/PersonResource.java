package service;

import javax.faces.bean.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;

@Path("/person")
@ApplicationScoped
public class PersonResource {

    @PersistenceContext(unitName = "servicePU")
    private EntityManager em;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Person> all() {
        return em.createNamedQuery("Person.findAll", Person.class)
                .getResultList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response create(Person person) throws Exception {
        if (person.getId() != null)  {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity("Unable to create Person, id was already set.")
                    .build();
        }

        try {
            em.persist(person);
            em.flush();
        } catch (ConstraintViolationException e) {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response
                    .serverError()
                    .entity(e.getMessage())
                    .build();
        }
        return Response
                .created(new URI("person/" + person.getId().toString()))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{personId}")
    public Person get(@PathParam("personId") Integer personId) {
        return em.find(Person.class, personId);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{personId}")
    @Transactional
    public Response remove(@PathParam("personId") Integer personId) {
        try {
            Person entity = em.find(Person.class, personId);
            em.remove(entity);
        } catch (Exception e) {
            return Response
                    .serverError()
                    .entity(e.getMessage())
                    .build();
        }
        return Response
                .noContent()
                .build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{personId}")
    @Transactional
    public Response update(@PathParam("personId") Integer personId, Person person) {
        try {
            Person entity = em.find(Person.class, personId);

            if (null == entity) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Person with id of " + personId + " does not exist.")
                        .build();
            }

            em.merge(person);

            return Response
                    .ok(person)
                    .build();
        } catch (Exception e) {
            return Response
                    .serverError()
                    .entity(e.getMessage())
                    .build();
        }
    }

}