package com.winlator.cmod.runtime.container;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.winlator.cmod.runtime.display.environment.ImageFs;
import com.winlator.cmod.runtime.wine.PeIconExtractor;
import com.winlator.cmod.shared.io.FileUtils;
import com.winlator.cmod.shared.util.StringUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

public class Shortcut {
  public final Container container;
  public final String name;
  public final String path;
  public final Bitmap icon;
  public final File file;
  public final File iconFile;
  public final String wmClass;
  private final JSONObject extraData = new JSONObject();
  private Bitmap coverArt; // Changed to private to use getter method
  private String customCoverArtPath; // Path to custom cover art
  private String architecture;

  private static final String COVER_ART_DIR =
      "app_data/cover_arts/"; // Removed leading "/" to keep it relative

  // Cap decoded bitmap dimensions to what the UI can ever actually display.
  // Library cards render cover art at roughly 286dp wide; 960px covers that
  // comfortably even on 4x-density screens. Icons are only ever shown at
  // <=64dp, so 192px is already generous headroom.
  // Without this, a user-picked custom cover art (e.g. a phone camera photo,
  // often 3000-4000px wide) gets decoded at FULL resolution into an
  // ARGB_8888 bitmap - tens of MB of heap for a single thumbnail - and this
  // happens once per shortcut, every time the library is scanned.
  private static final int COVER_ART_MAX_DIMENSION = 960;
  private static final int ICON_MAX_DIMENSION = 192;

  // Decodes a bitmap downsampled to fit within maxDimension on its longest
  // side, instead of decoding at native resolution and scaling later (which
  // still pays the full-size allocation cost). Falls back to null on
  // decode failure/OOM instead of crashing the whole shortcut load.
  private static Bitmap decodeSampledBitmapFromFile(String path, int maxDimension) {
    if (path == null) return null;
    try {
      BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
      boundsOptions.inJustDecodeBounds = true;
      BitmapFactory.decodeFile(path, boundsOptions);
      if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null;

      int sampleSize = 1;
      int halfWidth = boundsOptions.outWidth / 2;
      int halfHeight = boundsOptions.outHeight / 2;
      while ((halfWidth / sampleSize) >= maxDimension || (halfHeight / sampleSize) >= maxDimension) {
        sampleSize *= 2;
      }

      BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
      decodeOptions.inSampleSize = sampleSize;
      decodeOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
      return BitmapFactory.decodeFile(path, decodeOptions);
    } catch (OutOfMemoryError | Exception e) {
      Log.w("Shortcut", "Failed to decode bitmap (" + path + "): " + e);
      return null;
    }
  }

  public Shortcut(Container container, File file) {
    this.container = container;
    this.file = file;

    String execArgs = "";
    Bitmap icon = null;
    File iconFile = null;
    String wmClass = "";

    File[] iconDirs = {
      container.getIconsDir(64),
      container.getIconsDir(48),
      container.getIconsDir(32),
      container.getIconsDir(16)
    };
    String section = "";

    int index;
    for (String line : FileUtils.readLines(file)) {
      line = line.trim();
      if (line.isEmpty() || line.startsWith("#")) continue; // Skip empty lines and comments
      if (line.startsWith("[")) {
        section = line.substring(1, line.indexOf("]"));
      } else {
        index = line.indexOf("=");
        if (index == -1) continue;
        String key = line.substring(0, index);
        String value = line.substring(index + 1);

        if (section.equals("Desktop Entry")) {
          if (key.equals("Exec")) execArgs = value;
          if (key.equals("Icon")) {
            for (File iconDir : iconDirs) {
              iconFile = new File(iconDir, value + ".png");
              if (iconFile.isFile()) {
                icon = decodeSampledBitmapFromFile(iconFile.getPath(), ICON_MAX_DIMENSION);
                break;
              }
            }
          }
          if (key.equals("StartupWMClass")) wmClass = value;
        } else if (section.equals("Extra Data")) {
          try {
            extraData.put(key, value);
          } catch (JSONException e) {
          }
        }
      }
    }

    this.name = FileUtils.getBasename(file.getPath());
    this.icon = icon;
    this.iconFile = iconFile;

    String path = execArgs;
    int wineIndex = execArgs.lastIndexOf("wine ");
    if (wineIndex != -1) {
      path = execArgs.substring(wineIndex + 5).trim();
    }

    if (path.startsWith("\"") && path.endsWith("\"")) path = path.substring(1, path.length() - 1);
    path = StringUtils.unescape(path);

    // Normalize DOS paths like D:game.exe to D:\game.exe
    if (path != null && path.matches("^[A-Z]:[^\\\\/].*")) {
      path = path.substring(0, 2) + "\\" + path.substring(2);
    }
    // If it's just "D:", make it "D:\"
    if (path != null && path.matches("^[A-Z]:$")) {
      path = path + "\\";
    }

    this.path = path;
    this.wmClass = wmClass;

    this.customCoverArtPath = getExtra("customCoverArtPath");

    // Load cover art if available
    loadCoverArt();

    Container.checkObsoleteOrMissingProperties(extraData);

    normalizeEmulatorExtrasForArch();
  }

