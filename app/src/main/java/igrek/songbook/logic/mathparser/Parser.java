package igrek.todotree.mathparser;

import java.util.Vector;

public class Parser {
    static boolean is_formula(String formula){
        if(formula.length()==0) return false;
        if(formula.charAt(0) == '-') return true;
        return formula.charAt(0)>='0' && formula.charAt(0)<= '9';
    }
    
    static boolean is_number(char c){
        if(c=='.') return true;
        if(c>='0' && c<= '9') return true;
        return false;
    }
    static boolean is_operator(char c){
        if(c=='+') return true;
        if(c=='-') return true;
        if(c=='*') return true;
        if(c=='/') return true;
        return false;
    }
    static int segment_type(char c){
        if(is_number(c)) return SegmentType.NUMBER;
        if(is_operator(c)) return SegmentType.OPERATOR;
        return 0;
    }
    static int segment_type(String s){
        if(s.length()==0) return 0;
        return segment_type(s.charAt(0)); //typ na podstawie pierwszego znaku
    }
    
    //oblicza jedno podstawowe działanie
    static double calculate_operator(String argument_l, String argument_r, String operator) throws Exception{
        //parsowanie na double
        double arg_l = Double.parseDouble(argument_l);
        double arg_r = Double.parseDouble(argument_r);
        if(operator.equals("+")) return arg_l + arg_r;
        if(operator.equals("-")) return arg_l - arg_r;
        if(operator.equals("*")) return arg_l * arg_r;
        if(operator.equals("/")){
            if(arg_r == 0){
                throw new Exception("Błąd obliczeń: dzielenie przez 0");
            }
            return arg_l / arg_r;
        }
        throw new Exception("Błąd obliczeń: nieprawidłowy operator");
    }
    
    //formatowanie formuły
    static String formula_format(String formula){
        for(int i=0; i<formula.length(); i++){
            if(formula.charAt(i) == ','){ //zamiana przecinka na kropkę
                formula = formula.substring(0, i) + "." + formula.substring(i+1);
            }
            if(formula.charAt(i) == ' '){ //usunięcie spacji
                formula = formula.substring(0, i) + formula.substring(i+1);
                i--;
            }
        }
        return formula;
    }
    
    //podział ciągu znaków na segmenty
    static Vector segmentuj(String formula) throws Exception {
        Vector segmenty = new Vector();
        String segment = "";
        for(int i=0; i<formula.length(); i++){
            char znak = formula.charAt(i);
            //jeśli znak nie jest ani liczbą ani operatorem
            if(Parser.segment_type(znak) == 0){
                throw new Exception("Błąd: nieprawidłowy znak: "+znak);
            }
            if(segment.length() == 0){ //pusty segment
                segment += znak;
                continue;
            }
            //jeśli nowy znak (liczba) należy do tej samej grupy (liczby)
            if(Parser.segment_type(znak) == SegmentType.NUMBER && Parser.segment_type(segment) == SegmentType.NUMBER){
                //dopisz znak do segmentu
                segment += znak;
            }else{
                //zakończ segment, dodaj go do listy
                segmenty.addElement(new ParserSegment(segment,Parser.segment_type(segment)));
                //znak jest teraz początkiem nowego segmentu
                segment = "" + znak;
            }
        }
        //dodanie ostatniego segmentu
        if(segment.length() > 0){
            segmenty.addElement(new ParserSegment(segment,Parser.segment_type(segment)));
        }
        //jeśli pierwsze segmenty to liczba z minusem
        if(segmenty.size()>1){
            ParserSegment segment_pierwszy = (ParserSegment)(segmenty.firstElement());
            if(segment_pierwszy.type == SegmentType.OPERATOR && segment_pierwszy.text.equals("-")){
                ParserSegment segment_drugi = (ParserSegment)(segmenty.elementAt(1));
                if(segment_drugi.type == SegmentType.NUMBER){ //drugi segment - liczba
                    segment_drugi.text = "-" + segment_drugi.text; //przepisanie minusa do liczby
                    segmenty.removeElementAt(0);
                }
            }
        }
        return segmenty;
    }
    
