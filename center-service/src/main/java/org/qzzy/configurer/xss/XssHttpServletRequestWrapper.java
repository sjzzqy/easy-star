package org.qzzy.configurer.xss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * 重新包装一下Request。重写一些获取参数的方法，将每个参数都进行过滤
 *
 * @author Qzzy
 * @Generated: 2023/4/28 8:46
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final Logger logger = LoggerFactory.getLogger(XssHttpServletRequestWrapper.class);
    private HttpServletRequest orgRequest = null;

    private boolean isIncludeRichText = false;
    private boolean isUpData = false;

    public XssHttpServletRequestWrapper(HttpServletRequest request, boolean isIncludeRichText) {
        super(request);
        orgRequest = request;
        this.isIncludeRichText = isIncludeRichText;
        String contentType = request.getContentType();
        if (null != contentType) {
            isUpData = contentType.startsWith("multipart");
        }
    }

    /**
     * 覆盖getParameter方法，将参数名和参数值都做xss过滤.
     * 如果需要获得原始的值，则通过super.getParameterValues(name)来获取
     * getParameterNames,getParameterValues和getParameterMap也可能需要覆盖
     */
    @Override
    public String getParameter(String name) {
        // ("content".equals(name) || name.endsWith("WithHtml")) &&
        if (!isIncludeRichText) {
            return super.getParameter(name);
        }
//		name = XssUtils.stripXSSAndSql(name); 参数名不执行XSS，存在参数接收问题
        String value = super.getParameter(name);
        if (StringUtils.isBlank(value))
            return value;

        logger.info("Parameter Before Param {} = {}", name, value);
        if (StringUtils.isNotBlank(value)) {
            value = XssUtils.stripXSSAndSql(value);
        }
        logger.info("Parameter After Param {} = {}", name, value);
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] arr = super.getParameterValues(name);
        if (arr != null) {
            logger.info("ParameterValues Before Param {} = {}", name, arr);
            for (int i = 0; i < arr.length; i++) {
                arr[i] = XssUtils.stripXSSAndSql(arr[i]);
            }
            logger.info("ParameterValues After Param {} = {}", name, arr);
        }

        return arr;
    }

    /**
     * 覆盖getHeader方法，将参数名和参数值都做xss过滤。<br/>
     * 如果需要获得原始的值，则通过super.getHeaders(name)来获取<br/>
     * getHeaderNames 也可能需要覆盖
     */
    @Override
    public String getHeader(String name) {
//		name = XssUtils.stripXSSAndSql(name);
//		String value = super.getHeader(name);
//		if (StringUtils.isNotBlank(value)) {
//			value = XssUtils.stripXSSAndSql(value);
//		}
        return XssUtils.stripXSSAndSql(super.getHeader(name));
    }

    @Override
    public String getQueryString() {
        return XssUtils.stripXSSAndSql(super.getQueryString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (isUpData) {
            return super.getInputStream();
        } else {
            // 处理原request的流中的数据

            String str = getRequestBody(super.getInputStream());
            logger.info("RequestBody XSS Before {}", str);
            ObjectMapper om = new ObjectMapper();
            Map<String, Object> map = om.readValue(str, Map.class);
            Map<String, Object> resultMap = new HashMap<>(map.size());
            for (String key : map.keySet()) {
                Object val = map.get(key);
                if (map.get(key) instanceof String) {
                    resultMap.put(key, XssUtils.stripXSSAndSql(val.toString()));
                } else {
                    resultMap.put(key, val);
                }
            }
            str = om.writeValueAsString(resultMap);

            logger.info("RequestBody XSS After {}", str);
            final ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes());
            return new ServletInputStream() {
                @Override
                public int read() throws IOException {
                    return bais.read();
                }

                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                }
            };
        }

    }

    public String getRequestBody(ServletInputStream servletInputStream) {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(servletInputStream, Charset.forName("UTF-8")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (servletInputStream != null) {
                try {
                    servletInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    /**
     * 获取最原始的request
     *
     * @return
     */
    public HttpServletRequest getOrgRequest() {
        return orgRequest;
    }

    /**
     * 获取最原始的request的静态方法
     *
     * @return
     */
    public static HttpServletRequest getOrgRequest(HttpServletRequest req) {
        if (req instanceof XssHttpServletRequestWrapper) {
            return ((XssHttpServletRequestWrapper) req).getOrgRequest();
        }
        return req;
    }
}
