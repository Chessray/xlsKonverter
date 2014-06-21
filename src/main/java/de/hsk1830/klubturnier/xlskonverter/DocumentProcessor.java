package de.hsk1830.klubturnier.xlskonverter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

/**
 * Created by kleinr on 02/06/2014.
 */
public class DocumentProcessor {
    final Document document;
	private Element tableHeaderRow;

	DocumentProcessor(final File htmlTemplate) throws IOException {
		document = Jsoup.parse(htmlTemplate, null);
	}

	private void removeDataRows() {
		Element otherTableRow = tableHeaderRow.nextElementSibling();
		while (otherTableRow != null) {
			final Element toRemove = otherTableRow;
			otherTableRow = otherTableRow.nextElementSibling();
			toRemove.remove();
		}
	}

	Element getTableHeaderRow() {
		if (tableHeaderRow == null) {
			tableHeaderRow = getTableElement().getElementsByTag("tr").first();
			removeDataRows();
		}
		return tableHeaderRow;
	}

	Element getTableElement() {
		return document.getElementsByTag("table").first();
	}

	void addSpielerDataToTableElement(List<String> tableColumnHeaders, List<Spieler> spielerList) {
		final Element tableElement = getTableElement();
		spielerList.forEach(spieler -> {
			final Element spielerRow = new Element(Tag.valueOf("tr"), StringUtils.EMPTY).attr("style", "height: 23px;");
			tableColumnHeaders.forEach(tableColumnHeader -> {
				final Element attributeElement = new Element(Tag.valueOf("td"), StringUtils.EMPTY).classNames(Collections.singleton("style16_2"));
				final String attributeValue = spieler.getAttributeValue(tableColumnHeader);
				attributeElement.appendText(StringUtils.isBlank(attributeValue) ? "\u00a0" : attributeValue);
				spielerRow.appendChild(attributeElement);
			});
			tableElement.appendChild(spielerRow);
		});
	}

	List<String> getTableColumnHeaders() {
		return getTableHeaderRow().getElementsByTag("td").stream().map(Element::text).collect(Collectors.toList());
	}

	private String toOutputString() {
		// per Regex Passwort auf dieselbe Zeile wie vorherige Spalte
		return document.toString().replaceAll("(<tr style=\"height: 23px;\">\\s*<td.*>)\\s*(<td.*>)", "$1$2");
	}

	void writeConvertedString(final File outputFile) throws IOException {
		Files.write(Objects.requireNonNull(outputFile).toPath(), Collections.singleton(toOutputString()));
	}
}
