package game.tile;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import javax.imageio.ImageIO;

import system.GamePanel;

public class TileManager {
    GamePanel gp;
    Tile[] tile;
    int mapTileNum[][];

    public TileManager(GamePanel gp){

        this.gp = gp;
        tile = new Tile [10];
        getTileIMage();

    }

    public void setMap(int[][] worldMap) {
        mapTileNum = worldMap;
    }
    public void getTileIMage(){
        try{
            //ini untuk mengeluarkan tilenya kalo 0 itu grass, 1 itu wall, 2 itu water
            tile[0] = new Tile();
            tile[0].image = loadImage("/asset/tiles/grass.png");
            tile[1] = new Tile();
            tile[1].image = loadImage("/asset/tiles/wall.png");
            tile[2] = new Tile();
            tile[2].image = loadImage("/asset/tiles/water.png");
            tile[3] = new Tile();
            tile[3].image = loadImage("/asset/tiles/dirt.png");
            tile[4] = new Tile();
            tile[4].image = loadImage("/asset/tiles/tree.png");
            tile[5] = new Tile();
            tile[5].image = loadImage("/asset/tiles/sand.png");



        }catch(IOException e){
            e.printStackTrace();
        }
    }
    // Ini untuk bisa load file dari resource atau dari file system,
    // jadi kalo misalnya file itu ada di dalam jar atau di luar jar tetap bisa kebaca
    private BufferedImage loadImage(String path) throws IOException {
        InputStream is = getClass().getResourceAsStream(path);// Coba load dari resource terlebih dahulu
        if (is != null) { // Coba load dari resource, jika berhasil, langsung return gambarnya
            return ImageIO.read(is);
        }

        File file = new File(path.startsWith("/") ? path.substring(1) : path); // Coba load dari file system
        if (file.exists()) { // Jika file ada, load dan return gambarnya
            return ImageIO.read(file);
        }

        throw new IOException("Unable to load image: " + path);
    }
    // Ini untuk bisa load file dari resource atau dari file system,
    // jadi kalo misalnya file itu ada di dalam jar atau di luar jar tetap bisa kebaca
    private InputStream openResourceStream(String path) throws IOException {
        InputStream is = getClass().getResourceAsStream(path); 
        if (is != null) { // kalo null berarti file itu ada di resource, jadi langsung return streamnya
            return is;
        }

        File file = new File(path.startsWith("/") ? path.substring(1) : path);
        if (file.exists()) { // kalo file itu ada di file system, jadi langsung return streamnya
            return new FileInputStream(file);
        }

        throw new IOException("Unable to open resource: " + path);
    }

    public void loadMap(String filePath){
        //ini untuk ngebuat map dari map01.txt tersebut 
        try{
            InputStream is = openResourceStream(filePath);
            //BufferedReader dipake untuk ngebaca 0 1 2 nya
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            int col = 0;
            int row =0;

            while(col < gp.maxWorldCol && row < gp.maxWorldRow){
                //String line ini dipake untuk ngebaca isi 1 line di map01.txt yang dijadiin string
                String line = br.readLine();
                while(col < gp.maxWorldCol){
                    //ngebaca 0 1 2 sebagai 0 kalau 0 dan 1 kalau bukan 0
                    String numbers[] = line.split(" ");
                    // ngubah dari string jadi integer
                    int num = Integer.parseInt(numbers[col]);
                    //mapTileNum ini untuk nyimpan data yang udah di jadiin integer tersebut sebagai maptileNum
                    mapTileNum[col][row] = num;
                    col++;
                }
                if(col == gp.maxWorldCol){
                    col = 0;
                    row++;
                }
            }
            br.close();

        }catch(Exception e){
            e.printStackTrace();

        }

    }
    public void draw(Graphics2D g2){
        if (mapTileNum == null) return;

        int Worldcol = 0;
        int Worldrow = 0;

        while(Worldcol < gp.maxWorldCol && Worldrow < gp.maxWorldRow){
            int tileNum = mapTileNum[Worldcol][Worldrow];
            int worldX = Worldcol * gp.tileSize;
            int worldY = Worldrow * gp.tileSize;
            int screenX = worldX - gp.cameraWorldX + gp.besarLayar/2;
            int screenY = worldY - gp.cameraWorldY + gp.tinggiLayar/2;

            if (screenX + gp.tileSize > 0 &&
                screenX < gp.besarLayar &&
                screenY + gp.tileSize > 0 &&
                screenY < gp.tinggiLayar) {

                if (tileNum >= 0 && tileNum < tile.length && tile[tileNum] != null) {
                    g2.drawImage(tile[tileNum].image, screenX, screenY, gp.tileSize, gp.tileSize, null);
                }
            }

            Worldcol++;
            if(Worldcol == gp.maxWorldCol){
                Worldcol = 0;
                Worldrow++;
            }
        }
    }


}
