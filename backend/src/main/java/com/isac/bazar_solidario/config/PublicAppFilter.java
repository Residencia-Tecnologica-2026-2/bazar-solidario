package com.isac.bazar_solidario.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;

/** O contêiner único recebe SPA e API. A autenticação da API continua no SecurityFilterChain. */
@Component @Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class PublicAppFilter implements Filter {
    @Override public void doFilter(ServletRequest req,ServletResponse res,FilterChain chain) throws IOException,ServletException {
        var request=(HttpServletRequest)req;
        var response=(HttpServletResponse)res;
        String path=request.getRequestURI();
        if(path.startsWith("/api/")) {
            chain.doFilter(new HttpServletRequestWrapper(request) {
                @Override public String getRequestURI() {return path.substring(4);}
                @Override public String getServletPath() {return super.getServletPath().startsWith("/api/")?super.getServletPath().substring(4):super.getServletPath();}
                @Override public StringBuffer getRequestURL() {
                    String original=super.getRequestURL().toString();
                    return new StringBuffer(original.substring(0,original.length()-path.length())+path.substring(4));
                }
            },response);
            return;
        }
        // Somente navegação HTML recebe o fallback. Um erro de API nunca vira HTML HTTP 200.
        var index=new ClassPathResource("static/index.html");
        if("GET".equals(request.getMethod()) && request.getHeader("Accept")!=null
                && request.getHeader("Accept").contains("text/html") && !path.contains(".") && index.exists()) {
            response.setContentType("text/html;charset=UTF-8");
            response.setHeader("Cache-Control","no-cache");
            response.setHeader("X-Content-Type-Options","nosniff");
            try(var stream=index.getInputStream()) {stream.transferTo(response.getOutputStream());}
            return;
        }
        chain.doFilter(request,response);
    }
}
