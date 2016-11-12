package com.parse.objects;

public final class DemographicValues {
	// These titles are used for fieldnames in the parse objects for gender, age
	// continent, race and education.
	public static String GENDER_TITLE = "gender";
	public static String AGE_TITLE = "age";
	public static String CONTINENT_TITLE = "continent";
	public static String RACE_TITLE = "race";
	public static String EDUCATION_TITLE = "education";
	public static String RELIGION_TITLE = "religion";
	public static String DOB_TITLE = "date_of_birth";
	
	// All available values for each of the 5 dimensions.
	public static String ages[];
	public static String genders[];
	public static String continents[];
	public static String educations[];
	public static String races[];
	public static String religion[];

	// The null element is used for an unselected dimension. It is set to the 0
	// index when set as a data source to the spinner objects in the UI.
	public static String null_element = "Unspecified";

	public static String categories[];

	public static String type[];

	// the last options that are mandatory for every question.
	public static String last_options[];

	static {

		religion = new String[13];
		religion[0] = null_element;
		religion[1] = "Agnostic";
		religion[2] = "Atheist";
		religion[3] = "Buddhist";
		religion[4] = "Catholic";
		religion[5] = "Hindu";
		religion[6] = "Jehovah's Witness";
		religion[7] = "Jewish";
		religion[8] = "Mormon";
		religion[9] = "Islamic";
		religion[10] = "Orthodox";
		religion[11] = "Protestant";
		religion[12] = "Other";
		
		ages = new String[7];
		ages[0] = null_element;
		ages[1] = "under 18";
		ages[2] = "18-30";
		ages[3] = "31-40";
		ages[4] = "41-50";
		ages[5] = "51-70";
		ages[6] = "over 70";

		genders = new String[3];
		genders[0] = null_element;
		genders[1] = "Male";
		genders[2] = "Female";

		continents = new String[8];
		continents[0] = null_element;
		continents[1] = "Antartica";
		continents[2] = "Africa";
		continents[3] = "Asia";
		continents[4] = "Australia";
		continents[5] = "Europe";
		continents[6] = "N. America";
		continents[7] = "S. America";


		educations = new String[8];
		educations[0] = null_element;
		educations[1] = "High school diploma";
		educations[2] = "Associate's degree";		
		educations[3] = "Bachelor's degree";
		educations[4] = "Master's degree";
		educations[5] = "Professional degree";
		educations[6] = "Doctoral degree";
		educations[7] = "Other";

		races = new String[8];
		races[0] = null_element;
		races[1] = "Asian or Pacific Islander";
		races[2] = "American Indian or Alaskan Native";
		races[3] = "Black (not Hispanic Origin)";
		races[4] = "Hispanic ";
		races[5] = "White (not Hispanic Origin)";
		races[6] = "Mixed";
		races[7] = "Other";
			

		categories = new String[21];
		categories[0] = "Art";

		categories[1] = "Books";
		
		categories[2] = "Celebrities";

		categories[3] = "Family";
		categories[4] = "Food";

		categories[5] = "General";
		categories[6] = "History";
		categories[7] = "Life";
		categories[8] = "Love";
		categories[9] = "Medicine";
		categories[10] = "Movies";

		categories[11] = "Philosophy ";
		categories[12] = "Politics";
		categories[13] = "Pets";

		categories[14] = "RaiPoll";
		categories[15] = "Religion";

		categories[16] = "Science";
		categories[17] = "Space";
		categories[18] = "Sports";

		categories[19] = "Technology";
		categories[20] = "Travel";

		last_options = new String[7];
		last_options[0] = "Don't Know";
		last_options[1] = "Don't Care";
		last_options[2] = "All of the Above";
		last_options[3] = "None of the Above";
		last_options[4] = "Neutral";
		last_options[5] = "No opinion";
		last_options[6] = "I like bunnies";

		type = new String[2];
		type[0] = "Public";
		type[1] = "Private";

	}
}
