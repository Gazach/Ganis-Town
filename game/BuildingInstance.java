package game;

// Kelas ini untuk merepresentasikan sebuah bangunan yang sudah ditempatkan di world map, dengan tipe dan nama tertentu
public class BuildingInstance {
    private final BuildingType type;
    private String name;

    public BuildingInstance(BuildingType type, String name) {
        this.type = type;
        this.name = name;
    }

    public BuildingType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
