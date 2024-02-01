package com.krasnovm.FirstTryBot.service.ExchangeRatesService;

import com.krasnovm.FirstTryBot.client.CbrClient;
import com.krasnovm.FirstTryBot.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

@Service
public class ExchangeRatesServiceImpl implements ExchangeRatesService {

    //public static final Logger LOG = LoggerFactory.getLogger(ExchangeRatesServiceImpl.class);
    public static final String USD_XPATH = "/ValCurs//Valute[@ID='R01235']/Value";
    public static final String EUR_XPATH = "/ValCurs//Valute[@ID='R01239']/Value";
    public static final String CNY_XPATH = "/ValCurs//Valute[@ID='R01375']/Value";
    public static final String GBP_XPATH = "/ValCurs//Valute[@ID='R01035']/Value";
    public static final String TRY_XPATH = "/ValCurs//Valute[@ID='R01700J']/Value";
    public static final String UAH_XPATH = "/ValCurs//Valute[@ID='R01720']/Value";
    public static final String JPY_XPATH = "/ValCurs//Valute[@ID='R01820']/Value";
    public static final String KRW_XPATH = "/ValCurs//Valute[@ID='R01815']/Value";

    @Autowired
    private CbrClient client;

    @Cacheable (value = "usd", unless = "#result == null or #result.isEmpty()")
    @Override
    public String getUSDExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRates();
        return extractCurrencyValueFromXml(xml, USD_XPATH);
    }

    @Cacheable (value = "eur", unless = "#result == null or #result.isEmpty()")
    @Override
    public String getEURExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRates();
        return extractCurrencyValueFromXml(xml, EUR_XPATH);
    }

    @Cacheable (value = "cny", unless = "#result == null or #result.isEmpty()")
    @Override
    public String getCNYExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRates();
        return extractCurrencyValueFromXml(xml, CNY_XPATH);
    }

    @Cacheable (value = "gbp", unless = "#result == null or #result.isEmpty()")
    @Override
    public String getGBPExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRates();
        return extractCurrencyValueFromXml(xml, GBP_XPATH);
    }

    @Cacheable (value = "try", unless = "#result == null or #result.isEmpty()")
    @Override
    public String getTRYExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRates();
        return extractCurrencyValueFromXml(xml, TRY_XPATH);
    }

    @Cacheable (value = "uah", unless = "#result == null or #result.isEmpty()")
    @Override
    public String getUAHExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRates();
        return extractCurrencyValueFromXml(xml, UAH_XPATH);
    }

    @Cacheable (value = "jpy", unless = "#result == null or #result.isEmpty()")
    @Override
    public String getJPYExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRates();
        return extractCurrencyValueFromXml(xml, JPY_XPATH);
    }

    @Cacheable (value = "krw", unless = "#result == null or #result.isEmpty()")
    @Override
    public String getKRWExchangeRate() throws ServiceException {
        String xml = client.getCurrencyRates();
        return extractCurrencyValueFromXml(xml, KRW_XPATH);
    }




    @Caching(evict = {
            @CacheEvict("usd"),
            @CacheEvict("eur"),
            @CacheEvict("cny"),
            @CacheEvict("gbp"),
            @CacheEvict("try"),
            @CacheEvict("uah"),
            @CacheEvict("jpy"),
            @CacheEvict("krw")
    })
    @Override
    public void clearCache() {
        //LOG.info("Cache cleared");
    }

    private static String extractCurrencyValueFromXml(String xml, String xpathExpression) throws ServiceException {
        var source = new InputSource(new StringReader(xml));
        try {
            var xpath = XPathFactory.newInstance().newXPath();
            var document = (Document) xpath.evaluate("/", source, XPathConstants.NODE);

            return xpath.evaluate(xpathExpression, document);
        } catch (XPathExpressionException e) {
            throw new ServiceException("Unable to parse XML", e);
        }
    }
}
