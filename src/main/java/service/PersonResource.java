package service;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;

@Path("/person")
@ApplicationScoped
@Stateful
@ManagedBean
public class PersonResource {

    @PersistenceContext(unitName = "servicePU")
    private EntityManager em;

    @Resource
    private UserTransaction userTransaction;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Person> all() {
        return em.createNamedQuery("Person.findAll", Person.class)
                .getResultList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Person person) throws Exception {
        if (person.getId() != null)  {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity("Unable to create Person, id was already set.")
                    .build();
        }

        try {
            userTransaction.begin();
            em.persist(person);
            userTransaction.commit();
        } catch (ConstraintViolationException e) {
            userTransaction.rollback();
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            userTransaction.rollback();
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
    @Path("/{person_id}")
    public Person get(@PathParam("person_id") Integer person_id) {
        return em.find(Person.class, person_id);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{person_id}")
    public Response remove(@PathParam("person_id") Integer person_id) throws Exception {
        try {
            userTransaction.begin();
            Person entity = em.find(Person.class, person_id);
            em.remove(entity);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
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
    @Path("/{person_id}")
    public Response update(@PathParam("person_id") Integer person_id, Person person) throws Exception {
        try {
            Person entity = em.find(Person.class, person_id);

            if (null == entity) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Person with id of " + person_id + " does not exist.")
                        .build();
            }

            userTransaction.begin();
            em.merge(person);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            return Response
                    .serverError()
                    .entity(e.getMessage())
                    .build();
        }
        return Response
                .ok(person)
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearAll() throws Exception {
        Collection<Person> people = all();

        try {
            userTransaction.begin();
            for (Person person : people) {
                if (person.getId() != 1) {
                    person = em.find(Person.class, person.getId());
                    em.remove(person);
                }
            }
            userTransaction.commit();
        } catch (Exception e) {
            System.out.println("DELETE ALL PEOPLE FAILED");
            System.out.println(e.getMessage());
            e.printStackTrace();
            userTransaction.rollback();
            return Response
                    .serverError()
                    .entity(e.getMessage())
                    .build();
        }

        try {
            userTransaction.begin();
            em.createNativeQuery("ALTER SEQUENCE person_sequence RESTART WITH 2").executeUpdate();
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            System.out.println("Error resetting the person table!");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return Response
                    .serverError()
                    .build();
        }
        return Response
                .status(Response.Status.OK)
                .build();
    }

}