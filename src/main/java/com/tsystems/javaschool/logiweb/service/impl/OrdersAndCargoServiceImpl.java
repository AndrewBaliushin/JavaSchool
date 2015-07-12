package com.tsystems.javaschool.logiweb.service.impl;

import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.tsystems.javaschool.logiweb.LogiwebAppContext;
import com.tsystems.javaschool.logiweb.dao.CargoDao;
import com.tsystems.javaschool.logiweb.dao.CityDao;
import com.tsystems.javaschool.logiweb.dao.DeliveryOrderDao;
import com.tsystems.javaschool.logiweb.dao.TruckDao;
import com.tsystems.javaschool.logiweb.dao.exceptions.DaoException;
import com.tsystems.javaschool.logiweb.model.Cargo;
import com.tsystems.javaschool.logiweb.model.City;
import com.tsystems.javaschool.logiweb.model.DeliveryOrder;
import com.tsystems.javaschool.logiweb.model.Truck;
import com.tsystems.javaschool.logiweb.model.status.CargoStatus;
import com.tsystems.javaschool.logiweb.model.status.OrderStatus;
import com.tsystems.javaschool.logiweb.model.status.TruckStatus;
import com.tsystems.javaschool.logiweb.service.OrdersAndCargoService;
import com.tsystems.javaschool.logiweb.service.exceptions.LogiwebServiceException;
import com.tsystems.javaschool.logiweb.service.exceptions.ServiceValidationException;

/**
 * Data manipulation and business logic related to 
 * orders and cargoes.
 * 
 * @author Andrey Baliushin
 */
public class OrdersAndCargoServiceImpl implements OrdersAndCargoService {

    private static final Logger LOG = Logger.getLogger(OrdersAndCargoServiceImpl.class);
        
    private LogiwebAppContext ctx;

