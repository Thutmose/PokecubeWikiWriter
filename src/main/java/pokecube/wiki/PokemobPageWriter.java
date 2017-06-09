package pokecube.wiki;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;

import com.google.common.collect.Lists;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import pokecube.compat.Compat;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.database.PokedexEntry.EvolutionData;
import pokecube.core.database.PokedexEntry.SpawnData;
import pokecube.core.database.PokedexEntry.SpawnData.SpawnEntry;
import pokecube.core.database.SpawnBiomeMatcher;
import pokecube.core.moves.MovesUtils;
import pokecube.core.utils.PokeType;
import thut.api.terrain.BiomeType;

public class PokemobPageWriter extends PokecubeWikiWriter
{
    static void outputPokemonWikiInfo(PokedexEntry entry)
    {
        try
        {
            String fileName = Compat.CUSTOMSPAWNSFILE.replace("spawns.xml",
                    "pokemobs/" + pagePrefix + File.separator + pagePrefix + entry.getName() + ".md");
            File temp = new File(fileName.replace(pagePrefix + entry.getName() + ".md", ""));
            if (!temp.exists())
            {
                temp.mkdirs();
            }
            fwriter = new FileWriter(fileName);
            out = new PrintWriter(fwriter);
            int reference = 1;
            ArrayList<String> refs = Lists.newArrayList();
            String typeString = WordUtils.capitalize(PokeType.getTranslatedName(entry.getType1()));
            if (entry.getType2() != PokeType.unknown)
                typeString += "/" + WordUtils.capitalize(PokeType.getTranslatedName(entry.getType2()));

            // Print links to other pokemon
            PokedexEntry nex = Pokedex.getInstance().getNext(entry, 1);
            PokedexEntry pre = Pokedex.getInstance().getPrevious(entry, 1);
            out.println("| | | ");
            out.println("| --- | --- | ");
            String otherPokemon = "<- | ->";
            String next = "";
            if (nex != entry)
            {
                // reference++;
                next = referenceLink(nex.getTranslatedName(), nex.getTranslatedName());
                // refs.add(pokemobDir + pagePrefix + nex.getName());
            }
            String prev = "";
            if (pre != entry)
            {
                // reference++;
                prev = referenceLink(pre.getTranslatedName() + "", pre.getTranslatedName());
                // refs.add(pokemobDir + pagePrefix + pre.getName());
            }
            otherPokemon = "| " + prev + otherPokemon + next + " |";

            out.println(otherPokemon);

            // Print the name and header
            out.println("# " + entry.getTranslatedName());
            String numString = entry.getPokedexNb() + "";
            if (entry.getPokedexNb() < 10) numString = "00" + numString;
            else if (entry.getPokedexNb() < 100) numString = "0" + numString;
            out.println("| |");
            out.println("| --- |");
            out.println("| " + I18n.format("pokemob.type", typeString) + "\n" + I18n.format("pokemob.number", numString)
                    + "| \n");
            if (entry.hasShiny)
            {
                out.println(referenceImg((reference++) + "") + referenceImg((reference++) + ""));
                refs.add(gifDir + entry.getName() + ".png");
                refs.add(gifDir + entry.getName() + "S.png");
            }
            else
            {
                out.println(referenceImg((reference++) + ""));
                refs.add(gifDir + entry.getName() + ".png");
            }

            // Print the description
            out.println("## " + I18n.format("pokemob.description.header"));
            out.println(I18n.format("pokemob.description.type", entry.getTranslatedName(), typeString));
            if (entry.canEvolve())
            {
                for (EvolutionData d : entry.evolutions)
                {
                    if (d.evolution == null) continue;
                    nex = d.evolution;
                    String evoLink = referenceLink(nex.getTranslatedName(), nex.getTranslatedName());
                    // refs.add(pokemobDir + pagePrefix + nex.getName());
                    String evoString = null;
                    if (d.level > 0)
                    {
                        evoString = I18n.format("pokemob.description.evolve.level", entry.getTranslatedName(), evoLink,
                                d.level);
                    }
                    else if (d.item != null && d.gender == 0)
                    {
                        evoString = I18n.format("pokemob.description.evolve.item", entry.getTranslatedName(), evoLink,
                                d.item.getDisplayName());
                    }
                    else if (d.item != null && d.gender == 1)
                    {
                        evoString = I18n.format("pokemob.description.evolve.item.male", entry.getTranslatedName(),
                                evoLink, d.item.getDisplayName());
                    }
                    else if (d.item != null && d.gender == 2)
                    {
                        evoString = I18n.format("pokemob.description.evolve.item.female", entry.getTranslatedName(),
                                evoLink, d.item.getDisplayName());
                    }
                    else if (d.traded && d.item != null)
                    {
                        evoString = I18n.format("pokemob.description.evolve.traded.item", entry.getTranslatedName(),
                                evoLink, d.item.getDisplayName());
                    }
                    else if (d.happy)
                    {
                        evoString = I18n.format("pokemob.description.evolve.happy", entry.getTranslatedName(), evoLink);
                    }
                    else if (d.traded)
                    {
                        evoString = I18n.format("pokemob.description.evolve.traded", entry.getTranslatedName(),
                                evoLink);
                    }
                    else if (d.move != null && !d.move.isEmpty())
                    {
                        evoString = I18n.format("pokemob.description.evolve.move", entry.getTranslatedName(), evoLink,
                                MovesUtils.getMoveName(d.move).getUnformattedText());
                    }
                    if (evoString != null) out.print(evoString);
                }
            }
            if (entry.evolvesFrom != null)
            {
                String evoString = referenceLink(entry.evolvesFrom.getTranslatedName(),
                        entry.evolvesFrom.getTranslatedName());
                // refs.add(pokemobDir + pagePrefix +
                // entry.evolvesFrom.getName());
                out.println(I18n.format("pokemob.description.evolve.from", entry.getTranslatedName(), evoString));
            }
            out.println();

            // Print move list
            out.println("## " + I18n.format("pokemob.movelist.title"));
            out.println(I18n.format("pokemob.movelist.header"));
            out.println("| --- | --- | ");
            List<String> moves = Lists.newArrayList(entry.getMoves());
            List<String> used = Lists.newArrayList();
            for (int i = 1; i <= 100; i++)
            {
                List<String> newMoves = entry.getMovesForLevel(i, i - 1);
                if (!newMoves.isEmpty())
                {
                    for (String s : newMoves)
                    {
                        out.println("| " + (i == 1 ? "-" : i) + "| " + MovesUtils.getMoveName(s).getUnformattedText()
                                + "| ");
                        for (String s1 : moves)
                        {
                            if (s1.equalsIgnoreCase(s)) used.add(s1);
                        }
                    }
                }
            }
            moves.removeAll(used);

            if (moves.size() > 0)
            {
                out.println("## " + I18n.format("pokemob.tmlist.title"));
                out.println("|  |  |  |  |");
                out.println("| --- | --- | --- | --- |");
                boolean ended = false;
                int n = 0;
                for (String s : moves)
                {
                    ended = false;
                    out.print("| " + MovesUtils.getMoveName(s).getUnformattedText());
                    if (n % 4 == 3)
                    {
                        out.print("| \n");
                        ended = true;
                    }
                    n++;
                }
                if (!ended)
                {
                    out.print("| \n");
                }
            }
            if (!entry.related.isEmpty())
            {
                out.println("## " + I18n.format("pokemob.breedinglist.title"));
                out.println("|  |  |  |  |");
                out.println("| --- | --- | --- | --- |");
                int n = 0;
                boolean ended = false;
                for (PokedexEntry e : entry.related)
                {
                    if (e == null) continue;
                    ended = false;
                    // out.print("| " + formatPokemobLink(e, refs, (reference++)
                    // + ""));
                    out.print("|" + referenceLink(e.getTranslatedName(), e.getTranslatedName()));
                    if (n % 4 == 3)
                    {
                        out.print("| \n");
                        ended = true;
                    }
                    n++;
                }
                if (!ended)
                {
                    out.print("| \n");
                }
            }
            SpawnData data = entry.getSpawnData();
            if (data == null && entry.getChild() != null)
            {
                data = entry.getChild().getSpawnData();
            }
            if (data != null)
            {
                out.println("## " + I18n.format("pokemob.biomeslist.title"));
                out.println("|  |  |  |  |");
                out.println("| --- | --- | --- | --- |");
                int n = 0;
                boolean ended = false;
                boolean hasBiomes = false;
                Map<SpawnBiomeMatcher, SpawnEntry> matchers = data.matchers;
                List<String> biomes = Lists.newArrayList();
                for (SpawnBiomeMatcher matcher : matchers.keySet())
                {
                    String biomeString = matcher.spawnRule.values.get(SpawnBiomeMatcher.BIOMES);
                    typeString = matcher.spawnRule.values.get(SpawnBiomeMatcher.TYPES);
                    if (biomeString != null) hasBiomes = true;
                    else if (typeString != null)
                    {
                        String[] args = typeString.split(",");
                        BiomeType subBiome = null;
                        for (String s : args)
                        {
                            for (BiomeType b : BiomeType.values())
                            {
                                if (b.name.replaceAll(" ", "").equalsIgnoreCase(s))
                                {
                                    subBiome = b;
                                    break;
                                }
                            }
                            if (subBiome == null) hasBiomes = true;
                            subBiome = null;
                            if (hasBiomes) break;
                        }
                    }
                    if (hasBiomes) break;
                }
                if (hasBiomes) for (ResourceLocation key : Biome.REGISTRY.getKeys())
                {
                    Biome b = Biome.REGISTRY.getObject(key);
                    if (b != null)
                    {
                        if (data.isValid(b)) biomes.add(b.getBiomeName());
                    }
                }
                for (BiomeType b : BiomeType.values())
                {
                    if (data.isValid(b))
                    {
                        biomes.add(b.readableName);
                    }
                }
                for (String s : biomes)
                {
                    ended = false;
                    out.print("| " + s);
                    if (n % 4 == 3)
                    {
                        out.print("| \n");
                        ended = true;
                    }
                    n++;
                }
                if (!ended)
                {
                    out.print("| \n");
                }
            }

            if (!entry.forms.isEmpty())
            {
                out.println("## " + I18n.format("pokemob.alternateformes.title"));
                for (PokedexEntry entry1 : entry.forms.values())
                {
                    typeString = WordUtils.capitalize(PokeType.getTranslatedName(entry1.getType1()));
                    if (entry1.getType2() != PokeType.unknown)
                        typeString += "/" + WordUtils.capitalize(PokeType.getTranslatedName(entry1.getType2()));
                    // Print the name and header
                    out.println("## " + entry1.getTranslatedName());
                    out.println("| |");
                    out.println("| --- |");
                    out.println("| " + I18n.format("pokemob.type", typeString) + " |");
                    if (entry1.hasShiny)
                    {
                        out.println(referenceImg((reference++) + "") + referenceImg((reference++) + ""));
                        refs.add(gifDir + entry1.getName() + ".png");
                        refs.add(gifDir + entry1.getName() + "S.png");
                    }
                    else
                    {
                        out.println(referenceImg((reference++) + ""));
                        refs.add(gifDir + entry1.getName() + ".png");
                    }
                }
            }
            String pokemobs = referenceLink((reference++) + "", I18n.format("list.pokemobs.link"));
            refs.add(pagePrefix + "pokemobList");
            String home = referenceLink((reference++) + "", I18n.format("home.link"));
            refs.add(pagePrefix + "Home");
            out.println("\n" + pokemobs + "-------" + home + "\n");
            for (int i = 0; i < refs.size(); i++)
            {
                out.println("[" + (i + 1) + "]: " + refs.get(i).replace(" ", "%20"));
            }
            out.close();
            fwriter.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
