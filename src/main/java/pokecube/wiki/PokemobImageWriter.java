package pokecube.wiki;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.compat.Compat;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;

public class PokemobImageWriter extends PokecubeWikiWriter
{

    private static boolean            gifCaptureState;
    public static boolean             gifs           = true;
    private static int                currentCaptureFrame;
    private static int                currentPokemob = 1;
    private static int                numberTaken    = 1;
    private static int                WINDOW_XPOS    = 1;
    private static int                WINDOW_YPOS    = 1;
    private static int                WINDOW_WIDTH   = 200;
    private static int                WINDOW_HEIGHT  = 200;
    private static List<PokedexEntry> sortedEntries  = Lists.newArrayList();
    private static int                index          = 0;
    public static boolean             one            = false;

    static private void openPokedex()
    {
        Minecraft.getMinecraft().thePlayer.openGui(WikiWriteMod.instance, 0,
                Minecraft.getMinecraft().thePlayer.getEntityWorld(), 0, 0, 0);
    }

    static private void setPokedexBeginning()
    {
        if (!gifs)
        {
            index = 0;
            sortedEntries.clear();
            sortedEntries.addAll(Database.allFormes);
            Collections.sort(sortedEntries, new Comparator<PokedexEntry>()
            {
                @Override
                public int compare(PokedexEntry o1, PokedexEntry o2)
                {
                    int diff = o1.getPokedexNb() - o2.getPokedexNb();
                    if (diff == 0)
                    {
                        if (o1.base && !o2.base) diff = -1;
                        else if (o2.base && !o1.base) diff = 1;
                    }
                    return diff;
                }
            });
            return;
        }
        GuiGifCapture.pokedexEntry = Pokedex.getInstance().getEntry(1);

    }

    static private void cyclePokedex()
    {
        if (!gifs)
        {
            GuiGifCapture.pokedexEntry = sortedEntries.get(index++);
            return;
        }
        GuiGifCapture.pokedexEntry = Pokedex.getInstance().getNext(GuiGifCapture.pokedexEntry, 1);
        if (GuiGifCapture.pokedexEntry != null) currentPokemob = GuiGifCapture.pokedexEntry.getPokedexNb();
    }

    static public void beginGifCapture()
    {
        if (FMLCommonHandler.instance().getSide() == Side.CLIENT && !gifCaptureState)
        {
            gifCaptureState = true;
            openPokedex();
            setPokedexBeginning();
            System.out.println("Beginning Img capture...");
        }
    }

    static public boolean isCapturingGif()
    {
        return gifCaptureState;
    }

    public static void setCaptureTarget(int number)
    {
        GuiGifCapture.pokedexEntry = Database.getEntry(number);
    }

