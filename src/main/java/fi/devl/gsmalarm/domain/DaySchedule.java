/**
 * <CODE>DaySchedule</CODE>
 * <p>
 * class with daily schedules (ie. for monday or tuesday)
 * <p>
 * ChangeLog:
 * <p>
 * v0.1 - Initial version (9.3.2010)
 * v0.2 - isActive method now functional (10.5.2010)
 * v0.3 - added removeSchedule(int index) method (19.6.2010)
 *
 * @author Juha-Matti Sironen
 * @version 0.3
 * @date 19.6.2010
 */

package fi.devl.gsmalarm.domain;

import java.util.ArrayList;

public class DaySchedule {

    private String name;
    private int daynum;
    private ArrayList<String> onTime;
    private ArrayList<String> offTime;
    private ArrayList<String> onTimeValue;

    public DaySchedule(String name) {
        this.name = name;
        this.daynum = -1;
        this.onTime = new ArrayList<>();
        this.offTime = new ArrayList<>();
        this.onTimeValue = new ArrayList<>();
    }

    public void setName(String newName) {
        this.name = newName;
    }

    public String getName() {
        return this.name;
    }

    public int numberOfSchedules() {
        return this.onTime.size();
    }

    public void setDay(int num) {
        this.daynum = num;
    }

    public int getDay() {
        return this.daynum;
    }

    public int getOnHour(int schedn) {
        String[] ont;
        int onh = -1;

        if (this.onTime.get(schedn) != null) {
            ont = this.onTime.get(schedn).split(":");
            onh = Integer.parseInt(ont[0]);
        }

        return onh;
    }

    public void setOnTime(int schedn, String time) {
        this.onTime.set(schedn, time);
    }

    public void setOffTime(int schedn, String time) {
        this.offTime.set(schedn, time);
    }

    public int getOnMinute(int schedn) {
        String[] ont;
        int onm = -1;

        if (this.onTime.get(schedn) != null) {
            ont = this.onTime.get(schedn).split(":");
            onm = Integer.parseInt(ont[1]);
        }

        return onm;
    }

    public int getOffHour(int schedn) {
        String[] offt;
        int offh = -1;

        if (this.offTime.get(schedn) != null) {
            offt = this.offTime.get(schedn).split(":");
            offh = Integer.parseInt(offt[0]);
        }
        return offh;
    }

    public int getOffMinute(int schedn) {
        String[] offt;
        int offm = -1;

        if (this.offTime.get(schedn) != null) {
            offt = this.offTime.get(schedn).split(":");
            offm = Integer.parseInt(offt[1]);
        }
        return offm;
    }

    public boolean addSchedule(String onTime, String offTime, String onTimeValue) {
        this.onTime.add(onTime);
        this.offTime.add(offTime);
        this.onTimeValue.add(onTimeValue);
        return true;
    }

    public boolean removeSchedule(int index) {
        if (index >= 0) {
            this.onTime.remove(index);
            this.offTime.remove(index);
            this.onTimeValue.remove(index);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeSchedule(String onTime, String offTime, String onTimeValue) {
        int i;

        for (i = 0; i < this.onTime.size(); i++) {
            if (onTime.equals(this.onTime.get(i))) {
                this.onTime.remove(i);
                this.offTime.remove(i);
                this.onTimeValue.remove(i);
            }
        }
        return true;
    }

    boolean isActive(int curh, int curm) {
        boolean act = false;
        int onh;
        int onm;
        int offh;
        int offm;
        String[] schedon;
        String[] schedoff;

        for (int i = 0; i < this.onTime.size(); i++) {
            schedon = this.onTime.get(i).split(":");
            schedoff = this.offTime.get(i).split(":");

            onh = Integer.parseInt(schedon[0]);
            onm = Integer.parseInt(schedon[1]);

            offh = Integer.parseInt(schedoff[0]);
            offm = Integer.parseInt(schedoff[1]);

            if (curh == onh && curm >= onm)
                act = true;
            if (curh > onh && curh < offh)
                act = true;
            if (curh == offh && curm <= offm)
                act = true;

        }

        return act;
    }
}
