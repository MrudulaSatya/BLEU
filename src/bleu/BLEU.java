/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bleu;

/**
 *
 * @author MRUDULA
 */


import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.sql.ResultSet;

public class BLEU {

    /**
     * @param args the command line arguments
     */
    
      
    public static void main(String[] args) {
        // TODO code application logic here
             
            //Database for reading the synonyms
            DBConn conn = new DBConn();
            conn.getConnection();
            conn.stmtConnection();
            String synonym_query;
            
            //xml file reading for reference and candidate xml files
            readXML xml = new readXML();
            xml.readXMLfile();
            NodeList candList, refList;
            candList = xml.getCandList();
            refList = xml.getRefList();
            
            String[] splitC;
            String[] splitR1;
            String[] splitR2;
            String[] splitR3;
        
        
            String tempC ;
            String tempC1 ;
            String tempC2 ;
            String tempC3 ;
        
        
            int p1=0, p2=0, p3=0, p4=0;
            
            double BP;
            double BLEU;
            double avgLengthOfReferences,noOfwordsIncandi;
            int flagR3,flagR2;
            int flagp2R3,flagp2R2;
            int flagp3R3 ,flagp3R2;
            int ifBreak = 0;
         
	
        // loop that reads the xml one by one  
	for (int tempcand = 0; tempcand < candList.getLength(); tempcand ++) { 

		Node candNode = candList.item(tempcand);
                Node refNode = refList.item(tempcand);

		if (candNode.getNodeType() == Node.ELEMENT_NODE && refNode.getNodeType() == Node.ELEMENT_NODE) {

			Element candElement = (Element) candNode;
                        Element refElement = (Element) refNode;

			String cand;
                        cand = candElement.getElementsByTagName("DATA").item(0).getTextContent();
                        
                        String R1; 
                        R1 = refElement.getElementsByTagName("Ref1").item(0).getTextContent();
                        
                       
                         //Display values
                        System.out.println("cand: " + cand);
                        System.out.println("ref1: " + R1);
                        
                        
         
                        // splitting the candidate and reference sentences into individual words
                        String temp = cand.replaceAll("[\\n]"," ");
                        splitC = temp.replaceAll("[.,!?:;'।]","").split(" ");
         
                        temp = R1.replaceAll("[\\n]"," ");
                        splitR1 = temp.replaceAll("[.,!?:;'।]","").split(" ");
         
                       
                       
                        //calculating c for the current candidate sentence
                        noOfwordsIncandi = splitC.length;
                        avgLengthOfReferences = splitR1.length ;

                        //calculating p1
                        
                        //loop that traverses through each word of the candidate 
                        //one by one that is 1 gram precision
                        for(int i=0;i<splitC.length;i++){
                            
                             flagR3 = 2;
                             flagR2 = 2;
                             ifBreak = 0;
                             
                             //ith word stored in tempC 
                             tempC = splitC[i];
                             
                             //loop traverses R1 word by word
                             for(int j=0;j<splitR1.length;j++){
                                 
                                 //indication used later to 
                                 //see whether the word needs to be checked in R2
                                 flagR2=0;
                                  
                                 if(tempC.equalsIgnoreCase(splitR1[j])){
                                      p1++; //word found in R1
                                      splitR1[j]="";//empty the word in R1 to avoid matching it again
                                      flagR2=1;//no need to check in R2 for this word
                                      break;//comes out of the j loop
                                 } 
                                 //checking whther a synonym of the word exists or not
                                 else{
                                      ifBreak = 0; 
                                      synonym_query = "SELECT syn from hindi_syn where word = \"" + splitR1[j]+"\"";
                                      try(ResultSet rs1 = conn.runQuery(synonym_query)){
                                         while(rs1.next()){
                                             
                                             if(tempC.equalsIgnoreCase(rs1.getString("syn"))){
                                                 p1++; //synonym found!
                                                 splitR1[j]="";
                                                 flagR2=1;
                                                 ifBreak = 1;//indicates synonym found
                                                 break; // no need to check next synonym in database
                                             }
                                         }//while ends
                                      } catch(Exception e){}
                                      
                                     if(ifBreak == 1){ // checking to see whether a synonym was found
                                         break;//as synonym is found break from the R1 loop
                                     }
                                 }//else ends
                                 
                             } //for for R1 ends 
                             
                            
                        } // ENDS - for loop traversing the candidate array for 1 gram precision
                        
                        //print the final calculated p1
                        double unigram = p1/noOfwordsIncandi;
                        System.out.print("\n p1 = " + unigram  + "\n");
       
                        //splitting for p2
                        temp = R1.replaceAll("[\\n]"," ");
                        splitR1 = temp.replaceAll("[.,!?:;']","").split(" ");
         
                      
                         //Calculating p2
                         
                         //loop checking two consecutive words at a time i.e 2-gram precison
                         for(int h=1,l=0;h<splitC.length;l++,h++){
                                if(l<splitC.length){   
           
                                    flagp2R3 = 2;
                                    flagp2R2 = 2;
                                    
                                    //storing the 2 consecutive words
                                    tempC = splitC[l]; //the first word in sequence
                                    tempC1 = splitC[h]; //the second word in sequence
                                    
                                    //loop checking for the pair in R1
                                    for(int k=1,n=0;k<splitR1.length;n++,k++){
                                            if(n<splitR1.length){
                                                
                                                    flagp2R2 =0;//indication used later to check for the pair in R2
                                                    
                                                    if(tempC.equalsIgnoreCase(splitR1[n]) && tempC1.equalsIgnoreCase(splitR1[k])){
                                                            p2++; //pair found
                                                            splitR1[n]="";
                                                            flagp2R2=1;//no need to check in R2 for the pair
                                                            break;
                                                     }
                                            }
                                    } //R1 loop ends
                                    
                                  
                                } // ENDS if condition checking whether the current wprd selection lies inside the candidate array
           
                         } // ENDS - for loopp travesing for 2 gram precison
                         
                        //print p2 out
                         double bigram =  p2/(noOfwordsIncandi - 1);
                        System.out.print("\n p2 = " + bigram + "\n");
       
                         //splitting for p3
                         temp = R1.replaceAll("[\\n]"," ");
                         splitR1 = temp.replaceAll("[.,!?:;']","").split(" ");
         
                      
       
                        //calculating p3
                        
                         //checking three words at a time i.e 3 gram precison
                        for(int a=2,b=1,c=0;a<splitC.length;b++,a++,c++){
                                if(b<splitC.length && c<splitC.length){   
           
                                        flagp2R3 = 2;
                                        flagp2R2 = 2;
                                        
                                        tempC = splitC[c];//first word in sequence
                                        tempC1 = splitC[b];//second word in sequence
                                        tempC2 = splitC[a];//third word in sequence
                                        
                                        //loop checking in R1
                                        for(int k=2,l=1,m=0;k<splitR1.length;l++,k++,m++){
                                                if(l<splitR1.length && m<splitR1.length){
                                                            flagp2R2 =0;
                                                            if(tempC.equalsIgnoreCase(splitR1[m]) && tempC1.equalsIgnoreCase(splitR1[l]) && tempC2.equalsIgnoreCase(splitR1[k])){
                                                                         p3++;//found the triplet
                                                                         splitR1[m]="";
                                                                         flagp2R2=1;
                                                                         break;
                           
                                                            }
                                                }
                                        }//R1 checking for p3 ends
                                        
                                       
                                       
                                }
                         }//ENDS - 3 gram precison loop   
                        
                        //print p3 value out 
                        double trigram = p3/(noOfwordsIncandi-2);
                        System.out.print("\n p3 = " + trigram + "\n");
       
                         //splitting for p4
                         temp = R1.replaceAll("[\\n]"," ");
                         splitR1 = temp.replaceAll("[.,!?:;']","").split(" ");
         
                     
                        //calculating p4
                         
                        //loop to check the 4 gram precison 
                        for(int d=3,e=2,f=1,g=0;d<splitC.length;d++,e++,f++,g++){
                                if(f<splitC.length && e<splitC.length && g<splitC.length){   
           
                                                flagp3R3 = 2;
                                                flagp3R2 = 2;
                                                
                                                tempC = splitC[g];//1st word in sequence
                                                tempC1 = splitC[f];//2nd word in sequence
                                                tempC2 = splitC[e];//3rd word in sequence
                                                tempC3 = splitC[d];//4th word in sequence
                                                
                                                //loop checking for 4 gram precision in R1
                                                for(int k=3,l=2,m=1,n=0;k<splitR1.length;l++,k++,m++,n++){
                                                        if(l<splitR1.length && m<splitR1.length && n<splitR1.length){
                                                                    flagp3R2 =0;
                                                                    if(tempC.equalsIgnoreCase(splitR1[n]) && tempC1.equalsIgnoreCase(splitR1[m]) && tempC2.equalsIgnoreCase(splitR1[l]) && tempC3.equalsIgnoreCase(splitR1[k])){
                                                                                p4++;//quadruple found
                                                                                splitR1[n]="";//removing first word in sequence as it wont be matched again
                                                                                flagp3R2=1;
                                                                                break;
                           
                                                                    }
                                                        }
                                                }//loop ends for checking in R1
                                                
                                                
                                }
                        } // ENDS - loop for 4 gram precision
                        
                        //print p4 value out
                        double quadgram = p4/(noOfwordsIncandi-3);
                        System.out.print("\n p4 = " + quadgram + "\n");
                        System.out.println("r = "+avgLengthOfReferences);
                        System.out.println("c = "+noOfwordsIncandi);
                        // Calculating BP value 
                        if(noOfwordsIncandi > avgLengthOfReferences){
                                BP = 1;
                        }
                        else{
                                double x = avgLengthOfReferences/noOfwordsIncandi;
                                double y = 1 - x;
                                BP = Math.pow(2.71828,y);
                        }
                         System.out.println("BP = "+BP);
                        //Calculating BLEU
                        if(bigram==0){
                            BLEU = BP * Math.pow(2.71828,(Math.log(unigram))/4);
                        }
                        
                        else if(trigram==0){
                            BLEU = BP * Math.pow( 2.71828,((Math.log(unigram))/4) + ((Math.log(bigram))/4));
                        }
                        
                        else if(quadgram==0){
                            BLEU = BP * Math.pow(2.71828, ((Math.log(unigram))/4) + ((Math.log(bigram))/4) + ((Math.log(trigram))/4));
                        }
                        
                        else {
                            BLEU = BP * Math.pow(2.71828, ((Math.log(unigram))/4) + ((Math.log(bigram))/4) + ((Math.log(trigram))/4) + ((Math.log(quadgram))/4));
                        }
                        System.out.println("BLEU score:" + BLEU);
                                   
        } //if loop checking node type ends
                
               //resetting all values for next set of reference and candidate 
               p1 = 0; p2 =0; p3 = 0; p4=0; BLEU =0 ; BP = 0; 
      }// for loop reading the xml file ends
        
  }//main ends
        
}//class ends
    
