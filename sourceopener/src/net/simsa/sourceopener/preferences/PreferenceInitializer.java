package net.simsa.sourceopener.preferences;

import java.util.logging.Logger;

import net.simsa.sourceopener.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	Logger log = Logger.getLogger("PreferenceInitializer");

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_PORT, 9123);
		store.setDefault(PreferenceConstants.P_PASSWORD, "");
		store.setDefault(PreferenceConstants.P_USEPASSWORD, false);
		store.setDefault(PreferenceConstants.P_KEEP_COUNT, 6);
		store.setDefault(PreferenceConstants.P_START_ON_STARTUP, false);
	}

}
