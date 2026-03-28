package game.tile;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
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
        mapTileNum = new int [gp.maxWorldCol][gp.maxWorldRow];
        getTileIMage();
        loadMap("/asset/maps/worldmap.txt");

    }
    public void getTileIMage(){
        try{
            //ini untuk mengeluarkan tilenya kalo 0 itu grass, 1 itu wall, 2 itu water
            tile[0] = new Tile();
            tile[0].image = ImageIO.read(getClass().getResourceAsStream("/asset/tiles/grass.png"));
            tile[1] = new Tile();
            tile[1].image = ImageIO.read(getClass().getResourceAsStream("/asset/tiles/wall.png"));
            tile[2] = new Tile();
            tile[2].image = ImageIO.read(getClass().getResourceAsStream("/asset/tiles/water.png"));
            tile[3] = new Tile();
            tile[3].image = ImageIO.read(getClass().getResourceAsStream("/asset/tiles/dirt.png"));
            tile[4] = new Tile();
            tile[4].image = ImageIO.read(getClass().getResourceAsStream("/asset/tiles/tree.png"));
            tile[5] = new Tile();
            tile[5].image = ImageIO.read(getClass().getResourceAsStream("/asset/tiles/sand.png"));




        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void loadMap(String filePath){
        //ini untuk ngebuat map dari map01.txt tersebut 
        try{
            InputStream is = getClass().getResourceAsStream(filePath);
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
        // ini gambar tile pada x:0 y:0 yang bertambah 1 setiap udah menggambarkan jadi nanti 1 dan 1 dan seterusnya
        if(gp.player == null)return;
        int Worldcol = 0;
        int Worldrow = 0;
    

        while(Worldcol < gp.maxWorldCol && Worldrow < gp.maxWorldRow){
            // worldX dan worldY itu dunianya
            // screenX dan screenY itu kamera kamu di dunia itu 
            int tileNum = mapTileNum[Worldcol][Worldrow];
            int worldX = Worldcol * gp.tileSize;
            int worldY = Worldrow * gp.tileSize;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;
            // untuk biar gambarnya hanya ngerender yang ada di layar user jadi biar mulus performa
            if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                worldX - gp.tileSize< gp.player.worldX + gp.player.screenX &&
                worldY + gp.tileSize> gp.player.worldY - gp.player.screenY &&
                worldY - gp.tileSize< gp.player.worldY + gp.player.screenY){

                    


            g2.drawImage(tile[tileNum].image, screenX, screenY, gp.tileSize, gp.tileSize, null);
                    }
            Worldcol++;

            //setiap udah nyentuh max Collumnnya yaitu 16 bakal ke reset balik ke 0
            if(Worldcol == gp.maxWorldCol){
                Worldcol = 0;
                Worldrow++;


            }
        }
        

        
    }


}
