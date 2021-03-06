package net.wfoas.minecraft.reseditor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import net.wfoas.minecraft.reseditor.TextureCollector.TexMode;

public class MinecraftResEditor {
	public static ResEditorWindow rese;

	public static ImageIcon CR_1, NOTE_1, CR, NOTE;
	static String user, mail, pass;

	public static String getMail() {
		return mail;
	}

	public static String getName() {
		return user;
	}

	public static void read() {
		File folder = new File(System.getenv("appdata") + "/wfoasm-woma-net");
		folder.mkdirs();
		File file = new File(folder, "mc-res-editor.properties");
		if (!file.exists()) {
			new GitHubLoginDialog().setVisible(true);
		} else {
			readCredentials(file);
			startRegular();
			return;
		}
	}

	protected static void readCredentials(File file) {
		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(file));
			user = prop.getProperty("user");
			mail = prop.getProperty("mail");
			pass = prop.getProperty("pass");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveProperties(String user, String mail, String password) {
		Properties pro = new Properties();
		pro.setProperty("user", user);
		pro.setProperty("mail", mail);
		pro.setProperty("pass", password);
		File folder = new File(System.getenv("appdata") + "/wfoasm-woma-net");
		folder.mkdirs();
		try {
			FileOutputStream fos = new FileOutputStream(new File(folder, "mc-res-editor.properties"));
			pro.store(fos, "Do not modify!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ImageIcon downScale(ImageIcon ii) {
		ii.setImage(ii.getImage().getScaledInstance(16, 16, 4));
		return ii;
	}

	public static ImageIcon downScale(ImageIcon ii, int width, int height) {
		ii.setImage(ii.getImage().getScaledInstance(width, height, 4));
		return ii;
	}

	public static ImageIcon readClassImage(String s) {
		return readImage(MinecraftResEditor.class.getResource(s));
	}

	public static ImageIcon readImage(URL url) {
		ImageIcon ii = new ImageIcon(url);
		return ii;
	}

	public static void main(String... args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		CR_1 = readClassImage("/res/CRAFTING_RECIPES.png");
		NOTE_1 = readClassImage("/res/NOTES.png");
		CR = downScale(CR_1, 16, 16);
		NOTE = downScale(NOTE_1, 16, 16);
		read();
	}

	protected static void startRegular() {
		rese = new ResEditorWindow();
		String username = System.getProperty("user.home");
		File f = new File(username, "git/GameHelper");
		if (!f.exists()) {
			f = new File(username, "git/gamehelper-mc-189");
		}
		if (!f.exists()) {
			JOptionPane.showMessageDialog(null,
					"The GameHelper repository wasn't found!" + System.lineSeparator() + "Code: 0x2s", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		rese.openRepository(f);
		rese.redesign();
		rese.setVisible(true);
	}

	protected static String readFileIntoSingleString(File path) {
		try {
			BufferedReader writer = new BufferedReader(
					new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8));
			String ln = null;
			String doc = "";
			while ((ln = writer.readLine()) != null) {
				if (ln.startsWith("//")) {
					continue;
				}
				doc = doc + ln + System.lineSeparator();
			}
			writer.close();
			return doc;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected static void save(File path, String c) {
		if (path.exists())
			path.delete();
		try {
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
			writer.write(c);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected static void delete(File path) {
		path.delete();
	}

	public static void addBlockModelWithI18n(String modid, String blockid, String model, String pathtotex, String ger,
			String eng) {
		addModel(modid, blockid, model, pathtotex);
		addOrReplaceBlock(ger, blockid, modid, "de_DE");
		addOrReplaceBlock(eng, blockid, modid, "en_US");
	}

	public static void addSpecialBlockModelWithI18n(String modid, String blockid, String model,
			TextureCollector textures, String ger, String eng) {
		Properties p = new Properties();
		try {
			FileInputStream fis = new FileInputStream(
					new File(rese.repository, "minecraft-res-editor/special/" + model + ".res-desc"));
			p.load(fis);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		String blockState = p.getProperty("BlockState");
		addJustSpecialModelOrBlockState(modid, blockid,
				new File(rese.repository, "minecraft-res-editor/special/" + blockState), blockid, BLOCKSTATE);
		int mdl_count = Integer.parseInt(p.getProperty("BlockMdlCount"));
		for (int i = 0; i < mdl_count; i++) {
			System.out.println(
					new File(rese.repository, "minecraft-res-editor/special/" + p.getProperty("BlockMdl" + i)));
			addJustSpecialModelOrBlockState(modid, blockid,
					new File(rese.repository, "minecraft-res-editor/special/" + p.getProperty("BlockMdl" + i)),
					p.getProperty("BlockMdlOutName" + i).replace("%%blockid%%", blockid).replace("%%modid%%", modid),
					BLOCKMODEL);
		}
		addJustSpecialModelOrBlockState(modid, blockid,
				new File(rese.repository, "minecraft-res-editor/special/" + p.getProperty("ItemMdl")), blockid,
				ITEMMODEL);
		if (textures.getTm() == TexMode.ALL && p.getProperty("TextureMode").equalsIgnoreCase("all")) {
			addJustSpecialTexture(modid, blockid, textures.getUp_normal_all(),
					p.getProperty("Texture_all").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
		} else if (textures.getTm() == TexMode.SIDES && p.getProperty("TextureMode").equalsIgnoreCase("sides")) {
			addJustSpecialTexture(modid, blockid, textures.getUp_normal_all(),
					p.getProperty("Texture_up").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
			addJustSpecialTexture(modid, blockid, textures.getDown_bottom(),
					p.getProperty("Texture_down").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
			addJustSpecialTexture(modid, blockid, textures.getNorth_front(),
					p.getProperty("Texture_front").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
			addJustSpecialTexture(modid, blockid, textures.getWest_side(),
					p.getProperty("Texture_side").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
		} else if (textures.getTm() == TexMode.SINGLE && p.getProperty("TextureMode").equalsIgnoreCase("single")) {
			addJustSpecialTexture(modid, blockid, textures.getUp_normal_all(),
					p.getProperty("Texture_up").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
			addJustSpecialTexture(modid, blockid, textures.getDown_bottom(),
					p.getProperty("Texture_down").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
			addJustSpecialTexture(modid, blockid, textures.getNorth_front(),
					p.getProperty("Texture_north").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
			addJustSpecialTexture(modid, blockid, textures.getWest_side(),
					p.getProperty("Texture_west").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
			addJustSpecialTexture(modid, blockid, textures.getEast(),
					p.getProperty("Texture_east").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
			addJustSpecialTexture(modid, blockid, textures.getSouth(),
					p.getProperty("Texture_south").replace("%%blockid%%", blockid).replace("%%modid%%", modid));
		}
		addOrReplaceBlock(ger, blockid, modid, "de_DE");
		addOrReplaceBlock(eng, blockid, modid, "en_US");
	}

	public static void addItemModelWithI18n(String modid, String itemid, String model, String pathtotex, String ger,
			String eng) {
		addItemOnlyModel(modid, itemid, model, pathtotex);
		addOrReplaceItem(ger, itemid, modid, "de_DE");
		addOrReplaceItem(eng, itemid, modid, "en_US");
	}

	public static void addItemOnlyModel(String modid, String itemid, String model, String pathtotex) {
		File imf = new File(rese.repository, "minecraft-res-editor/item-models/" + model + ".gh_mdl");
		String bimdl = readFileIntoSingleString(imf);
		bimdl = bimdl.replaceAll("%%modid%%", modid);
		bimdl = bimdl.replaceAll("%%itemid%%", itemid);
		save(new File(rese.repository, "src/main/resources/assets/gamehelper/models/item/" + itemid + ".json"), bimdl);
		//
		try {
			Files.copy(new File(pathtotex).toPath(),
					new File(rese.repository, "src/main/resources/assets/gamehelper/textures/items/" + itemid + ".png")
							.toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static final byte BLOCKSTATE = 0, BLOCKMODEL = 1, ITEMMODEL = 2;

	public static void addJustSpecialModelOrBlockState(String modid, String blockid, File reseditor_mdl, String name,
			byte blockstate_blockmodel_itemmodel) {
		String bmdl = readFileIntoSingleString(reseditor_mdl);
		bmdl = bmdl.replaceAll("%%modid%%", modid);
		bmdl = bmdl.replaceAll("%%blockid%%", blockid);
		if (blockstate_blockmodel_itemmodel == BLOCKSTATE)
			save(new File(rese.repository, "src/main/resources/assets/gamehelper/blockstates/" + name + ".json"), bmdl);
		if (blockstate_blockmodel_itemmodel == BLOCKMODEL)
			save(new File(rese.repository, "src/main/resources/assets/gamehelper/models/block/" + name + ".json"),
					bmdl);
		if (blockstate_blockmodel_itemmodel == ITEMMODEL)
			save(new File(rese.repository, "src/main/resources/assets/gamehelper/models/item/" + name + ".json"), bmdl);
	}

	public static void addJustSpecialTexture(String modid, String blockid, String texture, String texture_name) {
		try {
			Files.copy(new File(texture).toPath(),
					new File(rese.repository,
							"src/main/resources/assets/gamehelper/textures/blocks/" + texture_name + ".png").toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void addModel(String modid, String blockid, String model, String pathtotex) {
		File langFile = new File(rese.repository, "minecraft-res-editor/models/" + model + ".gh_mdl");
		String bmdl = readFileIntoSingleString(langFile);
		bmdl = bmdl.replaceAll("%%modid%%", modid);
		bmdl = bmdl.replaceAll("%%blockid%%", blockid);
		save(new File(rese.repository, "src/main/resources/assets/gamehelper/models/block/" + blockid + ".json"), bmdl);
		//
		File imf = new File(rese.repository, "minecraft-res-editor/item-models/" + model + ".gh_mdl");
		String bimdl = readFileIntoSingleString(imf);
		bimdl = bimdl.replaceAll("%%modid%%", modid);
		bimdl = bimdl.replaceAll("%%blockid%%", blockid);
		save(new File(rese.repository, "src/main/resources/assets/gamehelper/models/item/" + blockid + ".json"), bimdl);
		//
		File bs = new File(rese.repository, "minecraft-res-editor/blockstates/" + model + ".gh_bs");
		String bibs = readFileIntoSingleString(bs);
		bibs = bibs.replaceAll("%%modid%%", modid);
		bibs = bibs.replaceAll("%%blockid%%", blockid);
		save(new File(rese.repository, "src/main/resources/assets/gamehelper/blockstates/" + blockid + ".json"), bibs);
		try {
			Files.copy(new File(pathtotex).toPath(),
					new File(rese.repository,
							"src/main/resources/assets/gamehelper/textures/blocks/" + blockid + ".png").toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void addTextString(File f, String blockid, String modid, String i18n, String eng_ger) {
		String doc = null;
		try {
			BufferedReader writer = new BufferedReader(
					new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
			String ln = null;
			doc = "";
			while ((ln = writer.readLine()) != null) {
				doc = doc + ln + System.lineSeparator();
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] sep = doc.split(System.lineSeparator());
		try {
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(f, false), StandardCharsets.UTF_8));
			for (String ds : sep) {
				bw.write(ds);
				bw.newLine();
			}
			bw.write(i18n + "=" + eng_ger);
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteTextStringBlock(File f, String blockid, String modid, String i18n) {
		String doc = null;
		try {
			BufferedReader writer = new BufferedReader(
					new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
			String ln = null;
			doc = "";
			while ((ln = writer.readLine()) != null) {
				doc = doc + ln + System.lineSeparator();
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] sep = doc.split(System.lineSeparator());
		List<String> sll = new ArrayList<String>();
		for (String s : sep) {
			if (s.toLowerCase().startsWith("tile." + modid + "." + blockid + ".name")) {
				continue;
			} else
				sll.add(s);
		}
		try {
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(f, false), StandardCharsets.UTF_8));
			for (String ds : sll) {
				bw.write(ds);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteTextStringitem(File f, String itemid, String modid, String i18n) {
		String doc = null;
		try {
			BufferedReader writer = new BufferedReader(
					new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8));
			String ln = null;
			doc = "";
			while ((ln = writer.readLine()) != null) {
				doc = doc + ln + System.lineSeparator();
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] sep = doc.split(System.lineSeparator());
		List<String> sll = new ArrayList<String>();
		for (String s : sep) {
			if (s.toLowerCase().startsWith("item." + modid + "." + itemid + ".name")) {
				continue;
			} else
				sll.add(s);
		}
		try {
			BufferedWriter bw = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(f, false), StandardCharsets.UTF_8));
			for (String ds : sll) {
				bw.write(ds);
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void addOrReplaceItem(String i18n, String itemid, String modid, String file) {
		File langFile = new File(rese.repository, "src/main/resources/assets/gamehelper/lang/" + file + ".lang");
		deleteTextStringitem(langFile, itemid, modid, i18n);
		addTextString(langFile, itemid, modid, "item." + modid + "." + itemid + ".name", i18n);
	}

	public static void addOrReplaceBlock(String i18n, String blockid, String modid, String file) {
		File langFile = new File(rese.repository, "src/main/resources/assets/gamehelper/lang/" + file + ".lang");
		deleteTextStringBlock(langFile, blockid, modid, i18n);
		addTextString(langFile, blockid, modid, "tile." + modid + "." + blockid + ".name", i18n);
	}

	public static void deleteItemModel(String itemid) {
		int choix = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete model '" + itemid + "'?");
		if (choix == JOptionPane.YES_OPTION) {
			delete(new File(rese.repository, "src/main/resources/assets/gamehelper/models/item/" + itemid + ".json"));
			delete(new File(rese.repository, "src/main/resources/assets/gamehelper/textures/items/" + itemid + ".png"));
			deleteTextStringitem(new File(rese.repository, "src/main/resources/assets/gamehelper/lang/de_DE.lang"),
					itemid, "gamehelper", "item.gamehelper." + itemid + ".name");
			deleteTextStringitem(new File(rese.repository, "src/main/resources/assets/gamehelper/lang/en_US.lang"),
					itemid, "gamehelper", "item.gamehelper." + itemid + ".name");
		} else
			return;
	}

	public static void deleteBlockModel(String blockid) {
		int choix = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete model '" + blockid + "'?");
		if (choix == JOptionPane.YES_OPTION) {
			delete(new File(rese.repository, "src/main/resources/assets/gamehelper/models/block/" + blockid + ".json"));
			delete(new File(rese.repository, "src/main/resources/assets/gamehelper/models/item/" + blockid + ".json"));
			delete(new File(rese.repository, "src/main/resources/assets/gamehelper/blockstates/" + blockid + ".json"));
			delete(new File(rese.repository,
					"src/main/resources/assets/gamehelper/textures/blocks/" + blockid + ".png"));
			deleteTextStringBlock(new File(rese.repository, "src/main/resources/assets/gamehelper/lang/de_DE.lang"),
					blockid, "gamehelper", "tile.gamehelper." + blockid + ".name");
			deleteTextStringBlock(new File(rese.repository, "src/main/resources/assets/gamehelper/lang/en_US.lang"),
					blockid, "gamehelper", "tile.gamehelper." + blockid + ".name");
		} else
			return;
	}

	protected static String slgh_name;

	protected static void readName() {
		File f = new File(rese.repository, "slgh_proj/project.slgh_proj");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
			String str = null;
			List<String> slist = new ArrayList<String>();
			while ((str = reader.readLine()) != null) {
				if (str.startsWith("#") || str.endsWith("#"))
					continue;
				slist.add(str);
			}
			reader.close();
			String proj_name = null;
			for (String s : slist) {
				if (s.startsWith("main_proj")) {
					proj_name = s.split(":")[1];
				}
			}
			if (proj_name == null)
				return;
			for (String s : slist) {
				if (s.startsWith(proj_name)) {
					slgh_name = s.split(":")[1];
					return;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String slghGetProjectName() {
		return "[SLGH] " + slgh_name;
	}
}