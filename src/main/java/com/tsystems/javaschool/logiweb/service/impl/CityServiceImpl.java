package com.tsystems.javaschool.logiweb.service.impl;

import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import com.tsystems.javaschool.logiweb.LogiwebAppContext;
import com.tsystems.javaschool.logiweb.dao.CityDao;
import com.tsystems.javaschool.logiweb.dao.exceptions.DaoException;
import com.tsystems.javaschool.logiweb.model.City;
import com.tsystems.javaschool.logiweb.service.CityService;
import com.tsystems.javaschool.logiweb.service.exceptions.LogiwebServiceException;

/**
 * Data manipulation and business logic related to Cities.
 * 
 * @author Andrey Baliushin
 */
public class CityServiceImpl implements CityService {
    
    private static final Logger LOG = Logger.getLogger(CityServiceImpl.class);

    private LogiwebAppContext ctx;

    public CityServiceImpl(LogiwebAppContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public City findById(int id) throws LogiwebServiceException {
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        CityDao cityDao = ctx.createCityDao(em);
        try {
            em.getTransaction().begin();
            City city = cityDao.find(id);
            em.getTransaction().commit();
            return city;
        } catch (DaoException e) {
            LOG.warn(e);
            throw new LogiwebServiceException(e);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }
    
    @Override
    public Set<City> findAllCities() throws LogiwebServiceException {
        EntityManager em = ctx.getEntityManagerFactory().createEntityManager();
        CityDao cityDao = ctx.createCityDao(em);
        try {
            em.getTransaction().begin();
            Set<City> cities = cityDao.findAll();
            em.getTransaction().commit();
            return cities;
        } catch (DaoException e) {
            LOG.warn(e);
            throw new LogiwebServiceException(e);
        } finally {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
        }
    }

}
