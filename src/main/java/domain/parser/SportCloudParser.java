package domain.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import domain.competition.Team;
import domain.competition.CompetitionInformations;
import org.dom4j.DocumentException;

import java.io.Reader;
import java.util.List;

/**
 * Created by fabricejeannet on 06/06/2016.
 */
public interface SportCloudParser {
    void parse(Reader reader) throws DocumentException;
    boolean isATeamCompetition();
    boolean isAnIndividualCompetition();
    List<Team> getTeams();
    CompetitionInformations getCompetitionInformations();

    String getJson() throws JsonProcessingException;

    String getEncoding();
}
