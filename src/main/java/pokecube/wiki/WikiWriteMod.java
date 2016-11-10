package pokecube.wiki;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;

@Mod(modid = WikiWriteMod.MODID, name = "wikiwriter", version = WikiWriteMod.VERSION, dependencies = "required-after:pokecube", acceptableRemoteVersions = "*", acceptedMinecraftVersions = WikiWriteMod.MCVERSIONS)
public class WikiWriteMod
{

    Map<PokedexEntry, Integer> genMap     = Maps.newHashMap();
    public static final String MODID      = "pokecube_wikioutput";
    public static final String VERSION    = "@VERSION@";

    public final static String MCVERSIONS = "[1.9.4]";

    @Instance(value = MODID)
    public static WikiWriteMod instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        if (event.getSide() == Side.CLIENT)
        {
            MinecraftForge.EVENT_BUS.register(this);
        }
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
    }

    @EventHandler
    public void serverLoad(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandBase()
        {
            @Override
            public String getCommandUsage(ICommandSender sender)
            {
                return "/pokewiki stuff";
            }

            @Override
            public String getCommandName()
            {
                return "pokewiki";
            }

            @Override
            public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
            {
                EntityPlayer player = getCommandSenderAsPlayer(sender);
                if (args.length == 1 && args[0].equals("all"))
                {
                    PokecubeWikiWriter.writeWiki();
                }
                else if (args.length >= 2 && args[0].equals("img"))
                {
                    boolean shiny = args.length == 3 && args[2].equalsIgnoreCase("S");
                    boolean all = args[1].equalsIgnoreCase("all");
                    if (server instanceof IntegratedServer)
                    {
                        GuiGifCapture.shiny = shiny;
                        PokedexEntry init = Pokedex.getInstance().getFirstEntry();
                        if (all)
                        {

                        }
                        else
                        {
                            init = Database.getEntry(args[1]);
                        }
                        if (init == null) throw new CommandException("Error in pokedex entry for " + args[2]);
                        PokecubeWikiWriter.one = !all;
                        PokecubeWikiWriter.gifs = false;
                        PokecubeWikiWriter.beginGifCapture();
                        GuiGifCapture.pokedexEntry = init;
                        Minecraft.getMinecraft().thePlayer.openGui(instance, 0, player.worldObj, 0, 0, 0);
                    }
                }
            }
        });
    }

    @SidedProxy
    public static CommonProxy proxy;

    public static class CommonProxy implements IGuiHandler
    {
        void setupModels()
        {
        }

        @Override
        public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return null;
        }

        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return null;
        }
    }

    public static class ServerProxy extends CommonProxy
    {
    }

    public static class ClientProxy extends CommonProxy
    {
        @Override
        public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
        {
            return new GuiGifCapture(null, player);
        }
    }
}
