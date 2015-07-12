package com.tsystems.javaschool.logiweb;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.tsystems.javaschool.logiweb.dao.CargoDao;
import com.tsystems.javaschool.logiweb.dao.CityDao;
import com.tsystems.javaschool.logiweb.dao.DeliveryOrderDao;
import com.tsystems.javaschool.logiweb.dao.DriverDao;
import com.tsystems.javaschool.logiweb.dao.DriverShiftJournaDao;
import com.tsystems.javaschool.logiweb.dao.TruckDao;
import com.tsystems.javaschool.logiweb.dao.UserDao;
import com.tsystems.javaschool.logiweb.dao.jpa.CargoDaoJpa;
import com.tsystems.javaschool.logiweb.dao.jpa.CityDaoJpa;
import com.tsystems.javaschool.logiweb.dao.jpa.DeliveryOrderDaoJpa;
import com.tsystems.javaschool.logiweb.dao.jpa.DriverDaoJpa;
import com.tsystems.javaschool.logiweb.dao.jpa.DriverShiftJournalDaoJpa;
import com.tsystems.javaschool.logiweb.dao.jpa.TruckDaoJpa;
import com.tsystems.javaschool.logiweb.dao.jpa.UserDaoJpa;
import com.tsystems.javaschool.logiweb.model.Cargo;
import com.tsystems.javaschool.logiweb.model.City;
import com.tsystems.javaschool.logiweb.model.DeliveryOrder;
import com.tsystems.javaschool.logiweb.model.Driver;
import com.tsystems.javaschool.logiweb.model.DriverShiftJournal;
import com.tsystems.javaschool.logiweb.model.Truck;
import com.tsystems.javaschool.logiweb.model.User;
import com.tsystems.javaschool.logiweb.service.CityService;
import com.tsystems.javaschool.logiweb.service.DriverService;
import com.tsystems.javaschool.logiweb.service.OrdersAndCargoService;
import com.tsystems.javaschool.logiweb.service.RouteService;
import com.tsystems.javaschool.logiweb.service.TrucksService;
import com.tsystems.javaschool.logiweb.service.UserService;
import com.tsystems.javaschool.logiweb.service.impl.CityServiceImpl;
import com.tsystems.javaschool.logiweb.service.impl.DriverServiceImpl;
import com.tsystems.javaschool.logiweb.service.impl.OrdersAndCargoServiceImpl;
import com.tsystems.javaschool.logiweb.service.impl.RouteServiceStub;
import com.tsystems.javaschool.logiweb.service.impl.TrucksSeviceimpl;
import com.tsystems.javaschool.logiweb.service.impl.UserServiceImpl;

/**
 * Class provides access to app context.
 * 
 * @author Andrey Baliushin
 */
public final class LogiwebAppContext {

    /**
     * Lazy holder {@see
     * https://en.wikipedia.org/wiki/Singleton_pattern#Initialization
     * -on-demand_holder_idiom}
     */
    private static class SingletonHolder {
        private static final LogiwebAppContext INSTANCE = new LogiwebAppContext();
    }

    public static LogiwebAppContext getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    /**
     * Holds name of session attribute where UserRole will be stored. Also used
     * to check if user is logged. (if equals to null then user is not logged
     * in)
     */
    public static final String SESSION_ATTR_TO_STORE_ROLE = "userRole";

    private static final String PERSISTENCE_UNIT = "logiweb";
    
    volatile private EntityManagerFactory emf;

    private TrucksService truckService;
    volatile private DriverService driverService;
    volatile private UserService userService;
    volatile private CityService cityService;
    volatile private OrdersAndCargoService ordersAndCargoService;

    volatile private RouteService routeService;

    private LogiwebAppContext() {
    }

    /**
     * Get singleton instance of EntityManagerFactory for 'logiweb'
     * persistence-unit.
     * 
     * @return unique instance of EntityManagerFactory
     */
    public EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (this) {
                emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
            }
        }
        return emf;
    }

    public DriverShiftJournaDao createDriverShiftJournaDao(EntityManager em) {
        return new DriverShiftJournalDaoJpa(
                DriverShiftJournal.class, em);
    }

    public CargoDao createCargoDao(EntityManager em) {
        return new CargoDaoJpa(Cargo.class, em);
    }

    public CityDao createCityDao(EntityManager em) {
        return new CityDaoJpa(City.class, em);
    }

    public DriverDao createDriverDao(EntityManager em) {
        return new DriverDaoJpa(Driver.class, em);
    }

    public DeliveryOrderDao createDeliveryOrderDao(EntityManager em) {
        return new DeliveryOrderDaoJpa(DeliveryOrder.class, em);
    }

    public TruckDao createTruckDao(EntityManager em) {
        return new TruckDaoJpa(Truck.class, em);
    }

    public UserDao createUserDao(EntityManager em) {
        return new UserDaoJpa(User.class, em);
    }

    public CityService getCityService() {
        if (cityService == null) {
            synchronized (this) {
                cityService = new CityServiceImpl(this);
            }
        }
        return cityService;
    }

    public DriverService getDriverService() {
        if (driverService == null) {
            synchronized (this) {
                driverService = new DriverServiceImpl(this);
            }
        }
        return driverService;
    }

    public OrdersAndCargoService getOrdersAndCargoService() {
        if (ordersAndCargoService == null) {
            synchronized (this) {
                ordersAndCargoService = new OrdersAndCargoServiceImpl(this);
            }
        }
        return ordersAndCargoService;
    }

    public RouteService getRouteService() {
        if (routeService == null) {
            synchronized (this) {
                routeService = new RouteServiceStub();
            }
        }
        return routeService;
    }

    public TrucksService getTruckService() {
        if (truckService == null) {
            synchronized (this) {
                truckService = new TrucksSeviceimpl(this);
            }
        }
        return truckService;
    }

    public UserService getUserService() {
        if (userService == null) {
            synchronized (this) {
                userService = new UserServiceImpl(this);
            }
        }
        return userService;
    }

}
