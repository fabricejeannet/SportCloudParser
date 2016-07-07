package domain.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.competition.Athlete;
import domain.competition.Team;
import domain.competition.Competition;
import domain.competition.CompetitionInformations;
import domain.competition.IndividualCompetition;
import domain.competition.TeamCompetition;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
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

    public void parse(File file) throws DocumentException {
        document = saxReader.read(file);
        root = document.getRootElement();
    }

    public boolean isATeamCompetition() {
        return root.getName().equals(TEAM_COMPETITION_IN_XML);
    }

    public boolean isAnIndividualCompetition() {
        return root.getName().equals(INDIVIDUAL_COMPETITION_IN_XML);
    }

    public CompetitionInformations getCompetitionInformations(){
        CompetitionInformations competitionInformations = new CompetitionInformations();
        competitionInformations.date = getJSONDate(root.attributeValue(DATE_IN_XML));
        competitionInformations.weapon = root.attributeValue(WEAPON_IN_XML);
        competitionInformations.gender = root.attributeValue(GENDER_IN_XML);
        competitionInformations.title = root.attributeValue(TITLE_IN_XML);
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
            jsonDate= UNKNOWN_IN_JSON;
        }

        return jsonDate;
    }

    public List<Team> getTeams() {
        ArrayList<Team> teams = new ArrayList<Team>();
        if(isATeamCompetition()) {
            Element teamsNode  = root.element(TEAMS_IN_XML);
            List<Element> teamNodes = teamsNode.elements();
            Iterator<Element> it = teamNodes.iterator();

            while (it.hasNext()){
                Element teamNode = it.next();
                teams.add(getTeam(teamNode));
            }
        }
        return teams;
    }

    private Team getTeam(Element teamNode){
        Team team = new Team();
        team.name = teamNode.attributeValue(NAME_IN_XML);
        team.clubName = teamNode.attributeValue(CLUB_IN_XML);
        List<Athlete> athletes = getTeamAtheletes(teamNode);
        team.athletes.addAll(athletes);
        return team;
    }

    public List<Athlete> getAthletes(){
        List<Athlete> athletes = new ArrayList<Athlete>();
        if(isAnIndividualCompetition()) {
            Element athletesNode  = root.element(ATHLETES_IN_XML);
            List<Element> athleteNodes = athletesNode.elements();
            Iterator<Element> it = athleteNodes.iterator();
            while (it.hasNext()){
                Element athleteNode = it.next();
                athletes.add(getAthelete(athleteNode));
            }

        }
        return athletes;
    }

    private Athlete getAthelete(Element athleteNode) {
        Athlete athlete = new Athlete();
        athlete.localId = athleteNode.attributeValue(ID);
        athlete.firstName = formatName(athleteNode.attributeValue(FIRST_NAME_IN_XML));
        athlete.lastName = formatName(athleteNode.attributeValue(LAST_NAME_IN_XML));
        String sex = athleteNode.attributeValue(GENDER_IN_XML);
        if(MEN_IN_XML.equals(sex)) {
            athlete.sex = MALE_IN_JSON;
        } else if (WOMEN_IN_XML.equals(sex)) {
            athlete.sex = FEMALE_IN_JSON;
        } else {
            athlete.sex = OTHER_IN_JSON;
        }
        athlete.countryCode = athleteNode.attributeValue(COUNTRY_CODE_IN_XML);
        return athlete;
    }


    private List<Athlete> getTeamAtheletes(Element teamNode){
        Iterator<Element> fencerIterator = teamNode.elementIterator(ATHLETE_IN_XML);
        ArrayList<Athlete> athletes = new ArrayList<Athlete>();
        while (fencerIterator.hasNext()) {
            Element thisFencer = fencerIterator.next();
            athletes.add(getAthelete(thisFencer));
        }
        return athletes;
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
            competition = new TeamCompetition();
            ((TeamCompetition)competition).teams = getTeams();
        } else {
            competition = new IndividualCompetition();
            ((IndividualCompetition)competition).athletes = getAthletes();
        }

        CompetitionInformations competitionInformations  = getCompetitionInformations();
        competition.date = competitionInformations.date;
        competition.name = competitionInformations.title;
        competition.sportType = getSportTypeFrom(competitionInformations);


        ObjectMapper mapper = new ObjectMapper();

        String json = mapper.writeValueAsString(competition);

        return json;
    }




    public String getSportTypeFrom(CompetitionInformations competitionInformations){
        String sportType = FENCING.concat(".");
        if(competitionInformations.weapon.equals(EPEE_IN_XML)) sportType = sportType.concat(EPEE_IN_JSON);
        if(competitionInformations.weapon.equals(FOIL_IN_XML)) sportType =sportType.concat(FOIL_IN_JSON);
        if(competitionInformations.weapon.equals(SABRE_IN_XML)) sportType =sportType.concat(SABRE_IN_JSON);
        if(isATeamCompetition()) {
            sportType = sportType.concat(TEAM_IN_JSON);
        } else {
            sportType = sportType.concat(INDIVIDUAL_IN_JSON);
        }
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
    public static final String EPEE_IN_XML = "E";
    public static final String FOIL_IN_XML = "F";
    public static final String SABRE_IN_XML = "S";

    public static final String MEN_IN_JSON = "Men";
    public static final String WOMEN_IN_JSON = "Women";
    public static final String MALE_IN_JSON = "male";
    public static final String FEMALE_IN_JSON = "female";
    public static final String OTHER_IN_JSON = "other";
    public static final String TEAM_IN_JSON = "Team";
    public static final String INDIVIDUAL_IN_JSON = "Individual";
    public static final String EPEE_IN_JSON = "epee";
    public static final String FOIL_IN_JSON = "foil";
    public static final String SABRE_IN_JSON = "sabre";

    public static final String TEAMS_IN_XML = "Equipes";
    public static final String ATHLETE_IN_XML = "Tireur";
    public static final String NAME_IN_XML = "Nom";
    public static final String CLUB_IN_XML = "Club";
    public static final String TEAM_COMPETITION_IN_XML = "CompetitionParEquipes";
    public static final String INDIVIDUAL_COMPETITION_IN_XML = "CompetitionIndividuelle";
    public static final String DATE_IN_XML = "Date";
    public static final String WEAPON_IN_XML = "Arme";
    public static final String GENDER_IN_XML = "Sexe";
    public static final String TITLE_IN_XML = "TitreLong";
    public static final String FIRST_NAME_IN_XML = "Prenom";
    public static final String LAST_NAME_IN_XML = "Nom";

    public static final String COUNTRY_CODE_IN_XML = "Nation";

    public static final String UNKNOWN_IN_JSON = "Unknown";
    private static final String ATHLETES_IN_XML = "Tireurs";

    private SAXReader saxReader = new SAXReader();
    private Element root;
    private Document document;

    private Logger logger = Logger.getLogger(this.getClass().getName());
}
