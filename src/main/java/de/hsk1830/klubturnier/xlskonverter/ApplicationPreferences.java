package de.hsk1830.klubturnier.xlskonverter;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by kleinr on 02/06/2014.
 */
final class ApplicationPreferences {
	private ApplicationPreferences() {}

	private static Preferences getPreferencesNode() {
		return getPreferencesParentNode().node("xlskonverter");
	}

	private static Preferences getPreferencesParentNode() {
		return Preferences.userRoot().node("de").node("hsk1830").node("klubturnier");
	}

	private static boolean preferencesNodeExists() throws BackingStoreException {
		return getPreferencesParentNode().nodeExists("xlskonverter");
	}

	static File getInitialDirectory(final String preferencesKeyForDirectory) {
		File initialDirectory = null;
		try {
			if (preferencesNodeExists()) {
				final String initialDirectoryPath = getPreferencesNode().get(preferencesKeyForDirectory, null);
				if (initialDirectoryPath != null) {
					final File storedInitialDirectory = new File(initialDirectoryPath);
					if (storedInitialDirectory.exists()) {
						initialDirectory = storedInitialDirectory;
					}
				}
			}
		} catch (BackingStoreException e) {
			// TODO
			e.printStackTrace();
		}
		return initialDirectory;
	}

	static void putParentIntoPreferencesIffFileNotNull(final String preferencesKeyForDirectory, final File file) {
		if (file != null) {
			getPreferencesNode().put(preferencesKeyForDirectory, file.getParent());
		}
	}
}
