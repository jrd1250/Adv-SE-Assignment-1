package service;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.ejb.Stateful;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;

@Path("/car")
@ApplicationScoped
@Stateful
@ManagedBean
public class CarResource {

    @PersistenceContext(unitName = "servicePU")
    private EntityManager em;

    @Resource
    private UserTransaction userTransaction;

    @Inject PersonResource personResource;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Car> all() {
        return em.createNamedQuery("Car.findAll", Car.class)
                .getResultList();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(Car car) throws Exception {
        if (car.getId() != null)  {
            return Response
                    .status(Response.Status.CONFLICT)
                    .entity("Unable to create Car, id was already set.")
                    .build();
        }

        try {
            userTransaction.begin();
            Person owner;
            if ((owner = car.getOwner()) != null && owner.getId() != null) {
                car.setOwner(personResource.get(owner.getId()));
            }
            em.persist(car);
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
                .created(new URI("car/" + car.getId().toString()))
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{car_id}")
    public Car get(@PathParam("car_id") Integer car_id) {
        return em.find(Car.class, car_id);
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{car_id}")
    public Response remove(@PathParam("car_id") Integer car_id) throws Exception {
        try {
            userTransaction.begin();
            Car entity = em.find(Car.class, car_id);
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
    @Path("/{car_id}")
    public Response update(@PathParam("car_id") Integer car_id, Car car) throws Exception {
        try {
            Car entity = em.find(Car.class, car_id);

            if (null == entity) {
                return Response
                        .status(Response.Status.NOT_FOUND)
                        .entity("Car with id of " + car_id + " does not exist.")
                        .build();
            }

            userTransaction.begin();
            Person owner;
            if ((owner = car.getOwner()) != null) {
                if (owner.getId() != null) {
                    car.setOwner(personResource.get(owner.getId()));
                }
            }

            em.merge(car);
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            return Response
                    .serverError()
                    .entity(e.getMessage())
                    .build();
        }
        return Response
                .ok(car)
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearAll() throws Exception {
        Collection<Car> cars = all();

        try {
            userTransaction.begin();
            for (Car car : cars) {
                if (car.getId() != 1) {
                    car = em.find(Car.class, car.getId());
                    em.remove(car);
                }
            }
            userTransaction.commit();
        } catch (Exception e) {
            System.out.println("DELETE ALL CARS FAILED");
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
            em.createNativeQuery("ALTER SEQUENCE car_sequence RESTART WITH 2").executeUpdate();
            userTransaction.commit();
        } catch (Exception e) {
            userTransaction.rollback();
            System.out.println("Error resetting the car table!");
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
