/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.mostinform.svgconverterservice;

/**
 *
 * @author Дмитрий
 */
import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Converter extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
        
        @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

            try {
                boolean noError = true;
                
                String[] viewBox = {"0","0","1200","800"};
                
                //TranscoderInput input_svg_image = new TranscoderInput( request.getInputStream());
                
                String parser = XMLResourceDescriptor.getXMLParserClassName();
                SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
        
       
        //URL url = new URL(getCodeBase(), "fileName.svg");
                Document doc = f.createDocument("svg",request.getInputStream());

                Element svg = doc.getDocumentElement();

                if (svg.hasAttribute("viewBox")) {
                    
                    Float x1;
                    Float y1;
                    Float x2;
                    Float y2;
                    
                    
                    viewBox = svg.getAttribute("viewBox").split(",");
                    
                    try{
                        x1 = new Float(viewBox[0]);
                        y1 = new Float(viewBox[1]);
                        x2 = new Float(viewBox[2]);
                        y2 = new Float(viewBox[3]);
                    }
                    catch (NumberFormatException ex){
                        x1=0f;
                        y1=0f;
                        x2=1200f;
                        y2=800f;
                    }
                    x2=x2+x1*2;
                    x1=0f;
                    y2=y2+y1*2;
                    y1=0f;
                    
                    
                    svg.setAttribute("viewBox", String.valueOf(x1)+","+ String.valueOf(y1)+","+ String.valueOf(x2)+","+ String.valueOf(y2));
                                        
                    viewBox = svg.getAttribute("viewBox").split(",");
                    
                    
                    NodeList pathList = svg.getElementsByTagName("path");
                    
                    Element pathContur = (Element)pathList.item(0);
                    
                    pathContur.setAttribute("stroke", "blue");
                    pathContur.setAttribute("fill", "white");
                    pathContur.setAttribute("stroke-width", "2");
                    
                    
                    svg.removeChild(pathList.item(1));
                    
                    for (int i=2; i<pathList.getLength();i++){
                        
                        Element detail = (Element)pathList.item(i);
                        
                        detail.setAttribute("stroke-width", "4");
                        
                    }
      
                    
                    
                     
              

                    //System.out.println( svg.getAttribute("viewBox"));
                     //
                  // notify the user somehow
                }
                


                TranscoderInput input_svg_image =   new TranscoderInput(doc); 
                
                //OutputStream jpg_ostream = new FileOutputStream("D:/bondtest-trace.jpg");
                OutputStream jpg_ostream = response.getOutputStream();
                TranscoderOutput output_jpg_image = new TranscoderOutput(jpg_ostream);
                // Step-3: Create JPEGTranscoder and define hints
                JPEGTranscoder my_converter = new JPEGTranscoder();
                my_converter.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,new Float(1));
                my_converter.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(viewBox[2]));
                my_converter.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(viewBox[3]));
                //my_converter.addTranscodingHint(JPEGTranscoder.KEY_MAX_WIDTH, new Float(1200));
                //my_converter.addTranscodingHint(JPEGTranscoder.KEY_BACKGROUND_COLOR, Color.white);
                //my_converter.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(600));
                //my_converter.addTranscodingHint(JPEGTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER, new Float(1f));
               //my_converter.addTranscodingHint(JPEGTranscoder.KEY_AOI, new Rectangle(0,0,10000,10000));
                // Step-4: Write output
                // Step-4: Write output
                my_converter.transcode(input_svg_image, output_jpg_image);
                
                
                jpg_ostream.flush();   
                jpg_ostream.close();
                
                doSetResult( response, 0 );
                
                //convert();	
            } catch (TranscoderException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
                response.sendError(500, "Error convert");
            }
		
		
	
	}
	
	protected void doSetResult( HttpServletResponse response, double result ) throws UnsupportedEncodingException, IOException {
		//String reply = "{\"error\":0,\"result\":" + Double.toString(result) + "}";
		//response.getOutputStream().write( reply.getBytes("UTF-8") );
		response.setContentType("application/json; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setStatus( HttpServletResponse.SC_OK );
	}		
	
	protected void doSetError( HttpServletResponse response ) throws UnsupportedEncodingException, IOException {
		String reply = "{\"error\":1}";
		response.getOutputStream().write( reply.getBytes("UTF-8") );
		response.setContentType("application/json; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setStatus( HttpServletResponse.SC_OK );
	}
        
        
        public void convert(){
            try {
                //Step -1: We read the input SVG document into Transcoder Input
                String svg_URI_input = new File("D:/bondtest-trace.svg").toURL().toString();
                TranscoderInput input_svg_image = new TranscoderInput(svg_URI_input);
                //Step-2: Define OutputStream to JPG file and attach to TranscoderOutput
                OutputStream jpg_ostream = new FileOutputStream("D:/bondtest-trace.jpg");
                TranscoderOutput output_jpg_image = new TranscoderOutput(jpg_ostream);
                // Step-3: Create JPEGTranscoder and define hints
                JPEGTranscoder my_converter = new JPEGTranscoder();
                my_converter.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,new Float(1));
                // Step-4: Write output
                my_converter.transcode(input_svg_image, output_jpg_image);
                // Step 5- close / flush Output Stream
                jpg_ostream.flush();   
                jpg_ostream.close();
            } catch (MalformedURLException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TranscoderException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
	

        
}