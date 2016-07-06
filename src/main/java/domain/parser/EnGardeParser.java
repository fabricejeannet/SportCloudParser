package domain.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.competition.Fencer;
import domain.competition.Team;
import domain.competition.Competition;
import domain.competition.CompetitionInformations;
import domain.competition.IndividualCompetition;
import domain.competition.TeamCompetition;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.Reader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fabricejeannet on 06/06/2016.
 */
public class EnGardeParser implements SportCloudParser {

    public static EnGardeParser create() {
        EnGardeParser parser = new EnGardeParser();
        return parser;
    }

    private EnGardeParser() {
    }

    public void parse(Reader xmlStream) throws DocumentException {
        document = saxReader.read(xmlStream);
        root = document.getRootElement();
    }

    public boolean isATeamCompetition() {
        return root.getName().equals(TEAM_COMPETITION);
    }

    public boolean isAnIndividualCompetition() {
        return root.getName().equals(INDIVIDUAL_COMPETITION);
    }

    public CompetitionInformations getCompetitionInformations(){
        CompetitionInformations competitionInformations = new CompetitionInformations();
        competitionInformations.date = getJSONDate(root.attributeValue(DATE));
        competitionInformations.weapon = root.attributeValue(WEAPON);
        competitionInformations.gender = root.attributeValue(GENDER);
        competitionInformations.title = root.attributeValue(TITLE);
        return competitionInformations;
    }

    private String getJSONDate(String inputDate) {
        String jsonDate;

        if(inputDate != null) {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate date = LocalDate.parse(inputDate, inputFormatter);
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            jsonDate = date.format(outputFormatter);
        } else {
            jsonDate=UNKNOWN;
        }

        return jsonDate;
    }

    public List<Team> getTeams() {
        Element teamsNode  = root.element(TEAMS);
        List<Element> teamNodes = teamsNode.elements();
        Iterator<Element> it = teamNodes.iterator();

        ArrayList<Team> teams = new ArrayList<Team>();
        while (it.hasNext()){
            Element teamNode = it.next();
            teams.add(getTeam(teamNode));
        }
        return teams;
    }

    private Team getTeam(Element teamNode){
        Team team = new Team();
        team.name = teamNode.attributeValue(NAME);
        team.clubName = teamNode.attributeValue(CLUB);
        List<Fencer> fencers = getFencers(teamNode);
        team.athletes.addAll(fencers);
        return team;
    }

    private List<Fencer> getFencers(Element teamNode){
        Iterator<Element> fencerIterator = teamNode.elementIterator(FENCER);
        ArrayList<Fencer> fencers = new ArrayList<Fencer>();
        while (fencerIterator.hasNext()) {
            Element thisFencer = fencerIterator.next();
            Fencer fencer = new Fencer();

            fencer.localId = thisFencer.attributeValue(ID);

            fencer.firstName = formatName(thisFencer.attributeValue(FIRST_NAME));
            fencer.lastName = formatName(thisFencer.attributeValue(LAST_NAME));

            String sex = thisFencer.attributeValue(GENDER);

            if(MEN_IN_XML.equals(sex)) {
                fencer.sex = MALE_IN_JSON;
            } else if (WOMEN_IN_XML.equals(sex)) {
                fencer.sex = FEMALE_IN_JSON;
            } else {
                fencer.sex = OTHER_IN_JSON;
            }

            fencer.countryCode = thisFencer.attributeValue(COUNTRY_CODE);

            fencers.add(fencer);
        }
        return fencers;
    }


    public String formatName(String name){
        if(name != null) {
            name = name.toLowerCase().trim();
            char[] chars = name.toCharArray();

            chars[0] = Character.toUpperCase(chars[0]);

            Pattern pattern = Pattern.compile("([\\s]|\\-)");
            Matcher matcher = pattern.matcher(name);

            while (matcher.find()) {
                int indexOfSpaceOrHyphen = matcher.start() + 1;
                Character character = name.charAt(indexOfSpaceOrHyphen);
                chars[indexOfSpaceOrHyphen] = Character.toUpperCase(character.charValue());
            }

            return new String(chars);
        }

        return name;
    }


    public String getJson() throws JsonProcessingException {

        Competition competition;

        if (isATeamCompetition()) {
            CompetitionInformations competitionInformations  = getCompetitionInformations();

            competition = new TeamCompetition();
            competition.date = competitionInformations.date;
            competition.name = competitionInformations.title;
            competition.sportType = getSportTypeFrom(competitionInformations);

            ((TeamCompetition)competition).teams = getTeams();
        } else {
            competition = new IndividualCompetition();
        }

        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(competition);

        return json;
    }


    public String getSportTypeFrom(CompetitionInformations competitionInformations){
        String sportType = FENCING.concat(".");
        if(competitionInformations.weapon.equals(EPEE_IN_XML)) sportType = sportType.concat(EPEE_IN_JSON);
        if(competitionInformations.weapon.equals(FOIL_IN_XML)) sportType =sportType.concat(FOIL_IN_JSON);
        if(competitionInformations.weapon.equals(SABRE_IN_XML)) sportType =sportType.concat(SABRE_IN_JSON);
        sportType = sportType.concat(TEAM_IN_JSON);
        if(competitionInformations.gender.equals(MEN_IN_XML)) sportType =sportType.concat(MEN_IN_JSON);
        if(competitionInformations.gender.equals(WOMEN_IN_XML)) sportType =sportType.concat(WOMEN_IN_JSON);
        return sportType;
    }


    public String getEncoding() {
        return document.getXMLEncoding();
    }

    public static final String ID = "ID";

    public static final String FENCING = "fencing";
    public static final String MEN_IN_XML = "M";
    public static final String WOMEN_IN_XML = "F";

    public static final String MEN_IN_JSON = "Men";
    public static final String WOMEN_IN_JSON = "Women";
    public static final String MALE_IN_JSON = "male";
    public static final String FEMALE_IN_JSON = "female";
    public static final String OTHER_IN_JSON = "other";

    public static final String TEAM_IN_JSON = "Team";
    public static final String INDIVIDUAL = "Individual";
    public static final String EPEE_IN_JSON = "epee";
    public static final String FOIL_IN_JSON = "foil";
    public static final String SABRE_IN_JSON = "sabre";
    public static final String EPEE_IN_XML = "E";
    public static final String FOIL_IN_XML = "F";
    public static final String SABRE_IN_XML = "S";
    public static final String TEAMS = "Equipes";
    public static final String FENCER = "Tireur";
    public static final String NAME = "Nom";
    public static final String CLUB = "Club";
    public static final String TEAM_COMPETITION = "CompetitionParEquipes";
    public static final String INDIVIDUAL_COMPETITION = "CompetitionIndividuelle";
    public static final String DATE = "Date";
    public static final String WEAPON = "Arme";
    public static final String GENDER = "Sexe";
    public static final String TITLE = "TitreLong";
    public static final String FIRST_NAME = "Prenom";
    public static final String LAST_NAME = "Nom";

    public static final String COUNTRY_CODE = "Nation";

    public static final String UNKNOWN = "Unknown";

    private SAXReader saxReader = new SAXReader();
    private Element root;
    private Document document;

    private Logger logger = Logger.getLogger(this.getClass().getName());
}
