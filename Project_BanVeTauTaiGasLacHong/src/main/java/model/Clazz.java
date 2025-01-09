package model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.Set;

@Entity
public class Clazz {
    @Id
    private String id;
    private int noOfStudents;

    @OneToMany(mappedBy = "clazz")
    private Set<Student> students;

    public Clazz(){

    }

    public Clazz(String id, int noOfStudents) {
        this.id = id;
        this.noOfStudents = noOfStudents;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNoOfStudents() {
        return noOfStudents;
    }

    public void setNoOfStudents(int noOfStudents) {
        this.noOfStudents = noOfStudents;
    }

    public Set<Student> getStudents() {
        return students;
    }

    public void setStudents(Set<Student> students) {
        this.students = students;
    }
}