  // If a per-game shortcut overrides the emulator/emulator64 fields, sanitize the
  // override so a stale legacy value (e.g. "Box64" persisted before the
  // arm64ec/x86_64 dropdowns were arch-filtered) doesn't override the
  // already-normalized container value at launch time. Only rewrites in-memory;
  // a real saveData() happens whenever the user next saves shortcut settings.
  private void normalizeEmulatorExtrasForArch() {
    if (container == null) return;
    boolean arm64ec = "arm64ec".equalsIgnoreCase(container.getExtra("wineprefixArch"));

    String emu32 = getExtra("emulator");
    if (emu32 != null && !emu32.isEmpty()) {
      String norm = arm64ec
              ? Container.sanitizeArm64ecEmulator32(emu32)
              : Container.sanitizeX86Emulator(emu32);
      if (!norm.equals(emu32)) putExtra("emulator", norm);
    }

    String emu64 = getExtra("emulator64");
    if (emu64 != null && !emu64.isEmpty()) {
      String norm = arm64ec ? "fexcore" : Container.sanitizeX86Emulator(emu64);
      if (!norm.equals(emu64)) putExtra("emulator64", norm);
    }
  }

  private void loadCoverArt() {
    // 1. Check if we already have a saved path in the metadata
    if (customCoverArtPath != null && !customCoverArtPath.isEmpty()) {
      File customCoverArtFile = new File(customCoverArtPath);
      if (customCoverArtFile.isFile()) {
        this.coverArt = decodeSampledBitmapFromFile(customCoverArtFile.getPath(), COVER_ART_MAX_DIMENSION);
        return;
      }
    }

    // 2. Check the "custom_icons" folder (This is where manual imports save their icons)
    String safeName = this.name.replace("/", "_").replace("\\", "_");
    File customIconFile =
        new File(
            container.getManager().getContext().getFilesDir(), "custom_icons/" + safeName + ".png");
    if (customIconFile.exists()) {
      this.coverArt = decodeSampledBitmapFromFile(customIconFile.getPath(), COVER_ART_MAX_DIMENSION);
      this.customCoverArtPath = customIconFile.getAbsolutePath();
      return;
    }

    // 3. SMART AUTO-DISCOVERY
    // We use the EXE path (this.path) to find the game on your Android storage
    ImageFs imageFs = ImageFs.find(container.getManager().getContext());
    File exeFile = com.winlator.cmod.runtime.wine.WineUtils.getNativePath(this.container, imageFs, this.path);

    if (exeFile != null && exeFile.exists()) {
      // A. Try extracting the real high-quality icon from the EXE
      if (PeIconExtractor.INSTANCE.extractAndSave(exeFile, customIconFile)) {
        this.coverArt = decodeSampledBitmapFromFile(customIconFile.getPath(), COVER_ART_MAX_DIMENSION);
        this.customCoverArtPath = customIconFile.getAbsolutePath();
        putExtra("customCoverArtPath", customCoverArtPath);
        saveData(); // Save it to the file so we don't have to extract it again
        return;
      }
    }

    // 4. Final Fallback: standard cover art location
    File defaultCoverArtFile = new File(COVER_ART_DIR, this.name + ".png");
    if (defaultCoverArtFile.isFile()) {
      this.coverArt = decodeSampledBitmapFromFile(defaultCoverArtFile.getPath(), COVER_ART_MAX_DIMENSION);
    }
  }

