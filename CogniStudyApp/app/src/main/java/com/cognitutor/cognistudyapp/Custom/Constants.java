package com.cognitutor.cognistudyapp.Custom;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryDayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryMonthStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryTridayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectDayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectMonthStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentSubjectTridayStats;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTotalDayStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTotalMonthStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTotalTridayStats;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Lance on 12/27/2015.
 */
public class Constants {

    public static class Parse {
        public static final String APPLICATION_ID = "iT8NyJO0dChjLyfVsHUTM8UZQLSBBJLxd43AX9IY";
        public static final String CLIENT_KEY = "mtCu0UsCYrVvQVIEkMlNkLoEFLlsIabVnWhTXvdA";
    }

    public static class Roles {
        public static final String TUTOR = "TUTOR";
        public static final String ADMIN = "ADMIN";
    }

    public static String[] getAllConstants(Class c) {

        Field[] fields = c.getFields();
        String[] constants = new String[fields.length];
        for(int i=0; i<fields.length; i++) {
            Object s = null;
            try {
                s = fields[i].get(c);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            constants[i] = (String) s;
        }
        return constants;
    }

    public static String getRandomConstant(Class c) {

        Field[] fields = c.getFields();
        Random rand = new Random();
        try {
            return (String) fields[rand.nextInt(fields.length)].get(c);
        } catch (IllegalAccessException e) { e.printStackTrace(); return null; }
    }

    public static class Analytics {

        public static class TestSectionType {
            public static final String OVERALL = "Overall";
            public static final String SUBJECT = "Subject";
            public static final String CATEGORY = "Category";
        }

        public static class RollingDateRange {
            public static final String ALL_TIME = "All Time";
            public static final String PAST_MONTH = "Past Month";
            public static final String PAST_WEEK = "Past Week";

            public static String[] getRollingStatsTypes() {
                return new String[]{ALL_TIME, PAST_MONTH, PAST_WEEK};
            }
        }

        public static class BlockType {
            public static final String MONTH = "MONTH";
            public static final String TRIDAY = "TRIDAY";
            public static final String DAY = "DAY";
        }

        public static HashMap<String, String> RollingDateRangeToBlockType = new HashMap<String, String>();
        static {
            RollingDateRangeToBlockType.put(RollingDateRange.ALL_TIME, BlockType.MONTH);
            RollingDateRangeToBlockType.put(RollingDateRange.PAST_MONTH, BlockType.TRIDAY);
            RollingDateRangeToBlockType.put(RollingDateRange.PAST_WEEK, BlockType.DAY);
        }

        public static HashMap<String, Integer> RollingDateRangeToNumSmallerBlocks = new HashMap<String, Integer>();
        static {
            RollingDateRangeToNumSmallerBlocks.put(RollingDateRange.ALL_TIME, 12);
            RollingDateRangeToNumSmallerBlocks.put(RollingDateRange.PAST_MONTH, 10);
            RollingDateRangeToNumSmallerBlocks.put(RollingDateRange.PAST_WEEK, 7);
        }

        public static HashMap<String, String> RollingDateRangeToDoubleBarTitle = new HashMap<String, String>();
        static {
            RollingDateRangeToDoubleBarTitle.put(RollingDateRange.ALL_TIME, "Progress over the Past Year");
            RollingDateRangeToDoubleBarTitle.put(RollingDateRange.PAST_MONTH, "Progress over the Past Month");
            RollingDateRangeToDoubleBarTitle.put(RollingDateRange.PAST_WEEK, "Progress over the Past Week");
        }

        public static Table<String, String, Class> TestSectionTypeAndBlockTypeToStudentBlockStatsClass = HashBasedTable.create();
        static {
            TestSectionTypeAndBlockTypeToStudentBlockStatsClass.put(TestSectionType.OVERALL, BlockType.MONTH, StudentTotalMonthStats.class);
            TestSectionTypeAndBlockTypeToStudentBlockStatsClass.put(TestSectionType.OVERALL, BlockType.TRIDAY, StudentTotalDayStats.class);
            TestSectionTypeAndBlockTypeToStudentBlockStatsClass.put(TestSectionType.OVERALL, BlockType.DAY, StudentTotalDayStats.class);
            TestSectionTypeAndBlockTypeToStudentBlockStatsClass.put(TestSectionType.SUBJECT, BlockType.MONTH, StudentSubjectMonthStats.class);
            TestSectionTypeAndBlockTypeToStudentBlockStatsClass.put(TestSectionType.SUBJECT, BlockType.TRIDAY, StudentSubjectDayStats.class);
            TestSectionTypeAndBlockTypeToStudentBlockStatsClass.put(TestSectionType.SUBJECT, BlockType.DAY, StudentSubjectDayStats.class);
            TestSectionTypeAndBlockTypeToStudentBlockStatsClass.put(TestSectionType.CATEGORY, BlockType.MONTH, StudentCategoryMonthStats.class);
            TestSectionTypeAndBlockTypeToStudentBlockStatsClass.put(TestSectionType.CATEGORY, BlockType.TRIDAY, StudentCategoryDayStats.class);
            TestSectionTypeAndBlockTypeToStudentBlockStatsClass.put(TestSectionType.CATEGORY, BlockType.DAY, StudentCategoryDayStats.class);
        }
    }

    public static class ParseObjectColumns {
        public static final String objectId = "objectId";
        public static final String ACL = "ACL";
        public static final String createdAt = "createdAt";
        public static final String updatedAt = "updatedAt";
    }

    public static class Loading {
        public static final int QUESTION_LOADING_TIME = 2000;
    }

    public static class MenuItem {
        public static final String SUGGESTED_QUESTIONS = "Suggested Questions";
        public static final String BOOKMARKS = "Bookmarks";
        public static final String ACHIEVEMENTS = "Achievements";
        public static final String SHOP = "Shop";
        public static final String SETTINGS = "Settings";
        public static final String HELP = "Help";
        public static final String SIGN_OUT = "Sign Out";

        public static class Attribute {
            public static final String LABEL = "LABEL";
            public static final String ICON = "ICON";
        }
    }

    public static class NotificationData {
        public static final String title = "title";
        public static final String alert = "alert";
        public static final String ACTIVITY = "ACTIVITY";
        public static class Activity {
            public static final String MAIN_ACTIVITY = "MAIN_ACTIVITY";
            public static final String CONVERSATION_ACTIVITY = "CONVERSATION_ACTIVITY";
            public static final String SUGGESTED_QUESTION_ACTIVITY = "SUGGESTED_QUESTION_ACTIVITY";
        }
    }

    public static class ClassName {
        public static final String StudentCategoryDayStats = StudentCategoryDayStats.class.getSimpleName();
        public static final String StudentCategoryTridayStats = StudentCategoryTridayStats.class.getSimpleName();
        public static final String StudentCategoryMonthStats = StudentCategoryMonthStats.class.getSimpleName();
        public static final String StudentSubjectDayStats = StudentSubjectDayStats.class.getSimpleName();
        public static final String StudentSubjectTridayStats = StudentSubjectTridayStats.class.getSimpleName();
        public static final String StudentSubjectMonthStats = StudentSubjectMonthStats.class.getSimpleName();
        public static final String StudentTotalDayStats = StudentTotalDayStats.class.getSimpleName();
        public static final String StudentTotalTridayStats = StudentTotalTridayStats.class.getSimpleName();
        public static final String StudentTotalMonthStats = StudentTotalMonthStats.class.getSimpleName();
    }

    public static class IntentExtra {
        public static final String FINISH_CHALLENGE_ACTIVITY = "FINISH_CHALLENGE_ACTIVITY";
        public static final String FINISH_NEW_CHALLENGE_ACTIVITY = "FINISH_NEW_CHALLENGE_ACTIVITY";
        public static final String REFRESH_CHALLENGE_LIST = "REFRESH_CHALLENGE_LIST";
        public static final String CHALLENGE_ID = "CHALLENGE_ID";
        public static final String RESPONSE_ID = "RESPONSE_ID";
        public static final String USER1OR2 = "USER1OR2";

        public static final String QUESTION_ID = "QUESTION_ID";

        public static class ParentActivity {
            public static final String PARENT_ACTIVITY = "PARENT_ACTIVITY";

            public static final String CHALLENGE_ACTIVITY = "CHALLENGE_ACTIVITY";
            public static final String SUGGESTED_QUESTIONS_ACTIVITY = "SUGGESTED_QUESTIONS_ACTIVITY";
            public static final String MAIN_ACTIVITY = "MAIN_ACTIVITY";
            public static final String QUESTION_HISTORY_ACTIVITY = "QUESTION_HISTORY_ACTIVITY";
            public static final String BOOKMARKS_LIST_ACTIVITY = "BOOKMARKS_LIST_ACTIVITY";
        }
    }

    enum CacheOrNetwork {
        CACHE, NETWORK
    }

    public static class ChallengeAttribute {
        public static final int NUM_DAYS_PER_TURN = 3;

        public static class Winner {
            public static final String NO_WINNER = "NO_WINNER";
        }
    }

    public static class Questions {
        public static final int NUM_QUESTIONS_PER_TURN = 3;
    }

    public static class QuestionRating {
        public static final String NOT_RATED = "NOT_RATED";
    }

    public static class GameBoard {
        public static int NUM_ROWS = 11;
        public static int NUM_COLUMNS = 8;
        public static int NUM_SHIPS = 6;
    }

    public static class GameBoardPositionStatus {
        public static final String UNKNOWN = "UNKNOWN";
        public static final String HIT = "HIT";
        public static final String MISS = "MISS";
        public static final String DETECTION = "DETECTION";
    }

    public static class OpponentType {
        public static final String FRIEND = "Friend";
        public static final String RANDOM = "Random";
        public static final String COMPUTER = "Computer";
        public static final String PRACTICE = "Practice";

        public static String[] getOpponentTypes() {
            return new String[] {FRIEND, RANDOM, COMPUTER, PRACTICE};
        }
    }

    // <editor-fold desc="Tests, Subjects and Categories">
    public static class Test {
        public static final String SAT = "SAT";
        public static final String ACT = "ACT";
        public static final String BOTH = "Both";

        public static String[] getTests() {
            return new String[] {SAT, ACT, BOTH} ;
        }
    }

    public static class Subject {
        public static final String OVERALL = "Overall";
        public static final String ALL_SUBJECTS = "All Subjects";
        public static final String READING = "Reading";
        public static final String MATH = "Math";
        public static final String ENGLISH = "English";
        public static final String SCIENCE = "Science";

        public static String[] getSubjects() {
            return new String[] {READING, MATH, ENGLISH, SCIENCE} ;
        }

        public static String[] getSubjectsPlusOverall() {
            return new String[] {OVERALL, READING, MATH, ENGLISH, SCIENCE} ;
        }

        public static String[] getSubjectsPlusAll() {
            return new String[] {ALL_SUBJECTS, READING, MATH, ENGLISH, SCIENCE} ;
        }
    }

    public static class Category {
        public static final String ALL_CATEGORIES = "All Categories";
        public static final String SOCIAL_STUDIES_SCIENCE = "Social Studies/Science";
        public static final String ARTS_LITERATURE = "Arts/Literature";
        public static final String PRE_ALGEBRA = "Pre-Algebra";
        public static final String ELEMENTARY_ALGEBRA = "Elementary Algebra";
        public static final String INTERMEDIATE_ALGEBRA = "Intermediate Algebra";
        public static final String GEOMETRY = "Geometry";
        public static final String TRIGONOMETRY = "Trigonometry";
        public static final String DATA_ANALYSIS = "Data Analysis";
        public static final String USAGE_AND_MECHANICS = "Usage and Mechanics";
        public static final String RHETORICAL_SKILLS = "Rhetorical Skills";
        public static final String DATA_REPRESENTATION = "Data Representation";
        public static final String RESEARCH_SUMMARIES = "Research Summaries";
        public static final String CONFLICTING_VIEWPOINTS = "Conflicting Viewpoints";

        public static String[] getCategories() {
            return new String[] {
                    SOCIAL_STUDIES_SCIENCE, ARTS_LITERATURE, PRE_ALGEBRA, ELEMENTARY_ALGEBRA, GEOMETRY, TRIGONOMETRY,
                    INTERMEDIATE_ALGEBRA, DATA_ANALYSIS, USAGE_AND_MECHANICS, RHETORICAL_SKILLS,
                    DATA_REPRESENTATION, RESEARCH_SUMMARIES, CONFLICTING_VIEWPOINTS
            } ;
        }
    }

    public static final Map<String, String[]> TestToSubject;
    static {
        Map<String, String[]> map = new HashMap<String, String[]>();
        String[] SAT_Subjects = new String[]{Subject.READING, Subject.MATH, Subject.ENGLISH};
        String[] ACT_BOTH_Subjects = new String[]{Subject.READING, Subject.MATH, Subject.ENGLISH, Subject.SCIENCE};
        map.put(Test.SAT, SAT_Subjects);
        map.put(Test.ACT, ACT_BOTH_Subjects);
        map.put(Test.BOTH, ACT_BOTH_Subjects);
        TestToSubject = Collections.unmodifiableMap(map);
    }

    public static final Map<String, String[]> TestToCategory;
    static {
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put(Test.SAT, new String[] {
                Category.SOCIAL_STUDIES_SCIENCE,
                Category.ARTS_LITERATURE,
                Category.PRE_ALGEBRA,
                Category.ELEMENTARY_ALGEBRA,
                Category.GEOMETRY,
                Category.TRIGONOMETRY,
                Category.INTERMEDIATE_ALGEBRA,
                Category.DATA_ANALYSIS,
                Category.USAGE_AND_MECHANICS,
                Category.RHETORICAL_SKILLS
        });
        map.put(Test.ACT, new String[] {
                Category.SOCIAL_STUDIES_SCIENCE,
                Category.ARTS_LITERATURE,
                Category.PRE_ALGEBRA,
                Category.ELEMENTARY_ALGEBRA,
                Category.GEOMETRY,
                Category.TRIGONOMETRY,
                Category.INTERMEDIATE_ALGEBRA,
                Category.USAGE_AND_MECHANICS,
                Category.RHETORICAL_SKILLS,
                Category.DATA_REPRESENTATION,
                Category.RESEARCH_SUMMARIES,
                Category.CONFLICTING_VIEWPOINTS
        });
        map.put(Test.BOTH, new String[] {
                Category.SOCIAL_STUDIES_SCIENCE,
                Category.ARTS_LITERATURE,
                Category.PRE_ALGEBRA,
                Category.ELEMENTARY_ALGEBRA,
                Category.GEOMETRY,
                Category.TRIGONOMETRY,
                Category.INTERMEDIATE_ALGEBRA,
                Category.DATA_ANALYSIS,
                Category.USAGE_AND_MECHANICS,
                Category.RHETORICAL_SKILLS,
                Category.DATA_REPRESENTATION,
                Category.RESEARCH_SUMMARIES,
                Category.CONFLICTING_VIEWPOINTS
        });
        TestToCategory = Collections.unmodifiableMap(map);
    }

    public static final Map<String, String[]> SubjectToCategory;
    static {
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put(Subject.READING, new String[]{Category.SOCIAL_STUDIES_SCIENCE, Category.ARTS_LITERATURE});
        map.put(Subject.MATH, new String[]{Category.PRE_ALGEBRA, Category.ELEMENTARY_ALGEBRA, Category.GEOMETRY, Category.TRIGONOMETRY, Category.INTERMEDIATE_ALGEBRA, Category.DATA_ANALYSIS});
        map.put(Subject.ENGLISH, new String[]{Category.USAGE_AND_MECHANICS, Category.RHETORICAL_SKILLS});
        map.put(Subject.SCIENCE, new String[]{Category.DATA_REPRESENTATION, Category.RESEARCH_SUMMARIES, Category.CONFLICTING_VIEWPOINTS});
        SubjectToCategory = Collections.unmodifiableMap(map);
    }

    // </editor-fold>

    // <editor-fold desc="Other Static Data Classes">

    public static class UserType {
        public static final String STUDENT = "STUDENT";
        public static final String TUTOR = "TUTOR";
        public static final String ADMIN = "ADMIN";
    }

    public static class AchievementGroupType {
        public static final String TOTAL_Q_CORRECT = "TOTAL_Q_CORRECT";
        public static final String STREAK_Q_CORRECT = "STREAK_Q_CORRECT";
        public static final String READING_Q_CORRECT = "READING_Q_CORRECT";
        public static final String MATH_Q_CORRECT = "MATH_Q_CORRECT";
        public static final String ENGLISH_Q_CORRECT = "ENGLISH_Q_CORRECT";
        public static final String TOTAL_SHIPS_DESTROYED = "TOTAL_SHIPS_DESTROYED";
        public static final String STREAK_HITS = "STREAK_HITS";
        public static final String CHALLENGES_WON_PLAYER = "CHALLENGES_WON_PLAYER";
        public static final String CHALLENGES_WON_COMPUTER = "CHALLENGES_WON_COMPUTER";
        public static final String BOUGHT_ITEM = "BOUGHT_ITEM";
        public static final String SHARED_ON_FB = "SHARED_ON_FB";
    }

    public static class ChallengeType {
        public static final String TWO_PLAYER = "TWO_PLAYER";
        public static final String ONE_PLAYER = "ONE_PLAYER";
        public static final String PRACTICE = "PRACTICE";
    }

    public static class ShopItemGroupType {
        public static final String SHIP_ABILITY = "SHIP_ABILITY";
        public static final String SHIP_SKIN = "SHIP_SKIN";
        public static final String SHOT_SKIN = "SHOT_SKIN";
    }

    public static class ShipType {
        public static final String ERASER = "ERASER";
        public static final String YELLOW_PENCIL = "YELLOW_PENCIL";
        public static final String GREEN_PENCIL = "GREEN_PENCIL";
        public static final String CALCULATOR = "CALCULATOR";
        public static final String PEN = "PEN";
        public static final String RULER = "RULER";
    }

    public static class CustomizableType extends ShipType {
        public static final String ABILITY_ERASER = "ABILITY_ERASER";
        public static final String ABILITY_PENCIL = "ABILITY_PENCIL";
        public static final String ABILITY_PEN = "ABILITY_PEN";
        public static final String ABILITY_CALCULATOR = "ABILITY_CALCULATOR";
        public static final String ABILITY_RULER = "ABILITY_RULER";
        public static final String TARGET = "TARGET";
    }

    public static class ShopItemType {
        public static final String ERASE_SHIP = "ERASE_SHIP";
        public static final String FLYING_PENCIL = "FLYING_PENCIL";
        public static final String BLOTCHY_PEN = "BLOTCHY_PEN";
        public static final String FOUR_FUNCTION_FRENZY = "FOUR_FUNCTION_FRENZY";
        public static final String MEASURE_ROW = "MEASURE_ROW";
        public static final String SKIN_ERASER_DEFAULT = "SKIN_ERASER_DEFAULT";
        public static final String SKIN_ERASER_WHITE = "SKIN_ERASER_WHITE";
        public static final String SKIN_ERASER_PINK_CAP = "SKIN_ERASER_PINK_CAP";
        public static final String SKIN_YELLOW_PENCIL_DEFAULT = "SKIN_YELLOW_PENCIL_DEFAULT";
        public static final String SKIN_YELLOW_PENCIL_BEE_COLORED = "SKIN_YELLOW_PENCIL_BEE_COLORED";
        public static final String SKIN_YELLOW_PENCIL_MECHANICAL = "SKIN_YELLOW_PENCIL_MECHANICAL";
        public static final String SKIN_GREEN_PENCIL_DEFAULT = "SKIN_GREEN_PENCIL_DEFAULT";
        public static final String SKIN_GREEN_PENCIL_GREEN_SWIRL = "SKIN_GREEN_PENCIL_GREEN_SWIRL";
        public static final String SKIN_GREEN_PENCIL_TREE = "SKIN_GREEN_PENCIL_TREE";
        public static final String SKIN_PEN_DEFAULT = "SKIN_PEN_DEFAULT";
        public static final String SKIN_PEN_FOUNTAIN = "SKIN_PEN_FOUNTAIN";
        public static final String SKIN_PEN_FEATHER = "SKIN_PEN_FEATHER";
        public static final String SKIN_CALCULATOR_DEFAULT = "SKIN_CALCULATOR_DEFAULT";
        public static final String SKIN_CALCULATOR_GOLDEN = "SKIN_CALCULATOR_GOLDEN";
        public static final String SKIN_CALCULATOR_BLACK_GREEN = "SKIN_CALCULATOR_BLACK_GREEN";
        public static final String SKIN_RULER_DEFAULT = "SKIN_RULER_DEFAULT";
        public static final String SKIN_RULER_GOLDEN = "SKIN_RULER_GOLDEN";
        public static final String SKIN_RULER_MEASURING_TAPE = "SKIN_RULER_MEASURING_TAPE";
        public static final String SKIN_TARGET_DEFAULT = "SKIN_TARGET_DEFAULT";
        public static final String SKIN_TARGET_CHECK_MARK = "SKIN_TARGET_CHECK_MARK";
        public static final String SKIN_TARGET_X_MARK = "SKIN_TARGET_X_MARK";
        public static final String SKIN_TARGET_SMILEY_FACE = "SKIN_TARGET_SMILEY_FACE";
        public static final String SKIN_TARGET_TARGET = "SKIN_TARGET_TARGET";
    }

    public static class RotationType {
        public static final String VERTICAL = "VERTICAL";
        public static final String HORIZONTAL = "HORIZONTAL";
    }

    public static class ResponseStatusType {
        public static final String UNANSWERED = "Unanswered";
        public static final String CORRECT = "Correct";
        public static final String INCORRECT = "INCORRECT";
    }

    public static class ReviewStatusType {
        public static final String PENDING = "PENDING";
        public static final String TUTOR_APPROVED = "TUTOR_APPROVED";
        public static final String APPROVED = "APPROVED";
        public static final String DENIED = "DENIED";
    }

    public static class AnswerLabelType {
        public static final String LETTER = "LETTER";
        public static final String ROMAN_NUMERAL = "ROMAN_NUMERAL";
    }

    public static Map<String, String[]> ShopItemGroupTypeToCustomizableTypes;
    static {
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put(ShopItemGroupType.SHIP_ABILITY, new String[] {
                CustomizableType.ABILITY_ERASER,
                CustomizableType.ABILITY_PENCIL,
                CustomizableType.ABILITY_PEN,
                CustomizableType.ABILITY_CALCULATOR,
                CustomizableType.ABILITY_RULER
        });
        map.put(ShopItemGroupType.SHIP_SKIN, getAllConstants(ShipType.class));
        map.put(ShopItemGroupType.SHOT_SKIN, new String[] {
                CustomizableType.TARGET
        });
        ShopItemGroupTypeToCustomizableTypes = Collections.unmodifiableMap(map);
    }

    public static Map<String, String[]> CustomizableTypeToShopItemTypes;
    static {
        Map<String, String[]> map = new HashMap<String, String[]>();
        map.put(CustomizableType.ABILITY_ERASER, new String[] { ShopItemType.ERASE_SHIP });
        map.put(CustomizableType.ABILITY_PENCIL, new String[] { ShopItemType.FLYING_PENCIL });
        map.put(CustomizableType.ABILITY_PEN, new String[] { ShopItemType.BLOTCHY_PEN });
        map.put(CustomizableType.ABILITY_CALCULATOR, new String[] { ShopItemType.FOUR_FUNCTION_FRENZY });
        map.put(CustomizableType.ABILITY_RULER, new String[] { ShopItemType.MEASURE_ROW });
        map.put(CustomizableType.ERASER, new String[] {
                ShopItemType.SKIN_ERASER_DEFAULT,
                ShopItemType.SKIN_ERASER_WHITE,
                ShopItemType.SKIN_ERASER_PINK_CAP
        });
        map.put(CustomizableType.YELLOW_PENCIL, new String[] {
                ShopItemType.SKIN_YELLOW_PENCIL_DEFAULT,
                ShopItemType.SKIN_YELLOW_PENCIL_BEE_COLORED,
                ShopItemType.SKIN_YELLOW_PENCIL_MECHANICAL
        });
        map.put(CustomizableType.GREEN_PENCIL, new String[] {
                ShopItemType.SKIN_GREEN_PENCIL_DEFAULT,
                ShopItemType.SKIN_GREEN_PENCIL_GREEN_SWIRL,
                ShopItemType.SKIN_GREEN_PENCIL_TREE
        });
        map.put(CustomizableType.PEN, new String[] {
                ShopItemType.SKIN_PEN_DEFAULT,
                ShopItemType.SKIN_PEN_FOUNTAIN,
                ShopItemType.SKIN_PEN_FEATHER
        });
        map.put(CustomizableType.CALCULATOR, new String[] {
                ShopItemType.SKIN_CALCULATOR_DEFAULT,
                ShopItemType.SKIN_CALCULATOR_BLACK_GREEN,
                ShopItemType.SKIN_CALCULATOR_GOLDEN
        });
        map.put(CustomizableType.RULER, new String[] {
                ShopItemType.SKIN_RULER_DEFAULT,
                ShopItemType.SKIN_RULER_GOLDEN,
                ShopItemType.SKIN_RULER_MEASURING_TAPE
        });
        map.put(CustomizableType.TARGET, new String[] {
                ShopItemType.SKIN_TARGET_DEFAULT,
                ShopItemType.SKIN_TARGET_CHECK_MARK,
                ShopItemType.SKIN_TARGET_X_MARK,
                ShopItemType.SKIN_TARGET_SMILEY_FACE,
                ShopItemType.SKIN_TARGET_TARGET,
        });
        CustomizableTypeToShopItemTypes = Collections.unmodifiableMap(map);
    }
    // </editor-fold>

    public static class CloudCodeFunction {
        public static final String DELETE_CHALLENGE = "deleteChallenge";
        public static final String CHOOSE_THREE_QUESTIONS = "chooseThreeQuestionsV2";
    }

    public static class PinNames {
        //TODO: Remove deprecated PinNames later

        public static final String PinData = "PinData";

        public static final String PeopleSearch = "PeopleSearch";
        public static final String PublicUserData = "PublicUserData";
        public static final String Challenge = "Challenge";
        public static final String CurrentUser = "CurrentUser";
        public static final String BlockStats = "BlockStats";
        public static final String fbFriends = "fbFriends"; //deprecated
    }
}
