package org.jenkins.ci.plugins.jobimport.utils;

public final class Constants {

    public static final String SEPARATOR = "/";
    public static final String JOBS_SEPARATOR = "jobs";
    public static final String JOBS_SEPARATOR_S = JOBS_SEPARATOR + SEPARATOR;
    public static final String JOBS_SEPARATOR_F = SEPARATOR + JOBS_SEPARATOR_S;

    public static final String URL_NAME= "job-import";
    public static final String REMOTE_URL_PARAM = "remoteUrl";
    public static final String JOB_URL_PARAM = "jobUrl";
    public static final String XML_API_QUERY = "api/xml?tree=jobs[name,url,description]";
    public static final String RECURSIVE_PARAM = "recursiveSearch";
    public static final String LOCAL_FOLDER_PARAM = "localFolder";

    public static final int MAX_STR_LEN = 4096;
}