  public String getArchitecture() {
    if (architecture == null || architecture.isEmpty()) detectArchitecture();
    return architecture;
  }

  private void detectArchitecture() {
    architecture = getExtra("architecture");
    if (architecture.isEmpty()
        && this.path != null
        && !this.path.isEmpty()
        && !this.path.equalsIgnoreCase("explorer.exe")) {
      ImageFs imageFs = ImageFs.find(container.getManager().getContext());
      File exeFile = com.winlator.cmod.runtime.wine.WineUtils.getNativePath(this.container, imageFs, this.path);
      if (exeFile != null && exeFile.exists() && !exeFile.isDirectory()) {
        architecture = com.winlator.cmod.runtime.wine.PEHelper.getArchitecture(exeFile);
        if (!architecture.isEmpty()) {
          putExtra("architecture", architecture);
          saveData();
        }
      }
    }
  }

  // Getters and setters for coverArt and customCoverArtPath
  public Bitmap getCoverArt() {
    return coverArt;
  }

  public void setCoverArt(Bitmap coverArt) {
    this.coverArt = coverArt;
  }

  public String getCustomCoverArtPath() {
    return customCoverArtPath;
  }

  public void setCustomCoverArtPath(String customCoverArtPath) {
    this.customCoverArtPath = customCoverArtPath;
    putExtra(
        "customCoverArtPath", customCoverArtPath); // Save the custom cover art path to extra data
    saveData(); // Save immediately to ensure persistence
    Log.d(
        "Shortcut",
        "Set and saved custom cover art path: " + customCoverArtPath); // Add a log for debugging
  }

  public String getExtra(String name) {
    return getExtra(name, "");
  }

  public String getExtra(String name, String fallback) {
    try {
      return extraData.has(name) ? extraData.getString(name) : fallback;
    } catch (JSONException e) {
      return fallback;
    }
  }

  public boolean usesContainerDefaults() {
    return "1".equals(getExtra("use_container_defaults", "0"));
  }

  public String getSettingExtra(String name, String containerValue) {
    if (usesContainerDefaults()) return containerValue;
    // A persisted extra of "" (legacy shortcuts or cleared fields) must not shadow
    // the container value. Otherwise graphicsDriver/graphicsDriverConfig resolve to
    // "" and AdrenotoolsManager.setDriverById silently falls back to system drivers.
    String extra = getExtra(name, "");
    return extra.isEmpty() ? containerValue : extra;
  }

  public void putExtra(String name, String value) {
    try {
      if (value != null) {
        extraData.put(name, value);
      } else extraData.remove(name);
    } catch (JSONException e) {
    }
  }

  public void saveData() {
    String content = "[Desktop Entry]\n";
    for (String line : FileUtils.readLines(file)) {
      if (line.contains("[Extra Data]")) break;
      if (!line.contains("[Desktop Entry]") && !line.isEmpty()) content += line + "\n";
    }

    if (extraData.length() > 0) {
      content += "\n[Extra Data]\n";
      Iterator<String> keys = extraData.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        try {
          content += key + "=" + extraData.getString(key) + "\n";
        } catch (JSONException e) {
        }
      }
    }

    // Verify that the file reference is correct
    if (!file.getName().endsWith(".desktop")) {
      Log.e("Shortcut", "Incorrect file reference before saving: " + file.getPath());
      return; // Prevent saving to an incorrect file
    }

    FileUtils.writeString(file, content);

