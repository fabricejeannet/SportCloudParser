package domain.parser;

import domain.competition.Athlete;
import domain.competition.Team;
import domain.competition.CompetitionInformations;
import org.dom4j.DocumentException;

import java.io.File;
import java.io.Reader;
import java.util.List;

/**
 * Created by fabricejeannet on 07/06/2016.
 */
public class BellePouleParser implements SportCloudParser{

    public static BellePouleParser create(){
        return null;
    }

    public void parse(File file) throws DocumentException {

    }

    public boolean isATeamCompetition() {
        return false;
    }

    public boolean isAnIndividualCompetition() {
        return false;
    }

    public List<Team> getTeams() {
        return null;
    }

    public List<Athlete> getAthletes() {
        return null;
    }

    public CompetitionInformations getCompetitionInformations() {
        return null;
    }

    public String getJson() {
        return null;
    }

    public String getEncoding() {
        return null;
    }
}
