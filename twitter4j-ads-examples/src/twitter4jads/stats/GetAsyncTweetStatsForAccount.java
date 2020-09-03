package twitter4jads.stats;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import twitter4jads.*;
import twitter4jads.api.TwitterAdsStatApi;
import twitter4jads.internal.models4j.TwitterException;
import twitter4jads.models.Granularity;
import twitter4jads.models.TwitterSegmentationType;
import twitter4jads.models.ads.*;
import twitter4jads.util.TwitterAdUtil;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * User: shivraj
 * Date: 12/05/16 2:08 PM.
 */
public class GetAsyncTweetStatsForAccount extends BaseAdsTest {

    public static void main(String[] args) {
        TwitterAds twitterAdsInstance = getTwitterAdsInstance();
        TwitterAdsStatApi statApi = twitterAdsInstance.getStatApi();
        long until = DateTime.parse("2020-09-03").getMillis();
        long since = DateTime.parse("2020-09-02").getMillis();
        try {
            BaseAdsResponse<JobDetails> twitterAsyncJob = statApi.createAsyncJob(accountId, TwitterEntityType.PROMOTED_TWEET, Lists.newArrayList("4qjtjc", "4qhi5u"), since, until, Boolean.TRUE, Granularity.DAY, Placement.ALL_ON_TWITTER, Optional.of(TwitterSegmentationType.LOCATIONS));
            BaseAdsListResponseIterable<JobDetails> jobExecutionDetails;
            boolean flag;
            long timeOut = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);
            do {
                flag = false; //continue iterating as long as status of job of job is either queued, uploading or processing
                TwitterAdUtil.reallySleep(10000L);
                jobExecutionDetails = statApi.getJobExecutionDetails(accountId, Lists.newArrayList(twitterAsyncJob.getData().getJobId()));

                for (BaseAdsListResponse<JobDetails> base : jobExecutionDetails) {
                    List<JobDetails> baselist = base.getData();
                    for (JobDetails jd : baselist) {
                        if ((jd != null) && (jd.getStatus() != TwitterAsyncQueryStatus.SUCCESS)) {
                            flag = true;
                        }
                    }
                }
            } while (flag && System.currentTimeMillis() <= timeOut);

            List<TwitterEntityStatistics> twitterEntityStatsList = Lists.newArrayList();

            for (BaseAdsListResponse<JobDetails> base : jobExecutionDetails) {
                List<JobDetails> baselist = base.getData();
                for (JobDetails jd : baselist) {
                    BaseAdsListResponse<TwitterEntityStatistics> allTwitterEntityStat = statApi.fetchJobDataAsync(jd.getUrl());
                    if (allTwitterEntityStat == null || allTwitterEntityStat.getData() == null) {
                        continue;
                    }
                    twitterEntityStatsList.addAll(allTwitterEntityStat.getData());
                }

                System.out.println(twitterEntityStatsList.size());
            }
        } catch (TwitterException e) {
            System.err.println(e.getErrorMessage());
        }
    }
}