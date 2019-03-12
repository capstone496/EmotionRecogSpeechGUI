package retrofit.model;

public class User {
    private Integer id;
    private String name;
    private String email;

    //constructor
    public User(String name, String email){
        this.name = name;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }


}
