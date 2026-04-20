package game;

public enum BuildingType {
    // bangunan dan ukurannya (width x height dalam tile)
    //  tinggal tambahin bangunan baru di enum ini, terus buat gambar normal dan hover nya di Toolbar.java
    // dan button otomatis akan muncul di toolbar, tinggal atur urutannya aja di enum ini
    HOUSE(1, 1),
    BUILDING_2X2(2, 2),
    BUILDING_2X4(2, 4);

    private final int width;
    private final int height;

    BuildingType(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
