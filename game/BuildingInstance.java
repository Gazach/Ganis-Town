package game;

// Kelas ini untuk merepresentasikan sebuah bangunan yang sudah ditempatkan di world map, dengan tipe dan nama tertentu
public class BuildingInstance {
    private final BuildingType type;
    private String name;
    // Jumlah penduduk yang diberikan oleh bangunan ini (hanya relevan untuk kategori HOUSING)
    private int population;
    // Urutan penempatan bangunan, digunakan untuk prioritas worker fill
    private int placementOrder;

    public BuildingInstance(BuildingType type, String name) {
        this.type = type;
        this.name = name;
        this.population = 0;
        this.placementOrder = -1;
    }

    public BuildingInstance(BuildingType type, String name, int population) {
        this.type = type;
        this.name = name;
        this.population = population;
        this.placementOrder = -1;
    }

    public BuildingInstance(BuildingType type, String name, int population, int placementOrder) {
        this.type = type;
        this.name = name;
        this.population = population;
        this.placementOrder = placementOrder;
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

    public int getPopulation() {
        return population;
    }

    public void setPopulation(int population) {
        this.population = population;
    }

    public int getPlacementOrder() {
        return placementOrder;
    }

    public void setPlacementOrder(int placementOrder) {
        this.placementOrder = placementOrder;
    }
}
