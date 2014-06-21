package de.hsk1830.klubturnier.xlskonverter;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Maps;

/**
 * Created by kleinr on 19/05/2014.
 */
public final class Spieler implements Comparable<Spieler> {
	public final String vorname;
	public final String nachname;
	private final Map<String, String> attributeMap;

	public Spieler(final String vorname, final String nachname) {
		this.vorname = Objects.requireNonNull(vorname);
		this.nachname = Objects.requireNonNull(nachname);
		this.attributeMap = Maps.newHashMap();
	}

	public String putAttributeValue(final String attributeKey, final String attributeValue) {
		return attributeMap.put(Objects.requireNonNull(attributeKey), Objects.requireNonNull(attributeValue));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Spieler spieler = (Spieler) o;

		if (!nachname.equals(spieler.nachname)) return false;
		if (!vorname.equals(spieler.vorname)) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = vorname.hashCode();
		result = 31 * result + nachname.hashCode();
		return result;
	}

	@Override
	public int compareTo(final Spieler other) {
		final int nachnameComparison = this.nachname.compareTo(other.nachname);
		return nachnameComparison == 0? this.vorname.compareTo(other.vorname): nachnameComparison;
	}

	@Override
	public String toString() {
		return "Spieler{" +
				"vorname='" + vorname + '\'' +
				", nachname='" + nachname + '\'' +
				", attributeMap=" + attributeMap +
				'}';
	}

	public String getAttributeValue(final String key) {
		return attributeMap.get(key);
	}
}
