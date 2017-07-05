package com.manh.rest.http2;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.coyote.http11.Http11AprProtocol;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.coyote.http2.Http2Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        ApplicationContext ctx = SpringApplication.run(DemoApplication.class, args);
    }


    @Bean
    public EmbeddedServletContainerFactory servletContainer() throws Exception {
        TomcatEmbeddedServletContainerFactory container = new TomcatEmbeddedServletContainerFactory();
        AprLifecycleListener aprLifecycle = new AprLifecycleListener();
        aprLifecycle.setSSLEngine("on");
        aprLifecycle.setUseOpenSSL(true);
        aprLifecycle.setUseAprConnector(true);
        container.addContextLifecycleListeners(aprLifecycle);
        container.addAdditionalTomcatConnectors(createHttp2AprConnector());

        container.addAdditionalTomcatConnectors(createNIOConnector());
        container.addAdditionalTomcatConnectors(createHttp1AprConnector());

        return container;
    }

    private Connector createHttp2AprConnector() throws Exception {
        Connector connector = new Connector("org.apache.coyote.http11.Http11AprProtocol");
        Http11AprProtocol protocol = (Http11AprProtocol) connector.getProtocolHandler();

        connector.setScheme("https");
        connector.setSecure(true);
        connector.setPort(9900);
        protocol.setSSLEnabled(true);
        protocol.setSSLProtocol("TLSv1.2");

        //Support HTTP/2
        connector.addUpgradeProtocol(new Http2Protocol());

        final String absoluteKeystoreFile = new ClassPathResource("keystore.p12").getFile().getAbsolutePath();
        System.out.println("##### keystoreFile:" + absoluteKeystoreFile);
        protocol.setKeystoreFile(absoluteKeystoreFile);
        protocol.setKeystorePass("tomcat");
        protocol.setKeystoreType("PKCS12");
        protocol.setKeyAlias("tomcat");
        protocol.setSSLCertificateFile(new ClassPathResource("cert.pem").getFile().getAbsolutePath());
        protocol.setSSLCertificateKeyFile(new ClassPathResource("key.pem").getFile().getAbsolutePath());
        protocol.setSSLPassword("tomcat");
        protocol.setSSLVerifyClient("optional");
        protocol.setClientAuth("false");

        return connector;

    }

    private Connector createHttp1AprConnector() throws Exception {
        Connector connector = new Connector("org.apache.coyote.http11.Http11AprProtocol");
        Http11AprProtocol protocol = (Http11AprProtocol) connector.getProtocolHandler();

        connector.setScheme("https");
        connector.setSecure(true);
        connector.setPort(9920);
        protocol.setSSLEnabled(true);
        protocol.setSSLProtocol("TLSv1.2");

        final String absoluteKeystoreFile = new ClassPathResource("keystore.p12").getFile().getAbsolutePath();
        System.out.println("##### keystoreFile:" + absoluteKeystoreFile);
        protocol.setKeystoreFile(absoluteKeystoreFile);
        protocol.setKeystorePass("tomcat");
        protocol.setKeystoreType("PKCS12");
        protocol.setKeyAlias("tomcat");
        protocol.setSSLCertificateFile(new ClassPathResource("cert.pem").getFile().getAbsolutePath());
        protocol.setSSLCertificateKeyFile(new ClassPathResource("key.pem").getFile().getAbsolutePath());
        protocol.setSSLPassword("tomcat");
        protocol.setSSLVerifyClient("optional");
        protocol.setClientAuth("false");

        return connector;

    }

    private Connector createNIOConnector() throws Exception {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
        connector.setScheme("https");
        connector.setSecure(true);
        connector.setPort(9910);
        protocol.setSSLEnabled(true);
        protocol.setKeystoreFile(new ClassPathResource("keystore.p12").getFile().getAbsolutePath());
        protocol.setKeystoreType("PKCS12");
        protocol.setKeystorePass("tomcat");
        protocol.setKeyAlias("tomcat");
        protocol.setClientAuth("false");
        protocol.setSSLProtocol("TLSv1.2");

        return connector;
    }
}
