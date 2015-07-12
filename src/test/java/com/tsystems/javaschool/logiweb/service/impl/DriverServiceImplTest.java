package com.tsystems.javaschool.logiweb.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tsystems.javaschool.logiweb.LogiwebAppContext;
import com.tsystems.javaschool.logiweb.dao.DriverDao;
import com.tsystems.javaschool.logiweb.dao.DriverShiftJournaDao;
import com.tsystems.javaschool.logiweb.dao.TruckDao;
import com.tsystems.javaschool.logiweb.dao.exceptions.DaoException;
import com.tsystems.javaschool.logiweb.model.City;
import com.tsystems.javaschool.logiweb.model.Driver;
import com.tsystems.javaschool.logiweb.model.DriverShiftJournal;
import com.tsystems.javaschool.logiweb.service.DriverService;
import com.tsystems.javaschool.logiweb.service.exceptions.LogiwebServiceException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LogiwebAppContext.class})
public class DriverServiceImplTest {

    private LogiwebAppContext ctxMock;
    private DriverDao driverDaoMock;
    private TruckDao truckDaoMock;
    private DriverShiftJournaDao shiftDaoMock;
    
    private EntityManagerFactory emfMock;
    private EntityManager emMock;
    private EntityTransaction transMock;
    
    @Before
    public void setUp() {
        ctxMock = PowerMockito.mock(LogiwebAppContext.class);
        
        emfMock = Mockito.mock(EntityManagerFactory.class);
        emMock = Mockito.mock(EntityManager.class);
        transMock = Mockito.mock(EntityTransaction.class);
        
        driverDaoMock = Mockito.mock(DriverDao.class);
        truckDaoMock = Mockito.mock(TruckDao.class);
        shiftDaoMock = Mockito.mock(DriverShiftJournaDao.class);
        
        Mockito.when(emMock.getTransaction()).thenReturn(transMock);
        Mockito.when(ctxMock.getEntityManagerFactory()).thenReturn(emfMock);
        Mockito.when(emfMock.createEntityManager()).thenReturn(emMock);
       
        Mockito.when(ctxMock.createDriverDao(Mockito.any(EntityManager.class))).thenReturn(driverDaoMock);
        Mockito.when(ctxMock.createTruckDao(Mockito.any(EntityManager.class))).thenReturn(truckDaoMock);
        Mockito.when(ctxMock.createDriverShiftJournaDao(Mockito.any(EntityManager.class))).thenReturn(shiftDaoMock);
    }
    
    
    /**
     * Test uses mock dao and entity manager to check algorithm
     * for choosing drivers by working hours from Shift Journals.
     * Drivers and shift journals are created by test.
     * 
     * @throws DaoException "thrown" from mock method. Can't happen.
     * @throws LogiwebServiceException "thrown" from mock method. Can't happen.
     */
    @Test
    public void testFindUnassignedToTrucksDriversByMaxWorkingHoursAndCity() throws DaoException, LogiwebServiceException {
        DriverService driverService = new DriverServiceImpl(ctxMock);
        
        Set<Driver> freeDrivers = new HashSet<Driver>();
        Driver d1 = new Driver(); //have 15 working hours in two shifts
        Driver d2 = new Driver(); //have 12 working hours in one shift
        Driver d3 = new Driver(); //have 0 hours
        Driver d4 = new Driver(); //5 this month, 10 in previous (one shift)
        freeDrivers.add(d1);
        freeDrivers.add(d2);
        freeDrivers.add(d3);
        freeDrivers.add(d4);
        
        Calendar calendar = Calendar.getInstance();  
        calendar.setTime(new Date());  //today
        
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        DriverShiftJournal j1 = new DriverShiftJournal();
        j1.setDriverForThisRecord(d1);
        j1.setId(1);
        j1.setShiftBeggined(calendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        j1.setShiftEnded(calendar.getTime());
        
        DriverShiftJournal j2 = new DriverShiftJournal();
        j2.setDriverForThisRecord(d1);
        j2.setId(1);
        calendar.set(Calendar.HOUR_OF_DAY, 15);
        j2.setShiftBeggined(calendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        j2.setShiftEnded(calendar.getTime());
        //Driver 1 have 15 hours in total
        
        DriverShiftJournal j3 = new DriverShiftJournal();
        j3.setDriverForThisRecord(d2);
        j3.setId(1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        j3.setShiftBeggined(calendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        j3.setShiftEnded(calendar.getTime());
        //Driver 2 have 12 hours in total
        
        DriverShiftJournal j4 = new DriverShiftJournal();
        j4.setDriverForThisRecord(d4);
        j4.setId(1);
        calendar.set(Calendar.HOUR_OF_DAY, 5); //ended
        j4.setShiftEnded(calendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.HOUR, -10); //10 H in prev. month        
        j4.setShiftBeggined(calendar.getTime());
        //driver 4 have 5 hours in this month, and 10 in prev.
        
        Set<DriverShiftJournal> journals = new HashSet<DriverShiftJournal>();
        journals.add(j1);
        journals.add(j2);
        journals.add(j3);
        journals.add(j4);
        
        Mockito.when(driverDaoMock.findByCityWhereNotAssignedToTruck(Mockito.any(City.class))).thenReturn(freeDrivers);
        Mockito.when(shiftDaoMock.findThisMonthJournalsForDrivers(freeDrivers)).thenReturn(journals);
        City anyCity = new City();
        
        //0 hours test. Expected: d3 (0 hours)
        Set<Driver> result = driverService
                .findUnassignedToTrucksDriversByMaxWorkingHoursAndCity(anyCity,
                        0);
        assertThat(result, hasItem(d3));
        
        //10 hours test. Expected: d3 (0 hours) and d4(5 hours) 
        result = driverService
                .findUnassignedToTrucksDriversByMaxWorkingHoursAndCity(anyCity,
                        10);
        assertThat(result, hasItems(d3, d4));
        
        //13 hours test. Expected: d2 (12h), d3 (0 hours) and d4(5 hours) 
        result = driverService
                .findUnassignedToTrucksDriversByMaxWorkingHoursAndCity(anyCity,
                        13);
        assertThat(result, hasItems(d2, d3, d4));
        
        // 16 hours test. Expected: d1 (15h), d2 (12h), d3 (0h) and d4(5h)
        result = driverService
                .findUnassignedToTrucksDriversByMaxWorkingHoursAndCity(anyCity,
                        16);
        assertThat(result, hasItems(d1, d2, d3, d4));

    }

}
