package domain;

import Factories.TestFactories;
import domain.competition.Athlete;
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
    public void canRecognizeIndividualCompetitionXML() throws DocumentException, IOException {
        initializeParser();
        assertThat(parser.isATeamCompetition()).isFalse();
        assertThat(parser.isAnIndividualCompetition()).isTrue();
    }


    @Test
    public void canExtractAthletes() throws IOException, DocumentException {
        initializeParser();
        List<Athlete> athletes = parser.getAthletes();
        Athlete athlete0 = athletes.get(0);
        Athlete athlete1 = athletes.get(1);
        Athlete athlete2 = athletes.get(2);

        assertThat(athlete0.countryCode).isEqualTo("BRA");
        assertThat(athlete0.firstName).isEqualTo("Renzo");
        assertThat(athlete0.lastName).isEqualTo("Agresta");
        assertThat(athlete0.sex).isEqualTo(EnGardeParser.MALE_IN_JSON);
        assertThat(athlete0.rank).isEqualTo("3");

        assertThat(athlete1.countryCode).isEqualTo("FRA");
        assertThat(athlete1.firstName).isEqualTo("Vincent");
        assertThat(athlete1.lastName).isEqualTo("Anstett");
        assertThat(athlete1.sex).isEqualTo(EnGardeParser.MALE_IN_JSON);
        assertThat(athlete1.rank).isEqualTo("2");

        assertThat(athlete2.countryCode).isEqualTo("FRA");
        assertThat(athlete2.firstName).isEqualTo("Bolade");
        assertThat(athlete2.lastName).isEqualTo("Apithy");
        assertThat(athlete2.sex).isEqualTo(EnGardeParser.MALE_IN_JSON);
        assertThat(athlete2.rank).isEqualTo("1");
    }


    @Test
    public void canExtractMatches() throws IOException, DocumentException {
        initializeParser();
        List<Athlete> athletes = parser.getAthletes();
        List<Match> matches = parser.getMatches();


        Athlete athlete0 = athletes.get(0);
        Athlete athlete1 = athletes.get(1);
        Athlete athlete2 = athletes.get(2);


        Match match0 = matches.get(0);
        Match match1 = matches.get(1);
        Match match2 = matches.get(2);
        Match match3 = matches.get(3);
        Match match4 = matches.get(4);



        assertThat(matches.size()).isEqualTo(5);

        assertThat(match0.opponent1.localId).isEqualTo(athlete0.localId);
        assertThat(match0.opponent1.score).isEqualTo("2");
        assertThat(match0.opponent1.outcome).isEqualTo(EnGardeParser.LOSER_IN_JSON);

        assertThat(match0.opponent2.localId).isEqualTo(athlete1.localId);
        assertThat(match0.opponent2.score).isEqualTo("5");
        assertThat(match0.opponent2.outcome).isEqualTo(EnGardeParser.WINNER_IN_JSON);

        assertThat(match0.phase).isEqualTo(EnGardeParser.ROUND_OF_POOLS_IN_JSON + "." + 1);

        assertThat(match1.opponent1.localId).isEqualTo(athlete1.localId);
        assertThat(match1.opponent1.score).isEqualTo("1");
        assertThat(match1.opponent1.outcome).isEqualTo(EnGardeParser.LOSER_IN_JSON);

        assertThat(match1.opponent2.localId).isEqualTo(athlete2.localId);
        assertThat(match1.opponent2.score).isEqualTo("5");
        assertThat(match1.opponent2.outcome).isEqualTo(EnGardeParser.WINNER_IN_JSON);
        assertThat(match1.phase).isEqualTo(EnGardeParser.ROUND_OF_POOLS_IN_JSON + "." + 1);

        assertThat(match2.phase).isEqualTo(EnGardeParser.ROUND_OF_POOLS_IN_JSON + "." + 2);
        assertThat(match2.opponent1.localId).isEqualTo(athlete0.localId);
        assertThat(match2.opponent1.score).isEqualTo("5");
        assertThat(match2.opponent1.outcome).isEqualTo(EnGardeParser.WINNER_IN_JSON);

        assertThat(match2.opponent2.localId).isEqualTo(athlete1.localId);
        assertThat(match2.opponent2.score).isEqualTo("4");
        assertThat(match2.opponent2.outcome).isEqualTo(EnGardeParser.LOSER_IN_JSON);


        assertThat(match3.phase).isEqualTo(EnGardeParser.TABLEAU_IN_JSON + "." + 128);
        assertThat(match3.opponent1.localId).isEqualTo(athlete0.localId);
        assertThat(match3.opponent1.score).isEqualTo("15");
        assertThat(match3.opponent1.outcome).isEqualTo(EnGardeParser.WINNER_IN_JSON);

        assertThat(match3.opponent2.localId).isEqualTo(athlete1.localId);
        assertThat(match3.opponent2.score).isEqualTo("7");
        assertThat(match3.opponent2.outcome).isEqualTo(EnGardeParser.LOSER_IN_JSON);

        assertThat(match4.phase).isEqualTo(EnGardeParser.TABLEAU_IN_JSON + "." + 64);
        assertThat(match4.opponent1.localId).isEqualTo(athlete1.localId);
        assertThat(match4.opponent1.score).isEqualTo("13");
        assertThat(match4.opponent1.outcome).isEqualTo(EnGardeParser.WINNER_IN_JSON);

        assertThat(match4.opponent2.localId).isEqualTo(athlete2.localId);
        assertThat(match4.opponent2.score).isEqualTo("6");
        assertThat(match4.opponent2.outcome).isEqualTo(EnGardeParser.LOSER_IN_JSON);
    }

    public void initializeParser() throws DocumentException, IOException {
        parser = EnGardeParser.create();
        File fakeFile =  TestFactories.xml().threeFencersIndivualCompetition();
        parser.parse(fakeFile);
    }

    private SportCloudParser parser;

}
