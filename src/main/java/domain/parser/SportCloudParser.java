package domain.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import domain.competition.Athlete;
import domain.competition.Team;
import domain.competition.CompetitionInformations;
import org.dom4j.DocumentException;

import java.io.File;
import java.util.List;

/**
 * Created by fabricejeannet on 06/06/2016.
 */
public interface SportCloudParser {
    void parse(File file) throws DocumentException;
    boolean isATeamCompetition();
    boolean isAnIndividualCompetition();
    List<Team> getTeams();
    List<Athlete> getAthletes();
    CompetitionInformations getCompetitionInformations();

    String getJson() throws JsonProcessingException;

    String getEncoding();
}
