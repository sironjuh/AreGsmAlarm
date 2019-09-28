/**
 * <CODE>TimeTable</CODE>
 * <p>
 * Schedule container.
 * <p>
 * ChangeLog:
 * <p>
 * v0.1 - Initial version (22.9.2009)
 * v0.2 - Modified to use dayschedules (09.03.2010)
 * v0.3 - isActive method now uses dayschedule data (10.5.2010)
 * v0.4 - added removeDaySchedule(int selected) method (19.6.2010)
 * <p>
 * TODO:
 * - alot
 *
 * @author Juha-Matti Sironen
 * @version 0.4
 * @date 16.06.2010
 */


package fi.devl.gsmalarm.domain;

import fi.devl.gsmalarm.domain.DaySchedule;

import java.util.ArrayList;
import java.util.Calendar;

public class TimeTable {

    private String name;
    private String id;
    private ArrayList<DaySchedule> daysched;
    private boolean active;

    String DATE_FORMAT_NOW = "HH:mm:ss dd-MM-yyyy";

    public TimeTable(String name) {
        this.name = name;
        this.id = null;
        this.daysched = new ArrayList<DaySchedule>();
        this.active = false;
    }

    public TimeTable(String name, String id) {
        this.name = name;
        this.id = id;
        this.daysched = new ArrayList<DaySchedule>();
        this.active = false;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getId() {
        return this.id;
    }

    public void sedId(String newId) {
        this.id = newId;
    }

    public void addDaySchedule(DaySchedule ds) {
        this.daysched.add(ds);
    }

    public ArrayList<DaySchedule> getDaySchedules() {
        return this.daysched;
    }

    public DaySchedule getDaySchedule(String day) {
        DaySchedule daysch = null;

        for (int i = 0; i < this.daysched.size(); i++) {
            if (this.daysched.get(i).getName().equals(day))
                daysch = this.daysched.get(i);
        }

        return daysch;
    }

    public DaySchedule getDaySchedule(int day) {
        DaySchedule daysch = null;

        if (this.daysched != null) {
            for (int i = 0; i < this.daysched.size(); i++) {
                if (this.daysched.get(i).getDay() == day)
                    daysch = this.daysched.get(i);
            }
        }
        return daysch;
    }

    public void removeDaySchedule(int selected) {
        this.daysched.remove(selected);

        if (this.daysched.size() == 0) {
            this.daysched = null;
        }
    }

    public boolean isActive() {
        Calendar cal = Calendar.getInstance();

        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int day = cal.get(Calendar.DAY_OF_WEEK);

        //System.out.println(hour + ":" + minute + " Viikonp�iv�: " + day);
        //System.out.println(sdf.format(cal.getTime()));

        this.active = false;

        for (int i = 0; i < this.daysched.size(); i++) {
            if (this.daysched.get(i).getDay() == day) {
                //System.out.println("p�iv� t�sm��");
                this.active = this.daysched.get(i).isActive(hour, minute);
            }
        }

        return this.active;
    }
}
