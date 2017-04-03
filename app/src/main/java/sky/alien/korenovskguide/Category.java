package sky.alien.korenovskguide;

/**
 * Created by asass on 18.03.2017.
 */

public class Category {
    private String name;
    private int numOfShops;
    private int thumbnail;

    public Category(){

    }
    public Category(String name, int numOfShops, int thumbnail){
        this.name = name;
        this.numOfShops = numOfShops;
        this.thumbnail = thumbnail;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public int getNumOfShops(){
        return numOfShops;
    }
    public void setNumOfShops(int numOfShops){
        this.numOfShops = numOfShops;
    }
    public int getThumbnail(){
        return thumbnail;
    }
    public void setThumbnail(int thumbnail){
        this.thumbnail = thumbnail;
    }
}
