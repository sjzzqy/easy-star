package org.qzzy.configurer.xss;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Qzzy
 * @Generated: 2023/4/28 9:00
 */
@Component
@Configuration
@EnableCaching
public class XssFilterConfigurer {
    /**
     * xss过滤拦截器
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    public FilterRegistrationBean xssFilterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new WebSecurityFilter());
        filterRegistrationBean.setOrder(Integer.MAX_VALUE - 1);
        filterRegistrationBean.setEnabled(true);
        filterRegistrationBean.addUrlPatterns("/*");
        Map<String, String> initParameters = new HashMap();
        // excludes用于配置不需要参数过滤的请求url
        initParameters.put("excludes", "/favicon.ico,/static/*");
        // isIncludeRichText主要用于设置富文本内容是否需要过滤
        initParameters.put("isIncludeRichText", "true");
        filterRegistrationBean.setInitParameters(initParameters);
        return filterRegistrationBean;
    }
}
