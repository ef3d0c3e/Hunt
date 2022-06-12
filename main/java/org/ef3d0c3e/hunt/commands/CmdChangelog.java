package org.ef3d0c3e.hunt.commands;

import java.text.MessageFormat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.ef3d0c3e.hunt.Messager;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public class CmdChangelog
{
	public static boolean command(CommandSender sender, Command cmd, String label, String[] args)
	{
		String pages[][] = {
			{ // Page 1
				"",
				" &9 &9 &n4 août 2021&9: &6&oHunt 3.0",
				"",
				"* Mise à jour à la version 1.17.1",
				"- Suppression de modes de jeu::● Plus de kits\n● Plus de teams\n● Plus de mode îles\n● Plus de mode swap\n● Plus de mode run\n● Plus de mode fortnite",
				"+ Ajout du changeur de skin",
				"+ Ajouts des cercueils fonctionnels",
				"* Le système de mort attribue plus justement les kills::● Si un joueur qu'on a attaqué\nmeurt dans les 7 secondes\nsuivantes, le kill nous est attribué",
				"~ Buff de l'épée::↑ L'épée tape aussi vite qu'en 1.8",
				"~ Ajustement de la hache::↑ La hache tape aussi vite qu'en 1.8\n↓ La hache inflige la moitié\nde ses dégâts actuels",
				"+ La border est désormais active dans le nether"
			},
			{ // Page 2
				"",
				" &9 &9 &n16 août 2021&9: &6&oHunt 3.1",
				"",
				"+ Retour du tracker",
				"* Le nether se ferme correctement",
				"+ Retour des kits::● Seulement le kit Estéban\npeut être joué pour l'instant",
				"+ Ajout de l'indicateur de vie",
				"* Refonte de l'interface::● Ajout de nametags indiquant\nnotre équipe\n● Scoreboard légèrement modifié\n● Le TAB est plus propre et affiche\nbeaucoup plus d'informations",
			},
			{ // Page 3
				"",
				" &9 &9 &n25 août 2021&9: &6&oHunt 3.2",
				"",
				"* Correction d'un bug avec le système de morts::● Il était possible d'être tué par\nun joueur dont on n'était pas\nla cible",
				"+ Retour du Kit Mehdi;;/kitinfo mehdi",
				"+ Retour du Kit Jean-Baptiste;;/kitinfo jb",
				"+ Retour du Kit Baptiste;;/kitinfo baptiste",
				"+ Retour du Kit Lino;;/kitinfo lino",
			},
			{ // Page 4
				"",
				" &9 &9 &n8 septembre 2021&9: &6&oHunt 3.3",
				"",
				"+ Retour du Kit Julien;;/kitinfo julien",
				"+ Retour du Kit Kélian;;/kitinfo kelian",
				"+ Retour du Kit Thomas;;/kitinfo thomas",
				"+ Rework du Kit Enzo;;/kitinfo enzo",
			},
			{ // Page 5
				"",
				" &9 &9 &n31 octobre 2021&9: &6&oHunt 3.4",
				"",
				"* Changement du MOTD",
				"+ Ajout du /help;;/help",
				"+ Ajout du /compass;;/compass",
				"+ Ajout du /inv <joueur>::● Permet de voir l'inventaire\nd'un joueur (Spectateur)",
				"+ Ajout du /cibles::● Affiche le roulement des\ncibles (Spectateur)",
				"+ Retour du Kit Tom;;/kitinfo tom",
				"+ Retour du Kit Flavien;;/kitinfo flavien",
				"+ Retour du Kit BlueKatss;;/kitinfo bk",
				"+ Rework du Kit Lanczos;;/kitinfo lanczos",
				"* Fix du Kit JB::● Se déconnecter pendant la\nmalédiction nous fait mourir",
				"* Fix du Kit Enzo::● On obtient les objets/effets\nmême si on est hors ligne",
				"* Correction de bugs liés à la déconnexion::● Un joueur peut être ressuscité\nen étant déconnecté.",
				"+ Les membres d'une équipe se voient en glowing",
				"* Les équipes peuvent avoir des noms espacées",
				"+ Ajout du chat d'équipe::● .<message>",
				"+ Ajout du 'Beacon' en (0, 0)::● Il permet de ressusciter\nun membre de notre équipe\ncontre un Diamant",
				"* Changement de la génération::● Les océans ont été\nremplacés par des plaines"
			},
			{ // Page 6
				"",
				" &9 &9 &n6 janvier 2022&9: &6&oHunt 3.5",
				"",
				"* Mis à jour pour la 1.18.1",
				"+ Ajouts des têtes de joueurs::● En mode normal, elles affichent\n qui est notre chasseur\n● En mode équipe, elles permettent\n de ressusciter un allié",
				"* Le beacon en mode équipe demande une tête de joueur à la place d'un diamant",
				"~ Ajustement du kit Julien::↑ Lorsqu'il tape en ayant de la\n cobblestone, il donne Slowness II (3s)\n↓ Taper avec de la cobble ne rajoute plus\n que +30% de dégâts au lieu de +50%\n↓ Le coût a augmenté de 2\n par niveau de Sharpness",
				"+ Les joueurs du kit Julien peuvent être Milk::● Milk julien lui fait perdre\n ½❤ et donne un Seau de Lait\n● Le lait à ⅚ chance de donner\n un effet positif\n● Et ⅙ chance de donner\n un effet négatif",
				"~ Buff du kit Baptiste::↑ Lorsqu'il réussi ses tirs\n ses flèches font de plus en\n plus de dégâts\n (+10% par flèche réussi, +50% max)",
				"~ Buff du kit Jean-Baptiste::↑ Les Squelettes, Zombies\n et les Piglins Zombifié ne\n l'attaquent plus",
				"+ Ajout du mode Round"
			},
			{ // Page 6
				"",
				" &9 &9 &n7 janvier 2022&9: &6&oHunt 3.6",
				"",
				"* Fix du mode round::● Tuer un joueur en zombie à la fin\n d'un round faisait bug la partie\n● On était TP aux mauvaises coordonnées à\n la fin d'un round",
				"+ Ajout du mode Fast",
			},
			{ // Page 7
				"",
				" &9 &9 &n1er mai 2022&9: &6&oHunt 3.7",
				"",
				"* Mise à jours vers la 1.18.2",
			},
			{ // Page 8
				"",
				" &9 &9 &n29 mai 2022&9: &6&oHunt 3.7.1",
				"",
				"* Corrections de bugs::● Julien n'était pas target par les undeads\n● JB était target par les undeads",
				"+ Les abeilles de Mehdi ont un nom aux couleurs de leur équipe"
			},
			{ // Page 9
				"",
				" &9 &9 &n12 juin 2022&9: &6&oHunt 3.8",
				"",
				"* Réécriture partielle du plugin",
				"* Refonte de l'interface (items)"
			},
		};
		
		int page;
		if (args.length == 0)
			page = pages.length - 1;
		else
		{
			try
			{
				page = Integer.valueOf(args[0]) - 1;
			}
			catch (NumberFormatException e)
			{
				sender.sendMessage("§cVous devez spécifier un numéro de page!");
				return true;
			}
		}
		
		if (page < 0 || page >= pages.length)
		{
			sender.sendMessage(MessageFormat.format("§cLa page ''§e{0}§c'' n'existe pas.", page + 1));
			return true;
		}

		ComponentBuilder navigator = new ComponentBuilder();
		if (page != 0)
		{
			TextComponent arrowLeft = new TextComponent(TextComponent.fromLegacyText("§l §l §c ««« "));
			arrowLeft.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, MessageFormat.format("/ch {0}", page)));
			arrowLeft.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new Text(MessageFormat.format("§7Page {0}", page))
			));
			navigator.append(arrowLeft);
		}
		else
		{
			TextComponent arrowLeft = new TextComponent(TextComponent.fromLegacyText("§l §l §7 ««« "));
			navigator.append(arrowLeft);
		}
		TextComponent numbers = new TextComponent(TextComponent.fromLegacyText(MessageFormat.format(
			"§6§l(§e{0}/{1}§6§l)", page +1 , pages.length
		)));
		navigator.append(numbers);
		if (page+1 != pages.length)
		{
			TextComponent arrowRight = new TextComponent(TextComponent.fromLegacyText("§c »»» "));
			arrowRight.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, MessageFormat.format("/ch {0}", page + 2)));
			arrowRight.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
				new Text(MessageFormat.format("§7Page {0}", page + 2))
			));
			navigator.append(arrowRight);
		}
		else
		{
			TextComponent arrowRight = new TextComponent(TextComponent.fromLegacyText("§7 »»» "));
			navigator.append(arrowRight);
		}
		
		for (int i = 0; i < pages[page].length; ++i)
			sender.spigot().sendMessage(parseEntry(pages[page][i]));
		
		sender.spigot().sendMessage(navigator.create());
		
		return true;
	}
	
	private static Text parseDesc(String desc)
	{
		return new Text(desc
			.replace("↑", "§r§a§l↑§r§a")
			.replace("↓", "§r§c§l↓§r§c")
			.replace("●", "§r§b§l●§r§b")
		);
	}
	
	private static BaseComponent[] parseEntry(String entry)
	{
		ComponentBuilder builder = new ComponentBuilder();

		char prev = '\0';
		for (int i = 0; i < entry.length(); ++i)
		{
			if (entry.charAt(i) == ':' && prev == ':')
			{
				String color = null;
				switch (entry.charAt(0))
				{
				case '*':
					color = "<#2050F0>";
					break;
				case '-':
					color = "<#D06040>";
					break;
				case '+':
					color = "<#20F050>";
					break;
				case '~':
					color = "<#C0D020>";
					break;
				}
				
				if (color != null)
					builder.append(new TextComponent(TextComponent.fromLegacyText(Messager.getColored(" " + color + entry.substring(0, i-1)))));
				else
					builder.append(new TextComponent(TextComponent.fromLegacyText(Messager.getColored(entry.substring(0, i-1)))));
				
				TextComponent desc = new TextComponent(TextComponent.fromLegacyText(" §8[§d§oVoir§8]"));
				desc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, parseDesc(entry.substring(i+1))));
				builder.append(desc);
				
				break;
			}
			else if (entry.charAt(i) == ';' && prev == ';')
			{
				String color = null;
				switch (entry.charAt(0))
				{
					case '*':
						color = "<#2050F0>";
						break;
					case '-':
						color = "<#D06040>";
						break;
					case '+':
						color = "<#20F050>";
						break;
					case '~':
						color = "<#C0D020>";
						break;
				}

				if (color != null)
					builder.append(new TextComponent(TextComponent.fromLegacyText(Messager.getColored(" " + color + entry.substring(0, i-1)))));
				else
					builder.append(new TextComponent(TextComponent.fromLegacyText(Messager.getColored(entry.substring(0, i-1)))));

				TextComponent desc = new TextComponent(TextComponent.fromLegacyText(" §8[§b§o§nVoir§8]"));
				desc.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("§6" + entry.substring(i+1))));
				desc.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, entry.substring(i+1)));
				builder.append(desc);

				break;
			}
			else if (i == entry.length()-1)
			{
				String color = null;
				switch (entry.charAt(0))
				{
				case '*':
					color = "<#2050F0>";
					break;
				case '-':
					color = "<#D06040>";
					break;
				case '+':
					color = "<#20F050>";
					break;
				case '~':
					color = "<#C0D020>";
					break;
				}

				if (color != null)
					builder.append(new TextComponent(TextComponent.fromLegacyText(Messager.getColored(" " + color + entry))));
				else
					builder.append(new TextComponent(TextComponent.fromLegacyText(Messager.getColored(entry))));
				break;
			}
			
			prev = entry.charAt(i);
		}

		return builder.create();
	}
}
