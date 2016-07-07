package dom4j;

import Factories.TestFactories;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by fabricejeannet on 06/06/2016.
 */
public class TestDom4j {


    @Test
    public void canCountTeams() throws DocumentException, IOException {
        SAXReader reader = new SAXReader();

        Document document = reader.read(new FileReader(TestFactories.xml().completeTeamCompetition()));

        Element root = document.getRootElement();

        Element listOfTeams = root.element("Equipes");

        assertThat(listOfTeams.elements("Equipe").size()).isEqualTo(17);
    }


}
