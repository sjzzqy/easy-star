package org.qzzy.configurer.xss;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Qzzy
 * @Generated: 2023/4/28 8:53
 */
public class XssUtils {
    public static final String REPLACE_STRING = "*";

    /**
     * 使用escapeHtml4
     * <p>
     * 会有弊端
     *
     * @param s
     * @return
     */
    public static String escapeHtml4(String s) {
        return StringEscapeUtils.escapeHtml4(s);
    }

    /**
     * xss校验
     *
     * @param s
     * @return
     */
    public static String xssEncode(String s) {
        if (StringUtils.isEmpty(s)) {
            return s;
        } else {
            s = stripXSSAndSql(s);
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '>':
                    sb.append("＞");// 转义大于号
                    break;
                case '<':
                    sb.append("＜");// 转义小于号
                    break;
                case '\'':
                    sb.append("＇");// 转义单引号
                    break;
                case '\"':
                    sb.append("＂");// 转义双引号
                    break;
                case '&':
                    sb.append("＆");// 转义&
                    break;
                case '#':
                    sb.append("＃");// 转义#
                    break;
                default:
                    sb.append(c);
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 正则删除法，效率不高
     *
     * @param str
     * @return
     */
    public static String stripXSSAndSql(String str) {
        if (StringUtils.isNotBlank(str)) {
            List<Object[]> ret = new ArrayList<Object[]>();
            ret.add(new Object[]{"<(no)?script[^>]*>.*?</(no)?script>", Pattern.CASE_INSENSITIVE});
            ret.add(new Object[]{"</script>", Pattern.CASE_INSENSITIVE});
            ret.add(new Object[]{"<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
            ret.add(new Object[]{"eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
            ret.add(new Object[]{"expression\\((.*?)\\)",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
            ret.add(new Object[]{"(javascript:|vbscript:|view-source:)*", Pattern.CASE_INSENSITIVE});
            ret.add(new Object[]{"<(\"[^\"]*\"|\'[^\']*\'|[^\'\">])*>",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
            ret.add(new Object[]{
                    "(window\\.location|window\\.|\\.location|document\\.cookie|document\\.|alert\\(.*?\\)|window\\.open\\()*",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});
            ret.add(new Object[]{
                    "<+\\s*\\w*\\s*(oncontrolselect|oncopy|oncut|ondataavailable|ondatasetchanged|ondatasetcomplete|ondblclick|ondeactivate|ondrag|ondragend|ondragenter|ondragleave|ondragover|ondragstart|ondrop|onerror=|onerroupdate|onfilterchange|onfinish|onfocus|onfocusin|onfocusout|onhelp|onkeydown|onkeypress|onkeyup|onlayoutcomplete|onload|onlosecapture|onmousedown|onmouseenter|onmouseleave|onmousemove|onmousout|onmouseover|onmouseup|onmousewheel|onmove|onmoveend|onmovestart|onabort|onactivate|onafterprint|onafterupdate|onbefore|onbeforeactivate|onbeforecopy|onbeforecut|onbeforedeactivate|onbeforeeditocus|onbeforepaste|onbeforeprint|onbeforeunload|onbeforeupdate|onblur|onbounce|oncellchange|onchange|onclick|oncontextmenu|onpaste|onpropertychange|onreadystatechange|onreset|onresize|onresizend|onresizestart|onrowenter|onrowexit|onrowsdelete|onrowsinserted|onscroll|onselect|onselectionchange|onselectstart|onstart|onstop|onsubmit|onunload)+\\s*=+",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL});

            for (Object[] a : ret) {
                Pattern compile = Pattern.compile(String.valueOf(a[0]), Integer.parseInt(String.valueOf(a[1])));
                str = compile.matcher(str).replaceAll("");
            }
        }
        return str;
    }
}
