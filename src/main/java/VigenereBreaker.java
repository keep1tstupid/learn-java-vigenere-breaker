import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/*
 * My work.
 */

public class VigenereBreaker {
    private static ArrayList<String> keyForMessage;
    private static HashMap<String, HashSet<String>> dictionaries;

    public VigenereBreaker() throws FileNotFoundException {
        keyForMessage = new ArrayList<String>();
        dictionaries = new HashMap<String, HashSet<String>>();
        prepareDictionaries();
    }

    // Work with dictionaries

    private static void fillDictionary(File f) throws FileNotFoundException {
        if (!f.exists()) {
            throw new FileNotFoundException();
        }

        Scanner scan = new Scanner(f);
        String filename = f.getName();
        HashSet<String> words = new HashSet<String>();
        while (scan.hasNextLine()) {
            String line = scan.nextLine().toLowerCase();
            if (!line.equals("")) {
                words.add(line);
            }
        }
        dictionaries.put(filename, words);
    }

    private static void prepareDictionaries() throws FileNotFoundException {
        fillDictionary(new File("dictionaries/Danish"));
        fillDictionary(new File("dictionaries/Dutch"));
        fillDictionary(new File("dictionaries/English"));
        fillDictionary(new File("dictionaries/French"));
        fillDictionary(new File("dictionaries/German"));
        fillDictionary(new File("dictionaries/Italian"));
        fillDictionary(new File("dictionaries/Portuguese"));
        fillDictionary(new File("dictionaries/Spanish"));
    }

    public static void checkDictionaries() {
        System.out.println("Available dictionaries are: ");
        for (Map.Entry<String, HashSet<String>> entry : dictionaries.entrySet()) {
                System.out.println(entry.getKey() + " " + entry.getValue().size() + " words");
        }
    }

    // Prepare substring

    public static String sliceString(String message, int whichSlice, int totalSlices) {
        StringBuilder sliced = new StringBuilder();
        for (int i = whichSlice; i < message.length(); i += totalSlices){
            sliced.append(message.charAt(i));
        }
        return sliced.toString();
    }

    // Work with keys

    public static int[] tryKeyLength(String encrypted, int keyLength, char mostCommon) {
        int[] keys = new int[keyLength];
        CaesarCracker decrypt = new CaesarCracker(mostCommon);
        for (int i = 0; i < keyLength; i++) {
            String s = sliceString(encrypted, i, keyLength);
            int a = decrypt.getKey(s);
            keys[i] = a;
        }
        return keys;
    }

    public static int countWords(String message, HashSet<String> dictionary){
        String[] words = message.split("\\W+");
        int count = 0;
        for (String word: words) {
            if (dictionary.contains(word.toLowerCase())) {
                count++;
            }
        }
        return count;
    }

    private static char mostCommonChar(HashSet<String> dictionary) {
        HashMap<Character, Integer> letters = new HashMap<Character, Integer>();

        for (String word: dictionary) {
            for (char c : word.toLowerCase().toCharArray()) {
                letters.put(c, letters.containsKey(c) ? letters.get(c) + 1 : 1);
            }
        }

        Map.Entry<Character, Integer> maxLetter = null;

        for (Map.Entry<Character, Integer> letter : letters.entrySet()) {
            if (maxLetter == null || letter.getValue().compareTo(maxLetter.getValue()) > 0) {
                maxLetter = letter;
            }
        }
        return maxLetter.getKey();
    }


    private static void saveMessageKey(int[] key) {
        keyForMessage.clear();
        keyForMessage.add("Length is " + key.length);
        keyForMessage.add(Arrays.toString(key));
    }

    public static void showKeys() {
        System.out.println(keyForMessage.toString());
        System.out.println(keyForMessage.size());
    }

    public static String breakForLanguage(String encrypted, HashSet<String> dictionary){
        int max = 0;
        String message = null;
        for (int i = 1; i <= 100; i++){
            int[] key = tryKeyLength(encrypted, i, mostCommonChar(dictionary));
            VigenereCipher chipher = new VigenereCipher(key);
            String res = chipher.decrypt(encrypted);
            if (countWords(res, dictionary) > max) {
                max = countWords(res, dictionary);
                message = res;
                saveMessageKey(key);
            }
        }

        //System.out.println("Amount of valid words is " + max);
        return message;
    }

    public static void breakForAllLanguages(String encrypted){
        int max = 0;
        String message = null;
        String language = null;
        for (String lang: dictionaries.keySet()) {
            String res = breakForLanguage(encrypted, dictionaries.get(lang));
            if (countWords(res, dictionaries.get(lang)) > max) {
                max = countWords(res, dictionaries.get(lang));
                message = res;
                language = lang;
            }
        }
        System.out.println(message + "\n" + language);
    }

    public static void breakVigenereAllLang() throws FileNotFoundException {
        File f = new File("secretmessage4.txt");
        if (!f.exists()) {
            throw new FileNotFoundException();
        }
        String message = f.toString();
        breakForAllLanguages(message);
    }

    public static void main(String[] args) throws FileNotFoundException {
        //VigenereBreaker test = new VigenereBreaker();
        //breakVigenereAllLang();
        //showKeys();
        //test.checkDictionaries();
    }
}
