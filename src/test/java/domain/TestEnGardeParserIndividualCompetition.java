package domain;

import Factories.TestFactories;
import domain.competition.Athlete;
import domain.competition.CompetitionInformations;
import domain.parser.EnGardeParser;
import domain.parser.SportCloudParser;
import org.dom4j.DocumentException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by fabricejeannet on 07/07/2016.
 */
public class TestEnGardeParserIndividualCompetition {

    @Test
    public void canRecognizeTeamCompetitionXML() throws DocumentException, IOException {
        initializeParser();
        assertThat(parser.isATeamCompetition()).isFalse();
        assertThat(parser.isAnIndividualCompetition()).isTrue();
    }


    @Test
    public void canExtractAthletes() throws IOException, DocumentException {
        initializeParser();
        List<Athlete> athletes = parser.getAthletes();
        Athlete athlete = athletes.get(0);
        assertThat(athlete.countryCode).isEqualTo("BRA");
        assertThat(athlete.firstName).isEqualTo("Renzo");
        assertThat(athlete.lastName).isEqualTo("Agresta");
        assertThat(athlete.sex).isEqualTo(EnGardeParser.MALE_IN_JSON);
        //"    <Tireur ID=\"76\" Nom=\"AGRESTA\" Prenom=\"Renzo\" Date Naissance=\"00.00.1985\" Sexe=\"M\" Nation=\"BRA\" Club=\"BRAZIL\" Classement=\"12\" Statut=\"N\" />\n" +

    }

    public void initializeParser() throws DocumentException, IOException {
        parser = EnGardeParser.create();
        File fakeFile =  TestFactories.xml().threeFencersIndivualCompetition();
        parser.parse(fakeFile);
    }

    private SportCloudParser parser;

}
