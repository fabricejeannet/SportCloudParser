package domain;

import Factories.TestFactories;
import domain.competition.CompetitionInformations;
import domain.competition.Team;
import domain.parser.EnGardeParser;
import domain.parser.SportCloudParser;
import org.dom4j.DocumentException;
import org.junit.Test;

import java.io.Reader;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by fabricejeannet on 06/06/2016.
 */
public class TestEnGardeParser {

    @Test
    public void canRecognizeTeamCompetitionXML() throws DocumentException {
        initializeParser();
        assertThat(parser.isATeamCompetition()).isTrue();
        assertThat(parser.isAnIndividualCompetition()).isFalse();
    }

    @Test
    public void canExtractCompetitionInformations() throws DocumentException {
        parser = EnGardeParser.create();
        Reader fakeXMLReader = TestFactories.xml().competitionInformations();
        parser.parse(fakeXMLReader);

        CompetitionInformations competitionInformations = parser.getCompetitionInformations();

        assertThat(competitionInformations.championship).isEqualTo("FFE");
        assertThat(competitionInformations.date).isEqualTo("2016-05-15");
        assertThat(competitionInformations.weapon).isEqualTo("S");
        assertThat(competitionInformations.gender).isEqualTo("F");
        assertThat(competitionInformations.federeation).isEqualTo("FFE");
        assertThat(competitionInformations.weapon).isEqualTo("S");
        assertThat(competitionInformations.organizer).isEqualTo("Thonon");
        assertThat(competitionInformations.category).isEqualTo("C");
        assertThat(competitionInformations.title).isEqualTo("Championnat de France");
        assertThat(competitionInformations.organizerUrl).isEqualTo("http://francethononescrimeclub.weebly.com");

        //<CompetitionParEquipes Championnat="FFE" ID="26182" Annee="2015/2016" Arme="S" Sexe="F" Federation="FFE" Organisateur="Thonon" Categorie="C" Date="15.05.2016" TitreCourt="SDC-eq" TitreLong="Championnat de France" URLorganisateur="http://francethononescrimeclub.weebly.com" >

        //<CompetitionIndividuelle Couleur="5" ID="5639db35" Annee="2015" Arme="E" Sexe="F" Organisateur="" Categorie="S" Date="05.11.2015" Appel="12:00" Scratch="12:00" Debut="12:00" TitreLong="CHAMPIONNAT IDF" URLorganisateur="" score_stuffing="0" Lieu="">
    }

    @Test
    public void canExtractTeams() throws DocumentException {
        initializeParser();
        List<Team> teams =  parser.getTeams();
        assertThat(teams.size()).isEqualTo(17);
    }

    @Test
    public void canExtractTeamMembers() throws DocumentException {
        parser = EnGardeParser.create();
        Reader fakeXMLReader = TestFactories.xml().oneTeamCompetition();
        parser.parse(fakeXMLReader);
        List<Team> teams =  parser.getTeams();
        Team team = teams.get(0);

        assertThat(team.athletes.get(0).localId).isEqualTo("29");
        assertThat(team.athletes.get(0).lastName).isEqualTo("CARRE");
        assertThat(team.athletes.get(0).firstName).isEqualTo("Adele");
        assertThat(team.athletes.get(0).sex).isEqualTo(EnGardeParser.FEMALE_IN_JSON);
        assertThat(team.athletes.get(0).countryCode).isEqualTo("FRA");

        assertThat(teams.size()).isEqualTo(1);
    }


    public void initializeParser() throws DocumentException {
        parser = EnGardeParser.create();
        Reader fakeXMLReader = TestFactories.xml().completeTeamCompetition();
        parser.parse(fakeXMLReader);
    }

    private SportCloudParser parser;

}
