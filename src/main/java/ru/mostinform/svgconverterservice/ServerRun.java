/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.mostinform.svgconverterservice;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
/**
 *
 * @author Дмитрий
 */
public class ServerRun {
    
    	public static void main(String[] args) {
            
                String version = "1.2.0";

                int port = 7799;

		Server server = new Server(port);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
        
		
		context.addServlet(new ServletHolder( new Converter( ) ),"/convert");
                context.addServlet(new ServletHolder( new ConverterDXF( ) ),"/convertdxf");

		HandlerList handlers = new HandlerList();
		handlers.setHandlers(new Handler[] { context });
		server.setHandler(handlers);   		

		try {
			server.start();
                        System.out.println("Version : "+version);
			System.out.println("Listening port : " + port );
	        
			server.join();
		} catch (Exception e) {
			System.out.println("Error.");
			e.printStackTrace();
		}

	}
    
}
