package ru.rian.riamessenger.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by Roman on 7/9/2015.
 */

@Table(name = "RosterEntryModels")
public class RosterEntryModel extends Model {
    // This is the unique id given by the server
    //@Column(name = "remote_id", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    //public long remoteId;
    // This is a regular field
    @Column(name = "Name", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String name;
    // This is an association to another activeandroid model
    @Column(name = "RosterGroupModel", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public RosterGroupModel rosterGroupModel;

    // Make sure to have a default constructor for every ActiveAndroid model
    public RosterEntryModel(){
        super();
    }

    public RosterEntryModel(/*int remoteId,*/ String name, RosterGroupModel rosterGroupModel){
        super();
        //this.remoteId = remoteId;
        this.name = name;
        this.rosterGroupModel = rosterGroupModel;
    }
}
