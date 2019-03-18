package uk.ac.ebi.ddi.extservices.utils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;

public class RetryClientTest {

    private RetryClient retryClient = new RetryClient();


    @Test
    public void test() {
        List<Integer> tried = new ArrayList<>();
        retryClient.execute(x -> tried.add(1));
        Assert.assertEquals(1, tried.size());
    }

    @Test
    public void testExceptionOccurred() {
        List<Integer> tried = new ArrayList<>();
        try {
            retryClient.execute(x -> {
                tried.add(1);
                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
            });
        } catch (Exception e) {
            Assert.assertEquals(5, tried.size());
        }
    }

    @Test
    public void test429ExceptionOccurred() {
        List<Integer> tried = new ArrayList<>();
        try {
            retryClient.execute(x -> {
                tried.add(1);
                throw new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);
            });
        } catch (Exception e) {
            Assert.assertEquals(30, tried.size());
        }
    }
}