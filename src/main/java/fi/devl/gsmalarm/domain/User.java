/**
 * <CODE>User</CODE>
 * <p>
 * class with methods that represents userdata.
 * <p>
 * ChangeLog:
 * <p>
 * v0.1 - Initial version (10.10.2009)
 * v0.2 - Modified to read userdata from xml-file (20.10.2009)
 * v0.3 - Modified to support new userlist.xml format (10.5.2010)
 * v0.4 - User has now a property of alarm classes (26.5.2010)
 * <p>
 * TODO:
 * - alot
 *
 * @author Juha-Matti Sironen
 * @version 0.4
 * @date 26.05.2010
 */

package fi.devl.gsmalarm.domain;

import java.util.ArrayList;

public class User {

    private String name;
    private String phoneNumber;
    private TimeTable schedule;
    private String id;
    private ArrayList<String> almIds;
    private boolean active;

    public User(String name, String phoneNumber, TimeTable schedule, String id, boolean active) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.schedule = schedule;
        this.id = id;
        this.almIds = new ArrayList<>();
        this.active = active;
    }

    public void changeName(String newName) {
        this.name = newName;
    }

    public void changeNumber(String newNumber) {
        this.phoneNumber = newNumber;
    }

    public void changeSchedule(TimeTable newSchedule) {
        this.schedule = newSchedule;
    }

    public void changeId(String newId) {
        this.id = newId;
    }

    public void changeState(boolean newState) {
        this.active = newState;
    }

    public void addAlmId(String newId) {
        this.almIds.add(newId);
    }

    public boolean hasAlmId(String id) {
        for (int i = 0; i < this.almIds.size(); i++) {
            if (this.almIds.get(i).equalsIgnoreCase(id))
                return true;
        }
        return false;
    }

    public boolean removeAlmId(String id) {
        for (int i = 0; i < this.almIds.size(); i++) {
            if (this.almIds.get(i).equalsIgnoreCase(id)) {
                this.almIds.remove(i);
                return true;
            }
        }
        return false;
    }

    public String getName() {
        return this.name;
    }

    public String getNumber() {
        return this.phoneNumber;
    }

    public String getTimeTableName() {
        if (!(this.schedule == null))
            return this.schedule.getName();
        else
            return "Ei aikataulua";
    }

    public String getTimeTableId() {
        if (!(this.schedule == null))
            return this.schedule.getId();
        else
            return "Ei aikataulua";
    }

    public TimeTable getTimeTable() {
        return this.schedule;
    }

    public String getId() {
        return this.id;
    }

    public ArrayList<String> getAlmIds() {
        return this.almIds;
    }

    public boolean almIdMatch(String id) {
        boolean retval = false;

        for (String almId : this.almIds) {
            if (almId.equals(id)) {
                retval = true;
                break;
            }
        }
        return retval;
    }

    public boolean getState() {
        return this.active;
    }
}
