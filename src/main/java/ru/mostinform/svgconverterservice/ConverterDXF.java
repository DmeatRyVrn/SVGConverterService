/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.mostinform.svgconverterservice;

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.servlet.ServletException;
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
import org.w3c.dom.NodeList;
import org.kabeja.parser.ParseException;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.parser.DXFParser;
import org.kabeja.svg.SVGGenerator;
import org.kabeja.xml.SAXGenerator;
import org.kabeja.xml.SAXPrettyOutputter;
import org.kabeja.xml.SAXSerializer;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Дмитрий
 */
public class ConverterDXF extends HttpServlet{
    
                        
    
        @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
            

            try {
                
                //Установим маштабирование
                int scale = 20;
                
                Float imageWidth = 23.870f;
                
                Float imageHeight = 5.070f;
                
                Float ratio = imageHeight/imageWidth;

                List<Detail> detailList = new ArrayList();
                
                Metal metal = new Metal();
                
                org.kabeja.parser.Parser dxfParser = org.kabeja.parser.ParserBuilder.createDefaultParser();
                
                String file = request.getParameter("file");
                
                Boolean onlyDetails = Boolean.valueOf(request.getParameter("onlydetails"));
                
                Boolean url = Boolean.valueOf(request.getParameter("url"));
                
                Boolean onlyMetal = Boolean.valueOf(request.getParameter("onlymetal"));
                
                
                
                //Если передали в параметре file путь к файлу, то берем из файла, иначе из тела запроса
                try{
                    if(file != null && !file.isEmpty()){
                        if (url){
                            
                            dxfParser.parse(new BufferedInputStream(new URL(file).openStream()), DXFParser.DEFAULT_ENCODING);
                            
                        }
                        else{
                            if (new File(file).exists()) {
                                dxfParser.parse(file);
                            } else{

                                throw new ParseException("Empty file"); 
                            }
                        }

                    }else{
                        dxfParser.parse(request.getInputStream(), DXFParser.DEFAULT_ENCODING);
                    }
                    
                }catch(Exception ex) {
                    throw new ParseException("Empty file"); 
                    //throw new FileNotFoundException("");
                }   
                

                
                DXFDocument docDXF = dxfParser.getDocument();
                
                if (docDXF == null) throw new ParseException("Empty file");

                //Берем первый слой
                DXFLayer layer =  docDXF.getDXFLayer("0");
                
                if (layer.getDXFEntities("LINE") == null) throw new ParseException("Empty file");

                
                List<DXFEntity> items = new ArrayList();
                
                //Уберем элементы на слое MARK, это маркировка она нам не нужна
                for (Object item :layer.getDXFEntities("LINE")){
                    DXFEntity entity = (DXFEntity)item;
                    if (entity.getLayerName().equals("MARK")){

                        items.add(entity);

                    }
                }
                
                items.forEach(item->layer.removeDXFEntity(item));
                

                        
                
                //Делаем SVG из DXF
                SAXGenerator generator = new SVGGenerator();
                
                
                SAXSerializer out = new SAXPrettyOutputter();
                
                //generator.setProperties(new HashMap());
                
               
                
                String[] viewBox = {"0","0","1200","800"};
                
                //TranscoderInput input_svg_image = new TranscoderInput( request.getInputStream());
                
                String parser = XMLResourceDescriptor.getXMLParserClassName();
                SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
                
                
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                
                out.setOutput(os);
                
                
                
                generator.generate(docDXF, out,new HashMap());
                 
                InputStream is = new ByteArrayInputStream(os.toByteArray());
        
                
                Document doc = f.createDocument("svg",is);

                Element svg = doc.getDocumentElement();

                if (svg.hasAttribute("viewBox")) {
                    
                    Float x1;
                    Float y1;
                    Float x2;
                    Float y2;

                    NodeList lineList = svg.getElementsByTagName("line");

                    //Найдем границы
                    Element line;
                    x1=0f;
                    y1=0f;
                    x2=1200f;
                    y2=800f;
                    
                    
                    for (int i = 0; i<lineList.getLength(); i++){
                        
                        line = (Element)lineList.item(i);
                        
                        if (line.hasAttribute("stroke")){
                            
                            line =  (Element)lineList.item(i+6);
                            
                            x2=new Float(line.getAttribute("x1"));
                            y1=new Float(line.getAttribute("y2"));
                            y2=new Float(line.getAttribute("y1"))-y1;
                            
                            break;
                            
                        }
                        
                    }
                    
                    y1=y1*-1f;
                    y2=y2/100*101*-1f;
                    x2=x2/100*101;
                    
                    
                    svg.removeAttribute("viewBox");
                    
                    svg.setAttribute("viewBox", String.valueOf(x1)+","+ String.valueOf(y1)+","+ String.valueOf(x2)+","+ String.valueOf(y2));
                    viewBox = svg.getAttribute("viewBox").split(",");
                    
                    NodeList textList = svg.getElementsByTagName("tspan");

                    
                    
                    for (int i=0; i<textList.getLength();i++){
                        
                        Element text = (Element)textList.item(i);
                        
                        if (text.getFirstChild().getNodeValue().contains("=")){
                            
                            Detail detail = new Detail();
                            
                            String detailStr = text.getFirstChild().getNodeValue();
                            
                            int strIndex = 0;
                            
                            String value;
                            
                            value = detailStr.substring(strIndex, detailStr.indexOf("="));
                            
                            strIndex=strIndex+value.length()+1;
                            
                            detail.setPosition(Integer.parseInt(value));
                            
                            value = detailStr.substring(strIndex, detailStr.indexOf("\t",strIndex));
                            
                            strIndex=strIndex+value.length()+1;
                            
                            detail.setArticul(value);
                            
                            value = detailStr.substring(strIndex, detailStr.indexOf("\t",strIndex));
                            
                            strIndex=strIndex+value.length()+1;
                            
                            detail.setQueue(Integer.parseInt(value));
                            
                            value = detailStr.substring(strIndex, detailStr.indexOf("\t",strIndex));
                            
                            detail.setId(Integer.parseInt(value));
                            
                            detailList.add(detail);

                        }
                        else if (text.getFirstChild().getNodeValue().contains("@")){
                            
                            //Metal metal = new Metal();
                            
                            String meatalStr = text.getFirstChild().getNodeValue();
                            
                            int strIndex = 0;
                            
                            String value;
                            
                            value = meatalStr.substring(strIndex, meatalStr.indexOf("@"));
                            
                            strIndex=strIndex+value.length()+1;
                            
                            metal.setMaterial(value);
                            
                            value = meatalStr.substring(strIndex, meatalStr.indexOf("@",strIndex));
                            
                            strIndex=strIndex+value.length()+1;
                            
                            metal.setThickness(value);
                            
                            value = meatalStr.substring(strIndex, meatalStr.indexOf("@",strIndex));
                            
                            strIndex=strIndex+value.length()+1;
                            
                            metal.setHeight(value);
                            
                            value = meatalStr.substring(strIndex, meatalStr.indexOf("@",strIndex));
                            
                            strIndex=strIndex+value.length()+1;
                            
                            metal.setWidth(value);
                            
                            value = meatalStr.substring(strIndex, meatalStr.indexOf("@",strIndex));
                            
                            strIndex=strIndex+value.length()+1;
                            
                            metal.setWeight(value);
                            
                            value = meatalStr.substring(strIndex, meatalStr.indexOf("@",strIndex));
                            
                            strIndex=strIndex+value.length()+1;
                            
                            value = value.substring(2);
                            
                            metal.setID(Integer.valueOf(value));
                            
                            value = meatalStr.substring(strIndex);
                            
                            metal.setAct(value);
                        }
                        
                        //Для печати раскроя уберем текст
                        text.setAttribute("visibility", "hidden");

                    }
      

                }
                
                if (onlyDetails) {
                    
                    for (Detail item: detailList){
                        response.getOutputStream().write((new Gson().toJson(item)).getBytes("UTF-8"));
                    }
                    
                    //detailList.forEach(item->response.getOutputStream().write(new Gson().toJson(item)));
                    
                    //response.getOutputStream().write(b);
                       
                }else if(onlyMetal){
                    response.getOutputStream().write((new Gson().toJson(metal)).getBytes("UTF-8"));
                }
                else{

                    //Делаем JPG из SVG
                    TranscoderInput input_svg_image =   new TranscoderInput(doc); 

                    //OutputStream jpg_ostream = new FileOutputStream("D:/bondtest-trace.jpg");
                    OutputStream jpg_ostream = response.getOutputStream();
                    TranscoderOutput output_jpg_image = new TranscoderOutput(jpg_ostream);
                    // Step-3: Create JPEGTranscoder and define hints
                    JPEGTranscoder my_converter = new JPEGTranscoder();
                    my_converter.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,new Float(1));
                    
                    if ((new Float(viewBox[3])/new Float(viewBox[2]))<ratio){
                        
                        my_converter.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(viewBox[2])*scale);
                        my_converter.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(viewBox[2])*ratio*scale);
                        
                    }
                    else{
                        my_converter.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(viewBox[3])/ratio*scale);
                        my_converter.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(viewBox[3])*scale);
                    }    

                    my_converter.transcode(input_svg_image, output_jpg_image);


                    jpg_ostream.flush();   
                    jpg_ostream.close();

                    doSetResult( response, 0 );
                }
                
                //convert();	
            } catch (TranscoderException ex) {
                //Logger.getLogger(Converter.class.getName()).log(Level.SEVERE, null, ex);
                   // true forces append mode
                String logMessage;   
                
                logMessage = request.getQueryString();
                
                logErrorToFile("Errorlog.txt",ex,logMessage);
                
                logMessage = ex.getMessage()+"\nquery:"+request.getQueryString();

                response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
                response.getOutputStream().write(logMessage.getBytes("UTF-8"));
                //response.sendError(500, "Error convert");
                
            } catch (ParseException | SAXException | IOException ex ) {
                String logMessage;   
                
                logMessage = request.getQueryString();
                
                logErrorToFile("Errorlog.txt",ex,logMessage);
                
                logMessage = ex.getMessage()+"\nquery:"+request.getQueryString();

                response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
                response.getOutputStream().write(logMessage.getBytes("UTF-8"));
            }
            //response.sendError(500, "Error convert: "+ex.getMessage());
            //Logger.getLogger(ConverterDXF.class.getName()).log(Level.SEVERE, null, ex);
            //response.sendError(500, "Error convert: "+ex.getMessage());
            
		
		
	
	}
        
	protected void doSetResult( HttpServletResponse response, double result ) throws UnsupportedEncodingException, IOException {
		//String reply = "{\"error\":0,\"result\":" + Double.toString(result) + "}";
		//response.getOutputStream().write( reply.getBytes("UTF-8") );
		response.setContentType("application/json; charset=UTF-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setStatus( HttpServletResponse.SC_OK );
	}        

        
        private void logErrorToFile(String fileName, Exception ex, String message) throws IOException{
            
            FileHandler fh = new FileHandler(fileName, true);   // true forces append mode
            SimpleFormatter sf = new SimpleFormatter(); 
            fh.setFormatter(sf);
            Logger log = Logger.getLogger(ConverterDXF.class.getName());
            log.setUseParentHandlers(false);
            log.addHandler(fh);
            log.log(Level.SEVERE, message);
            log.log(Level.SEVERE, null, ex);
            
            fh.close();
            //log.addHandler(fh);

            
        }
    
}
