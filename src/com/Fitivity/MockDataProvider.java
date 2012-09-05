
package com.fitivity;

import java.util.Random;

public class MockDataProvider {
    // A utility method that generates random Vehicles
    public static FitivityActivity getRandomVehicle(String name) {
    	FitivityActivity vehicle = null;
        Random random = new Random();
        int type = random.nextInt(3);
        switch (type) {
            case 0:
                vehicle = new Car(name);
                break;
            case 1:
                vehicle = new Bus(name);
                break;
            case 2:
                vehicle = new Bike(name);
                break;
        }
        return vehicle;
    }
}
