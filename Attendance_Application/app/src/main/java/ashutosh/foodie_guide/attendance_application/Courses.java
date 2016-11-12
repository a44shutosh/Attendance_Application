package ashutosh.foodie_guide.attendance_application;

/**
 * Created by Ashutosh on 08/11/2016.
 */
public class Courses {
    private String name;
    private int numOfSongs;
    private int thumbnail;

    public Courses() {
    }

    public Courses(String name, int thumbnail) {
        this.name = name;
        this.thumbnail = thumbnail;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }

}
