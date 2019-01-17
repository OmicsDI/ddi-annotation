package uk.ac.ebi.ddi.extservices.utils;

import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.Collections;

public class RetryClient {

    private static final int RETRIES = 5;
    private RetryTemplate retryTemplate = new RetryTemplate();

    public RetryClient() {
        SimpleRetryPolicy policy =
                new SimpleRetryPolicy(RETRIES, Collections.singletonMap(Exception.class, true));
        retryTemplate.setRetryPolicy(policy);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(2000);
        backOffPolicy.setMultiplier(1.6);
        retryTemplate.setBackOffPolicy(backOffPolicy);
    }

    protected RetryTemplate getRetryTemplate() {
        return retryTemplate;
    }
}
