package game;

public enum BuildingType {
    // bangunan dan ukurannya (width x height dalam tile)
    //  tinggal tambahin bangunan baru di enum ini, terus buat gambar normal dan hover nya di Toolbar.java
    // dan button otomatis akan muncul di toolbar, tinggal atur urutannya aja di enum ini
    // harga dan income per second nya juga diatur di enum ini, tinggal sesuaikan dengan ukuran bangunannya
    HOUSE(1, 1, 500, 2),
    BUILDING_2X2(2, 2, 1500, 10),
    BUILDING_2X4(2, 4, 3000, 25);

    private final int width;
    private final int height;
    private final int price;
    private final int incomePerSecond;

    BuildingType(int width, int height, int price, int incomePerSecond) {
        this.width = width;
        this.height = height;
        this.price = price;
        this.incomePerSecond = incomePerSecond;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getPrice() {
        return price;
    }

    public int getIncomePerSecond() {
        return incomePerSecond;
    }
}