    //sprawdzenie poprawności segmentów
    static void segments_verify(Vector segmenty) throws Exception {
        String segment;
        for(int i=0; i<segmenty.size(); i++){
            ParserSegment segment2 = (ParserSegment)(segmenty.elementAt(i));
            segment = segment2.text;
            if(segment2.type==SegmentType.NUMBER){ //liczba
                //brak kropki na końcu i na początku
                if(segment.charAt(0) == '.'){
                    throw new Exception("Błąd: kropka na początku liczby: "+segment);
                }
                if(segment.charAt(segment.length()-1) == '.'){
                    throw new Exception("Błąd: kropka na końcu liczby: "+segment);
                }
                //maksymalnie jedna kropka
                if(segment.indexOf('.')!=segment.lastIndexOf('.')){
                    throw new Exception("Błąd: nieprawidłowa liczba: "+segment);
                }
            }
            //jeśli poprzedni też był operatorem
            if(segment2.type == SegmentType.OPERATOR && i>0){
                ParserSegment poprzedni = (ParserSegment)(segmenty.elementAt(i-1));
                if(poprzedni.type == SegmentType.OPERATOR){
                    throw new Exception("Błąd: operator w nieprawidłowym miejscu: "+segment);
                }
            }
        }
        //ostatni segment jest operatorem
        ParserSegment segment_ostatni = (ParserSegment)(segmenty.lastElement());
        if(segment_ostatni.type == SegmentType.OPERATOR){
            throw new Exception("Błąd składni: operator na końcu");
        }
        //pierwszy segment jest operatorem
        ParserSegment segment_pierwszy = (ParserSegment)(segmenty.firstElement());
        if(segment_pierwszy.type == SegmentType.OPERATOR){
            throw new Exception("Błąd składni: operator na początku: "+segment_pierwszy.text);
        }
    }
    
    //obliczanie wyniku z segmentów
    static String segments_calculate(Vector segmenty) throws Exception {
        while(segmenty.size() > 1){
            //znajdź operator o najwyższym priorytecie
            int operator_max = -1;
            //szukaj * lub /
            for(int i=0; i<segmenty.size(); i++){
                ParserSegment segment2 = (ParserSegment)(segmenty.elementAt(i));
                if(segment2.type == SegmentType.OPERATOR){
                    if(segment2.text.equals("*") || segment2.text.equals("/")){
                        operator_max = i;
                        break;
                    }
                }
            }
            if(operator_max == -1){ //jeśli nic nie znaleziono
                //szukaj + lub -
                for(int i=0; i<segmenty.size(); i++){
                    ParserSegment segment2 = (ParserSegment)(segmenty.elementAt(i));
                    if(segment2.type == SegmentType.OPERATOR){
                        if(segment2.text.equals("+") || segment2.text.equals("-")){
                            operator_max = i;
                            break;
                        }
                    }
                }
            }
            if(operator_max == -1){ //jeśli nic nie znaleziono
                throw new Exception("Błąd składni (1)");
            }
            if(operator_max <= 0 || operator_max >= segmenty.size()-1){
                throw new Exception("Błąd składni (2)");
            }
            //odczytanie argumentów
            ParserSegment argument_l = (ParserSegment)(segmenty.elementAt(operator_max-1));
            ParserSegment operator = (ParserSegment)(segmenty.elementAt(operator_max));
            ParserSegment argument_r = (ParserSegment)(segmenty.elementAt(operator_max+1));
            //wykonanie pojedynczej operacji
            double wynik = Parser.calculate_operator(argument_l.text, argument_r.text, operator.text);
            argument_l.text = ""+wynik;
            //jeśli wynik zawiera kropkę
            if(argument_l.text.indexOf('.')>=0){
                //obcięcie zer na końcu
                while(argument_l.text.charAt(argument_l.text.length()-1) == '0'){
                    argument_l.text = argument_l.text.substring(0, argument_l.text.length()-1);
                }
                //obcięcie kropki na końcu
                if(argument_l.text.charAt(argument_l.text.length()-1) == '.'){
                    argument_l.text = argument_l.text.substring(0, argument_l.text.length()-1);
                }
            }
            //usunięcie operatora i prawego argumentu
            segmenty.removeElementAt(operator_max);
            segmenty.removeElementAt(operator_max);
        }
        return ((ParserSegment)segmenty.firstElement()).text; //wynik = ostatni (jedyny) segment na liście
    }
}