    // Notify the app that a shortcut was added/updated
    if (container != null
        && container.getManager() != null
        && container.getManager().getContext() != null) {
      android.content.Context context = container.getManager().getContext();
      android.content.Intent intent =
          new android.content.Intent(context.getPackageName() + ".SHORTCUT_ADDED");
      intent.putExtra("shortcut_path", file.getPath());
      intent.putExtra("shortcut_added", true);
      context.sendBroadcast(intent);
    }
  }

  public void genUUID() {
    if (getExtra("uuid").equals("")) {
      putExtra("uuid", UUID.randomUUID().toString());
      saveData();
    }
  }

  // Save the custom cover art to the default cover art directory
  public void saveCustomCoverArt(Bitmap coverArt) {
    try {
      File coverArtDir =
          new File(
              container.getRootDir(),
              COVER_ART_DIR); // Ensure the path is relative to the container's root directory
      if (!coverArtDir.exists()) {
        boolean created = coverArtDir.mkdirs();
        if (!created) {
          Log.e(
              "Shortcut", "Failed to create cover art directory: " + coverArtDir.getAbsolutePath());
        }
      }

      File coverFile = new File(coverArtDir, this.name + ".png");
      if (FileUtils.saveBitmapToFile(coverArt, coverFile)) {
        this.coverArt = coverArt; // Update the cover art
        setCustomCoverArtPath(coverFile.getPath()); // Update the path and save data
        Log.d("Shortcut", "Custom cover art saved at: " + coverFile.getPath());
      } else {
        Log.e("Shortcut", "Failed to save custom cover art.");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void removeCustomCoverArt() {
    if (customCoverArtPath != null && !customCoverArtPath.isEmpty()) {
      File customCoverArtFile = new File(customCoverArtPath);

      // Log the path to be deleted
      Log.d("Shortcut", "Removing custom cover art file at: " + customCoverArtPath);

      // Delete the file if it exists
      if (customCoverArtFile.exists() && customCoverArtFile.delete()) {
        Log.d("Shortcut", "Custom cover art file deleted successfully.");
      } else {
        Log.e("Shortcut", "Failed to delete custom cover art file or it doesn't exist.");
      }
    }

    // Reset the custom cover art path and cover art object
    this.customCoverArtPath = null;
    this.coverArt = null;

    // Remove it from extra data and save the state
    putExtra("customCoverArtPath", null);
    saveData();

    // Log the state after removal
    Log.d(
        "Shortcut",
        "Shortcut state saved after removing custom cover art. Current path: "
            + customCoverArtPath);
  }

  public boolean cloneToContainer(Container newContainer) {
    try {
      // Define the path for the new .desktop file in the new container
      File newShortcutFile = new File(newContainer.getDesktopDir(), this.file.getName());

      // Read the existing .desktop file
      ArrayList<String> lines = FileUtils.readLines(this.file);

      // Prepare the content for the new .desktop file with updated container_id
      StringBuilder updatedContent = new StringBuilder();
      boolean containerIdFound = false;

      for (String line : lines) {
        String trimmed = line.trim();
        // MSLink writes the '=' form, which the ':'-only match missed.
        boolean colon = trimmed.startsWith("container_id:");
        if (colon || trimmed.startsWith("container_id=")) {
          updatedContent.append("container_id").append(colon ? ':' : '=')
              .append(newContainer.id).append("\n");
          containerIdFound = true;
        } else {
          updatedContent.append(line).append("\n");
        }
      }

      // If the container_id wasn't found in the original file, add it
      if (!containerIdFound) {
        updatedContent.append("container_id:").append(newContainer.id).append("\n");
      }

      // Write the updated content to the new .desktop file
      FileUtils.writeString(newShortcutFile, updatedContent.toString());

      // Optionally copy the icon if it exists
      if (this.iconFile != null && this.iconFile.isFile()) {
        File newIconFile = new File(newContainer.getIconsDir(64), this.iconFile.getName());
        FileUtils.copy(this.iconFile, newIconFile);
      }

      return true;
    } catch (Exception e) {
      Log.e("Shortcut", "Failed to clone shortcut to new container", e);
      return false;
    }
  }

  public int getContainerId() {
    return container.id;
  }

  public JSONObject getExtraData() {
    return extraData;
  }

  public String getExecutable() {

    String exe = "";
    try {
      List<String> lines = Files.readAllLines(file.toPath());
      for (String line : lines) {
        if (line.startsWith("Exec")) {
          exe = line.substring(line.lastIndexOf("\\") + 1, line.length()).replaceAll("\\s+$", "");
          break;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return exe;
  }
}
