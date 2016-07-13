package domain.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Match;
import domain.Opponent;
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
        athlete.rank = getAthleteRank(athlete.localId);
        return athlete;
    }


    private String getAthleteRank(String localId){
        Element phasesNode = root.element(PHASES_IN_XML);
        String athleteRank = null;

        if(phasesNode != null) {
            Element phaseDeTableauxNode = phasesNode.element(PHASES_DE_TABLEAUX_IN_XML);
            Iterator<Element> it = phaseDeTableauxNode.elementIterator(ATHLETE_IN_XML);
            boolean found = false;

            while (it.hasNext() && !found){
                Element tireurNode = it.next();
                found = tireurNode.attributeValue(REF).equals(localId);
                if(found) athleteRank = tireurNode.attributeValue(FINAL_RANK_IN_XML);
            }
        }

        return athleteRank;
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
            ((IndividualCompetition)competition).matches = getMatches();
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

    public List<Match> getMatches() {
        List<Match> matches = new ArrayList<Match>();
        matches.addAll(getPouleMatches());
        matches.addAll(getTableMatches());
        return matches;
    }


    private List<Match> getPouleMatches() {
        Element phasesNode = root.element(PHASES_IN_XML);
        List<Match> allMatches = new ArrayList<Match>();

        List<Element> toursDePoulesNode = phasesNode.elements(TOUR_DE_POULES_IN_XML);

        currentRoundsOfPool = 1;

        for (Element tourDePoulesNode : toursDePoulesNode) {
            Iterator<Element> poulesIterator = tourDePoulesNode.elementIterator(POULE_IN_XML);
            while (poulesIterator.hasNext()) {
                Element pouleNode = poulesIterator.next();
                allMatches.addAll(extractPouleMatches(pouleNode));
            }
            currentRoundsOfPool++;
        }

        return allMatches;
    }



    private List<Match> extractPouleMatches(Element node) {
        Iterator<Element> matchesIterator = node.elementIterator(MATCH_IN_XML);
        List<Match> matches = new ArrayList<Match>();
        while(matchesIterator.hasNext()) {
            Element matchNode = matchesIterator.next();
            matches.add(extractPouleMatch(matchNode));
        }
        return matches;
    }

    private Match extractPouleMatch(Element matchNode) {
        Match match = new Match();
        List<Element> tireurs = matchNode.elements(ATHLETE_IN_XML);
        match.opponent1 = extractOpponent(tireurs.get(0));
        match.opponent2 = extractOpponent(tireurs.get(1));
        match.phase = ROUND_OF_POOLS_IN_JSON + "." + currentRoundsOfPool;
        return match;
    }

    private Opponent extractOpponent(Element element) {
        Opponent opponent = new Opponent();
        opponent.localId = element.attributeValue(REF);
        opponent.score = element.attributeValue(SCORE_IN_XML);
        if(element.attributeValue(STATUT_IN_XML).equals(WINNER_IN_XML)) {
            opponent.status = WINNER_IN_JSON;
        } else {
            opponent.status = LOSER_IN_JSON;
        }
        return opponent;
    }


    private List<Match> getTableMatches() {
        Element phasesNode = root.element(PHASES_IN_XML);
        Element phaseDeTableauxNode = phasesNode.element(PHASES_DE_TABLEAUX_IN_XML);
        Element suiteDeTableauxNode = phaseDeTableauxNode.element(SUITE_DE_TABLEAUX_IN_XML);

        List<Element> tableauxNode = suiteDeTableauxNode.elements(TABLEAU_IN_XML);

        List<Match> allMatches = new ArrayList<Match>();


        for (Element tableauNode : tableauxNode) {
            Iterator<Element> matchNodes = tableauNode.elementIterator(MATCH_IN_XML);
            String taille = tableauNode.attributeValue(TAILLE_IN_XML);

            while (matchNodes.hasNext()) {
                Element matchNode = matchNodes.next();
                Match match = extractTableauMatch(matchNode);
                match.phase = TABLEAU_IN_JSON;
                if (taille != null) match.phase = match.phase + "." + taille;

                allMatches.add(match);
            }
        }

        return allMatches;
    }

    private Match extractTableauMatch(Element matchNode) {
        Match match = new Match();
        List<Element> tireurs = matchNode.elements(ATHLETE_IN_XML);
        match.opponent1 = extractOpponent(tireurs.get(0));
        match.opponent2 = extractOpponent(tireurs.get(1));
        match.phase = ROUND_OF_POOLS_IN_JSON + "." + currentRoundsOfPool;
        return match;
    }

    public static final String ID = "ID";
    public static final String REF = "REF";


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
    public static final String WINNER_IN_JSON = "winner";
    public static final String LOSER_IN_JSON = "loser";
    public static final String ROUND_OF_POOLS_IN_JSON ="roundsOfPool";
    public static final String TABLEAU_IN_JSON ="directEliminationTable";


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
    private static final String PHASES_IN_XML = "Phases";
    private static final String PHASES_DE_TABLEAUX_IN_XML = "PhaseDeTableaux";
    private static final String SUITE_DE_TABLEAUX_IN_XML = "SuiteDeTableaux";

    private static final String FINAL_RANK_IN_XML = "RangFinal";
    private static final String TOUR_DE_POULES_IN_XML = "TourDePoules";
    private static final String POULE_IN_XML = "Poule";
    private static final String MATCH_IN_XML = "Match";
    private static final String SCORE_IN_XML = "Score";
    private static final String STATUT_IN_XML = "Statut";
    private static final String WINNER_IN_XML = "V";
    private static final String LOSER_IN_XML = "D";
    private static final String TABLEAU_IN_XML = "Tableau";
    private static final String TAILLE_IN_XML = "Taille";

    private SAXReader saxReader = new SAXReader();
    private Element root;
    private Document document;

    private int currentRoundsOfPool;

    private Logger logger = Logger.getLogger(this.getClass().getName());
}
