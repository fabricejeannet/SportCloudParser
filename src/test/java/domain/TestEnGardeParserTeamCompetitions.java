package domain;

import Factories.TestFactories;
import domain.competition.CompetitionInformations;
import domain.competition.Team;
import domain.parser.EnGardeParser;
import domain.parser.SportCloudParser;
import org.dom4j.DocumentException;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by fabricejeannet on 06/06/2016.
 */
public class TestEnGardeParserTeamCompetitions {

    @Test
    public void canRecognizeTeamCompetitionXML() throws DocumentException, IOException {
        initializeParser();
        assertThat(parser.isATeamCompetition()).isTrue();
        assertThat(parser.isAnIndividualCompetition()).isFalse();
    }

    @Test
    public void canExtractCompetitionInformations() throws DocumentException, IOException {
        parser = EnGardeParser.create();
        File fakeFile =  TestFactories.xml().competitionInformations();
        parser.parse(fakeFile);

        CompetitionInformations competitionInformations = parser.getCompetitionInformations();

        assertThat(competitionInformations.date).isEqualTo("2016-05-15");
        assertThat(competitionInformations.weapon).isEqualTo("S");
        assertThat(competitionInformations.gender).isEqualTo("F");
        assertThat(competitionInformations.title).isEqualTo("Championnat de France");

        //<CompetitionParEquipes Championnat="FFE" ID="26182" Annee="2015/2016" Arme="S" Sexe="F" Federation="FFE" Organisateur="Thonon" Categorie="C" Date="15.05.2016" TitreCourt="SDC-eq" TitreLong="Championnat de France" URLorganisateur="http://francethononescrimeclub.weebly.com" >

        //<CompetitionIndividuelle Couleur="5" ID="5639db35" Annee="2015" Arme="E" Sexe="F" Organisateur="" Categorie="S" Date="05.11.2015" Appel="12:00" Scratch="12:00" Debut="12:00" TitreLong="CHAMPIONNAT IDF" URLorganisateur="" score_stuffing="0" Lieu="">
    }

    @Test
    public void canExtractTeams() throws DocumentException, IOException {
        initializeParser();
        List<Team> teams =  parser.getTeams();
        Team team = teams.get(0);

        assertThat(team.name).isEqualTo("BORDEAUX CAM 1");
        assertThat(team.clubName).isEqualTo("BORDEAUX CAM");
        assertThat(teams.size()).isEqualTo(17);

    }

    @Test
    public void canExtractTeamMembers() throws DocumentException, IOException {
        parser = EnGardeParser.create();

        File fakeFile =  TestFactories.xml().oneTeamCompetition();
        parser.parse(fakeFile);

        List<Team> teams =  parser.getTeams();
        Team team = teams.get(0);

        assertThat(team.athletes.get(0).localId).isEqualTo("29");
        assertThat(team.athletes.get(0).lastName).isEqualTo("Carre");
        assertThat(team.athletes.get(0).firstName).isEqualTo("Adele");
        assertThat(team.athletes.get(0).sex).isEqualTo(EnGardeParser.FEMALE_IN_JSON);
        assertThat(team.athletes.get(0).countryCode).isEqualTo("FRA");

        assertThat(teams.size()).isEqualTo(1);
    }


    @Test
    public void canFormatNames() throws DocumentException, IOException {
        parser = EnGardeParser.create();
        File fakeFile =  TestFactories.xml().oneTeamCompetition();
        parser.parse(fakeFile);
        List<Team> teams =  parser.getTeams();
        Team team = teams.get(0);

        //Name = TOTO du bord de-la-plage
        assertThat(team.athletes.get(1).lastName).isEqualTo("Toto Du Bord De-La-Plage");
        assertThat(team.athletes.get(1).firstName).isEqualTo("Elsa");

    }

    public void initializeParser() throws DocumentException, IOException {
        parser = EnGardeParser.create();
        File fakeFile =  TestFactories.xml().completeTeamCompetition();
        parser.parse(fakeFile);
    }

    private SportCloudParser parser;

}