    static public void doCapturePokemobGif()
    {
        if (gifCaptureState && FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
        {
            doCapturePokemobGifClient();
        }
    }

    static private void doCapturePokemobGifClient()
    {
        int h = Minecraft.getMinecraft().displayHeight;
        int w = Minecraft.getMinecraft().displayWidth;
        int x = w / 2;
        int y = h / 2;

        WINDOW_XPOS = -250;
        WINDOW_YPOS = -250;
        WINDOW_WIDTH = 120;
        WINDOW_HEIGHT = 120;
        int xb, yb;

        xb = GuiGifCapture.x;
        yb = GuiGifCapture.y;
        int width = WINDOW_WIDTH * w / xb;
        int height = WINDOW_HEIGHT * h / yb;

        x += WINDOW_XPOS;
        y += WINDOW_YPOS;
        if (GuiGifCapture.pokedexEntry != null) currentPokemob = GuiGifCapture.pokedexEntry.getPokedexNb();
        else return;

        int x1 = x;
        int y1 = y;
        // int i1 = 7;
        // int j1 = 3;
        capture:
        if (GuiGifCapture.icon)
        {
            // x = x1 + i1 * 32 + -38;
            // y = y1 + j1 * 32 + 0;
            String tex = GuiGifCapture.pokedexEntry.texturePath.replace("/entity/", "/entity_icon/");
            String pokename = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml",
                    new String("img" + File.separator + currentPokemob + "_"));
            GL11.glReadBuffer(GL11.GL_FRONT);
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            String currentFrameSuffix = new String();

            if (currentCaptureFrame < 10) currentFrameSuffix = "0";

            String shinysuffix = GuiGifCapture.shiny && GuiGifCapture.pokedexEntry.hasShiny ? "s" : "";

            currentFrameSuffix += currentCaptureFrame + shinysuffix + ".png";
            String fileName = pokename + currentFrameSuffix;
            if (!gifs)
            {
                String name = GuiGifCapture.pokedexEntry.getName() + shinysuffix + ".png";
                if (GuiGifCapture.icon) name = name.toLowerCase(Locale.ENGLISH);
                fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml",
                        new String("img" + File.separator + tex + name));
            }
            File file = new File(fileName);
            file.mkdirs();
            int n = 0;

            int x0 = width, y0 = height, xf = 0, yf = 0;
            for (int i = 0; i < width; i++)
            {
                for (int j = 0; j < height; j++)
                {
                    int k = (i + (width * j)) * 4;
                    int r = buffer.get(k) & 0xFF;
                    int g = buffer.get(k + 1) & 0xFF;
                    int b = buffer.get(k + 2) & 0xFF;
                    if (!(r == 1 && g == 2 && b == 3))
                    {
                        x0 = Math.min(i, x0);
                        xf = Math.max(i, xf);
                        y0 = Math.min(j, y0);
                        yf = Math.max(j, yf);
                        n++;
                    }
                }
            }

            int dy = yf - y0;
            int dx = xf - x0;
            int ow = width;
            int oh = height;
            width = dx + 1;// Math.max(dx, dy);
            height = dy + 1;// width;
            System.out.println(width + " " + height + " " + oh + " " + ow + " " + GuiGifCapture.pokedexEntry);
            if (width < 0 || height < 0) break capture;

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            n = 0;
            for (int i = x0; i < x0 + width; i++)
            {
                for (int j = y0; j < y0 + height; j++)
                {
                    int k = (i + (ow * j)) * 4;
                    int r = buffer.get(k) & 0xFF;
                    int g = buffer.get(k + 1) & 0xFF;
                    int b = buffer.get(k + 2) & 0xFF;
                    int a = 0xFF;
                    if (GuiGifCapture.icon && r == 1 && g == 2 && b == 3)
                    {
                        a = 0;
                        n++;
                    }
                    image.setRGB(i - x0, height - (j - y0 + 1), (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
            System.out.println(n + " blanks");

            try
            {
                ImageIO.write(image, "png", file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            x = x1;
            y = y1;

            String pokename = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml",
                    new String("img" + File.separator + currentPokemob + "_"));
            GL11.glReadBuffer(GL11.GL_FRONT);
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
            GL11.glReadPixels(x, y, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

            String currentFrameSuffix = new String();

            if (currentCaptureFrame < 10) currentFrameSuffix = "0";

            String shinysuffix = GuiGifCapture.shiny && GuiGifCapture.pokedexEntry.hasShiny ? "S" : "";

            currentFrameSuffix += currentCaptureFrame + shinysuffix + ".png";
            String fileName = pokename + currentFrameSuffix;
            if (!gifs)
            {
                String name = GuiGifCapture.pokedexEntry.getName() + shinysuffix + ".png";
                fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml", new String("img" + File.separator + name));
            }
            File file = new File(fileName);
            file.mkdirs();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int i = 0; i < width; i++)
            {
                for (int j = 0; j < height; j++)
                {
                    int k = (i + (width * j)) * 4;
                    int r = buffer.get(k) & 0xFF;
                    int g = buffer.get(k + 1) & 0xFF;
                    int b = buffer.get(k + 2) & 0xFF;
                    int a = 0xFF;
                    image.setRGB(i, height - (j + 1), (a << 24) | (r << 16) | (g << 8) | b);
                }
            }

            try
            {
                ImageIO.write(image, "png", file);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        currentCaptureFrame++;
        if (Keyboard.isKeyDown(Keyboard.KEY_HOME))
        {
            currentPokemob = 1;
            numberTaken = 1;
            gifCaptureState = false;
            System.out.println("Img capture Aborted!");
            return;
        }
        if (currentCaptureFrame > 28 || !gifs)
        {
            currentCaptureFrame = 0;
            numberTaken++;
            if ((gifs && numberTaken >= Pokedex.getInstance().getEntries().size())
                    || (!gifs && index >= sortedEntries.size()) || one)
            {
                currentPokemob = 1;
                numberTaken = 1;
                gifCaptureState = false;
                System.out.println("Img capture complete!");
            }
            else cyclePokedex();
        }
    }
}
