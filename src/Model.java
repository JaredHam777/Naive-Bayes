import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;



public class Model {
	
	int numFiles;
	public Map<String, int[]> wordModel;
	public static Set<String> dictionary;
	public Map<String, Float> wordOccurrences;
	public Model() throws Exception {
		populateDictionary("data/English");
		this.numFiles = getNumberOfFiles("data/newsgroups/alt.atheism");
	}
	public int[] zeroArray;
	public String name;
	
	public Model(String path) throws Exception {
		this.name = path.substring(path.lastIndexOf('\\')+1); 
		
		numFiles = this.getNumberOfFiles(path);
		zeroArray = new int[numFiles];
		for(int i : zeroArray) {i = 0;}
		populateDictionary("data/English");
		wordModel = new HashMap<String, int[]>();
		this.importFilesIntoModel(path, 0);
		this.wordMapToOccurrences();
	}
	
	public static void populateDictionary(String path) throws FileNotFoundException	{
		
		if(dictionary == null) {
			compileEnglishDictionary(path);
		}
	}
	
	public void saveToFile(String path) throws IOException {
		//only need to save our word occurrences:
		BufferedWriter output=null;
		File resultFile = new File(path + "/" + this.toString() + "-Occurrences");
		output = new BufferedWriter(new FileWriter(resultFile));
		for(Entry<String, Float> word : this.wordOccurrences.entrySet())	{
			output.write(word.getKey() + " " +  word.getValue().toString() + "\n");
		}		
		output.close();
	}
	
	public void loadFromFile(String path) throws Exception {
		this.name = path.substring(path.lastIndexOf('/') + 1, path.lastIndexOf('-'));
		File file = new File(path);
		Scanner sc = new Scanner(file);		
		String line = null;
		this.wordOccurrences = new HashMap<String, Float>();
		while(sc.hasNextLine()) {
			line = sc.nextLine();
			String[] values = line.split(" ");
			this.wordOccurrences.put(values[0], Float.parseFloat(values[1]));
		}
		sc.close();
		
		
	}
	
	public enum wordPatterns	{
		Word("[a-z]+"), Number("[0-9]+");
		
		String value;
		wordPatterns(String value) {
			this.value = value;
		}	
	}
	
	@Override
	public String toString()	{
		return this.name;
	}
	
	public void wordMapToOccurrences()	{
		this.wordOccurrences = new HashMap<String, Float>();
		for(Entry<String, int[]> wordVector : this.wordModel.entrySet())	{			
			int sum = 0;			
			for(int i : wordVector.getValue()) {
				if(i>0) {
					sum++;
				}
			}
			float val = sum / ((float) wordVector.getValue().length);
			this.wordOccurrences.put(wordVector.getKey(), val);
		}
	}

	public static void compileEnglishDictionary(String path) throws FileNotFoundException	{
		dictionary = new HashSet<String>();
		File file = new File(path);
	
		Scanner sc = new Scanner(file);					
		while(sc.hasNextLine())	{	
			dictionary.add(sc.nextLine());
		}
		sc.close();
	}
	
	public int getNumberOfFiles(String path) throws Exception {
		int count = 0;
		File folder = new File(path);
		if(!folder.isDirectory()) {throw new Exception(path + " is not a directory");}
		for(File fileEntry : folder.listFiles()) {
			count++;
		}
		return count;
	}
	
	public void importFilesIntoModel(String path, int fileNum) throws Exception {
		File folder = new File(path);
		if(!folder.isDirectory()) {throw new Exception(path + " is not a directory");}
		for(File fileEntry : folder.listFiles()) {
			importFileIntoModel(fileEntry, fileNum);
			fileNum++;
		}
	}
	
	public char[] invalidChars = {'.', ',', '\"', '\'', '(', ')', '>', '!', '?', '/', '@', '#', 
			':', ';', '$', '%', '^', '&', '*', '-', '_', '+', '=', '[', '{', ']', '}', '\\', '|', '<' };
	
	public void importFileIntoModel(File file, int fileNum) throws FileNotFoundException {
		//ignore first ten lines
		
		Scanner sc = new Scanner(file);			
		String line = null;		
		while(sc.hasNextLine() && (line = sc.nextLine()).contains(": ")) {}
		while(sc.hasNextLine())	{
			if(line==null) {line = sc.nextLine();}
			line = filterNonLetters(line);		
			//System.out.println(line);
			
			String[] lines = line.split("\\s+");
			if(lines.length>0)	{
				addToDictionary(lines,fileNum);
			}
			line = sc.nextLine();
		}
		
		sc.close();
	}

	private void addToDictionary(String[] words, int currentFile)	{
		for(String word : words)	{			
			word = word.toLowerCase();
			if(word.length()<2) {continue;}
			if(dictionary.contains(word))	{				
				if(this.wordModel.containsKey(word))	{
					this.wordModel.get(word)[currentFile]++;					
				}	else	{	
					int[] newArr = zeroArray.clone();
					newArr[currentFile]++;
					this.wordModel.put(word, newArr);
				}
			}
		}
	}
	
	public String filterNonLetters(String line)	{
		//System.out.println("BEFORE " + line);
		String s = line.replaceAll("[^a-z^A-Z]", " ");		
		return s;
	}
	
	
}

