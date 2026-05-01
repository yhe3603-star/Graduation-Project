package com.dongmedicine.common.constant;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String API_BASE = "/api";

    public static final class User {
        private User() {}
        public static final String BASE = API_BASE + "/user";
        public static final String LOGIN = "/login";
        public static final String REGISTER = "/register";
        public static final String ME = "/me";
        public static final String VALIDATE = "/validate";
        public static final String CHANGE_PASSWORD = "/change-password";
        public static final String REFRESH_TOKEN = "/refresh-token";
        public static final String CAPTCHA = "/captcha";
        public static final String LIST = "/list";
        public static final String ROLE = "/role";
        public static final String BAN = "/ban";
        public static final String UNBAN = "/unban";
        public static final String TOKEN = "/token";
    }

    public static final class Knowledge {
        private Knowledge() {}
        public static final String BASE = API_BASE + "/knowledges";
        public static final String LIST = "/list";
        public static final String SEARCH = "/search";
        public static final String STATS = "/stats";
        public static final String FILTER_OPTIONS = "/filter-options";
        public static final String FAVORITE = "/favorite";
        public static final String VIEW = "/view";
    }

    public static final class Inheritor {
        private Inheritor() {}
        public static final String BASE = API_BASE + "/inheritors";
        public static final String LIST = "/list";
        public static final String SEARCH = "/search";
        public static final String HOT = "/hot";
    }

    public static final class Plant {
        private Plant() {}
        public static final String BASE = API_BASE + "/plants";
        public static final String LIST = "/list";
        public static final String SEARCH = "/search";
        public static final String SIMILAR = "/similar";
        public static final String RANDOM = "/random";
        public static final String BATCH = "/batch";
        public static final String STATS = "/stats";
    }

    public static final class Qa {
        private Qa() {}
        public static final String BASE = API_BASE + "/qa";
        public static final String LIST = "/list";
        public static final String SEARCH = "/search";
        public static final String CATEGORIES = "/categories";
        public static final String FEEDBACK = "/feedback";
        public static final String HOT = "/hot";
    }

    public static final class Quiz {
        private Quiz() {}
        public static final String BASE = API_BASE + "/quiz";
        public static final String RANDOM = "/random";
        public static final String SUBMIT = "/submit";
        public static final String RECORDS = "/records";
        public static final String QUESTIONS = "/questions";
        public static final String COUNT = "/count";
    }

    public static final class PlantGame {
        private PlantGame() {}
        public static final String BASE = API_BASE + "/plant-game";
        public static final String SUBMIT = "/submit";
        public static final String RECORDS = "/records";
    }

    public static final class Resource {
        private Resource() {}
        public static final String BASE = API_BASE + "/resources";
        public static final String LIST = "/list";
        public static final String SEARCH = "/search";
        public static final String HOT = "/hot";
        public static final String DOWNLOAD = "/download";
        public static final String PREVIEW = "/preview";
        public static final String VIEW = "/view";
        public static final String UPLOAD = "/upload";
    }

    public static final class Comment {
        private Comment() {}
        public static final String BASE = API_BASE + "/comments";
        public static final String MY = "/my";
        public static final String LIKE = "/like";
        public static final String REPORT = "/report";
    }

    public static final class Favorite {
        private Favorite() {}
        public static final String BASE = API_BASE + "/favorites";
        public static final String MY = "/my";
    }

    public static final class Feedback {
        private Feedback() {}
        public static final String BASE = API_BASE + "/feedback";
        public static final String MY = "/my";
    }

    public static final class Visual {
        private Visual() {}
        public static final String BASE = API_BASE + "/visual";
        public static final String PLANT_DISTRIBUTION = "/plant-distribution";
        public static final String PRESCRIPTION_FREQUENCY = "/prescription-frequency";
        public static final String KNOWLEDGE_STATS = "/knowledge-stats";
        public static final String PLANT_STATS = "/plant-stats";
    }

    public static final class Admin {
        private Admin() {}
        public static final String BASE = API_BASE + "/admin";
        public static final String DASHBOARD = "/dashboard";
        public static final String LOGS = "/logs";
    }

    public static final class File {
        private File() {}
        public static final String BASE = API_BASE + "/files";
        public static final String UPLOAD = "/upload";
        public static final String DOWNLOAD = "/download";
    }

    public static final class Chat {
        private Chat() {}
        public static final String BASE = API_BASE + "/chat";
        public static final String SEND = "/send";
    }
}