    public OrdersAndCargoServiceImpl(LogiwebAppContext ctx) {
        this.ctx = ctx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<DeliveryOrder> findAllOrders() throws LogiwebServiceException {
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        DeliveryOrderDao deliveryOrderDao = ctx.createDeliveryOrderDao(em);
        try {
            em.getTransaction().begin();
            Set<DeliveryOrder> orders = deliveryOrderDao.findAll();
            em.getTransaction().commit();
            return orders;
        } catch (DaoException e) {
            LOG.warn("Something unexcpected happend.");
            throw new LogiwebServiceException(e);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Cargo> findAllCargoes() throws LogiwebServiceException {
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        CargoDao cargoDao = ctx.createCargoDao(em);
        try {
            em.getTransaction().begin();
            Set<Cargo> orders = cargoDao.findAll();
            em.getTransaction().commit();
            return orders;
        } catch (DaoException e) {
            LOG.warn("Something unexcpected happend.");
            throw new LogiwebServiceException(e);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DeliveryOrder addNewOrder(DeliveryOrder newOrder)
            throws LogiwebServiceException {
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        DeliveryOrderDao deliveryOrderDao = ctx.createDeliveryOrderDao(em);
        try {
            em.getTransaction().begin();
            deliveryOrderDao.create(newOrder);
            em.getTransaction().commit();

            LOG.info("Order created. ID#" + newOrder.getId());

            return newOrder;
        } catch (DaoException e) {         
            LOG.warn("Something unexpected happend.", e);
            throw new LogiwebServiceException(e);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeliveryOrder findOrderById(int id) throws LogiwebServiceException {
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        DeliveryOrderDao deliveryOrderDao = ctx.createDeliveryOrderDao(em);
        try {
            em.getTransaction().begin();
            DeliveryOrder order = deliveryOrderDao.find(id);
            em.getTransaction().commit();

            return order;
        } catch (DaoException e) {         
            LOG.warn("Something unexpected happend.", e);
            throw new LogiwebServiceException(e);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void addCargo(Cargo newCargo) throws ServiceValidationException, LogiwebServiceException {
        try {
            validateNewCargoForEmptyFields(newCargo);
        } catch (ServiceValidationException e) {
            throw e;
        }
        
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        DeliveryOrderDao deliveryOrderDao = ctx.createDeliveryOrderDao(em);
        CityDao cityDao = ctx.createCityDao(em);
        CargoDao cargoDao = ctx.createCargoDao(em);
        try {
            em.getTransaction().begin();
            
            //get managed entities
            City originCity = cityDao.find(newCargo.getOriginCity().getId());
            City destinationCity = cityDao.find(newCargo.getDestinationCity()
                    .getId());
            DeliveryOrder orderForCargo = deliveryOrderDao.find(newCargo
                    .getOrderForThisCargo().getId());
            
            
            //switch detached entities in cargo to managed ones
            newCargo.setOriginCity(originCity);
            newCargo.setDestinationCity(destinationCity);
            newCargo.setOrderForThisCargo(orderForCargo);
            
            validateCargoManagedFieldsByBusinessRequirements(newCargo);            
            
            newCargo.setStatus(CargoStatus.WAITING_FOR_PICKUP);
            
            cargoDao.create(newCargo);
            LOG.info("New cargo with id #" + newCargo.getId() + "created for irder id #" + orderForCargo.getId());
            em.refresh(newCargo.getOrderForThisCargo());
            em.getTransaction().commit();
        } catch (DaoException e) {         
            LOG.warn("Something unexpected happend.", e);
            throw new LogiwebServiceException(e);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
    
   /**
    * Validate cargo (fields must be managed)
    * 
    * Req.:
    * Both destination an origin cities must exist, and must not be the same city.
    * Order for this cargo must exist.
    * Order must be with status NOT READY
    * @param c cargo where all fields are managed entities.
    * @throws ServiceValidationException if requirements are not met. 
    */
    private void validateCargoManagedFieldsByBusinessRequirements(Cargo c) throws ServiceValidationException {
        if (c.getOriginCity() == null) {
            throw new ServiceValidationException("Origin city does not exist.");
        } else if (c.getDestinationCity() == null) {
            throw new ServiceValidationException(
                    "Destination city does not exist.");
        } else if (c.getOriginCity().equals(c.getDestinationCity())) {
            throw new ServiceValidationException("Cities must be different.");
        } else if (c.getOrderForThisCargo() == null) {
            throw new ServiceValidationException(
                    "Order for this cargo does not exist.");
        } else if (c.getOrderForThisCargo().getStatus() != OrderStatus.NOT_READY) {
            throw new ServiceValidationException(
                    "Order must be in NOT READY status to add new cargo.");
        } else if (c.getOrderForThisCargo().getAssignedTruck() != null) {
            throw new ServiceValidationException(
                    "Order can't be assigned to Truck.");
        }
    }
    
    /**
     * Validate that cities, title, weight, and order fields are not empty or null.
     * @param c
     * @throws ServiceValidationException if validation failed.
     */
    private void validateNewCargoForEmptyFields(Cargo c) throws ServiceValidationException {
        if (StringUtils.isBlank(c.getTitle())) {
            throw new ServiceValidationException("Cargo title can't be blank.");
        } else if (c.getWeight() <= 0f) {
            throw new ServiceValidationException("Cargo weight must be greater than zero.");
        } else if (c.getOriginCity() == null) {
            throw new ServiceValidationException("Origin city must be specified.");
        } else if (c.getDestinationCity() == null) {
            throw new ServiceValidationException("Desitnation city must be specified.");
        } else if (c.getOrderForThisCargo() == null) {
            throw new ServiceValidationException("Delivery Order must be specified.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void assignTruckToOrder(Truck truck, int orderId)
            throws ServiceValidationException, LogiwebServiceException {
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        DeliveryOrderDao deliveryOrderDao = ctx.createDeliveryOrderDao(em);
        TruckDao truckDao = ctx.createTruckDao(em);
        try {
            em.getTransaction().begin();
            
            if(truck == null) {
                throw new ServiceValidationException("Truck does not exist.");
            } 
            truck = truckDao.find(truck.getId());       //switch to managed entity
            if(truck == null) {
                throw new ServiceValidationException("Truck does not exist.");
            } 
            
            if(truck.getStatus() != TruckStatus.OK) {
                throw new ServiceValidationException("Truck must have OK status.");
            } else if (truck.getAssignedDeliveryOrder() != null ) {
                throw new ServiceValidationException("Truck must not have assigned orders.");
            }
            
            DeliveryOrder order = deliveryOrderDao.find(orderId);
            
            if(order == null) {
                throw new ServiceValidationException("Order " + orderId + " does not exist.");
            } else if (order.getAssignedTruck() != null) {
                throw new ServiceValidationException("Order " + orderId + " must not be assigned to another truck.");
            } else if (order.getAssignedCargoes() == null || order.getAssignedCargoes().isEmpty()) {
                throw new ServiceValidationException("Order " + orderId + " must have cargo.");
            }
            
            truck.setAssignedDeliveryOrder(order);
            order.setAssignedTruck(truck);
            em.merge(truck);
            
            LOG.info("Truck id#" + truck.getId() + " assign to order id#" + order.getId());
            
            em.getTransaction().commit();
        } catch (DaoException e) {
            LOG.warn("Something unexpected happened.", e);
            throw new LogiwebServiceException(e);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setReadyStatusForOrder(DeliveryOrder order)
            throws ServiceValidationException, LogiwebServiceException {
        if(order == null) {
            throw new ServiceValidationException("Order does not exist.");
        } 
        
        if (order.getAssignedCargoes() == null
                || order.getAssignedCargoes().isEmpty()) {
            throw new ServiceValidationException(
                    "Order must contain at least 1 cargo.");
        } else if (order.getAssignedTruck() == null) {
            throw new ServiceValidationException(
                    "Order must have assigned truck.");
        } else if (order.getAssignedTruck().getDrivers() == null
                || order.getAssignedTruck().getDrivers().size() < order
                        .getAssignedTruck().getCrewSize()) {
            throw new ServiceValidationException(
                    "Truck must have full crew. Assign drivers.");
        }
        
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        DeliveryOrderDao deliveryOrderDao = ctx.createDeliveryOrderDao(em);
        try {
            em.getTransaction().begin();
            order.setStatus(OrderStatus.READY_TO_GO);
            deliveryOrderDao.update(order);
            
            LOG.info("Order id#" + order.getId() + " changed status to " + OrderStatus.READY_TO_GO);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            LOG.warn("Something unexpected happened.", e);
            throw new LogiwebServiceException(e);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
    
}
