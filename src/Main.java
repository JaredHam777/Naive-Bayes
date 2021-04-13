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

public class Main {
	
	static public Map<String, Float> probEachWord;
	static public ArrayList<Model> groups;
	static String folderPath = "data/newsgroups";
	
	public static double probability(int groupIndex, String[] words) {
		Model m = groups.get(groupIndex);
		double p = 1.0;		
		for(String word : words)
		{			
			Float pWord = probEachWord.get(word);
			if(pWord==null) {continue;}
			Float pWordGivenM = m.wordOccurrences.get(word);			
			if(pWordGivenM==null) {
				//this word is found in other groups but not this one:
				pWordGivenM = (float) (1.0 / (pWord  * m.numFiles * groups.size() * groups.size() * m.numFiles));			
			}
			p*= (pWordGivenM / (pWord * groups.size()));
		}
		return p;
	}
	
	public static Model getBestFit(String[] words)	{		
		double max = 0;
		Model bestFit = null;
		for(int i=0; i<groups.size(); i++)	{
			double p = probability(i, words);
			System.out.println("P: " + p);
			if(p>max) {
				max = p;
				bestFit = groups.get(i);
			}
		}
		return bestFit;
	}
	
	public static void buildModel(String folderPath) throws Exception	{
		
		groups = new ArrayList<Model>();
		probEachWord = new HashMap<String, Float>();
		
		File folder = new File(folderPath);
		if(!folder.isDirectory()) {throw new Exception(folderPath + " is not a directory");}
		for(File fileEntry : folder.listFiles()) {
			if(!fileEntry.isDirectory()) {throw new Exception(fileEntry + " is not a directory");}
			Model m = new Model(fileEntry.getPath());
			groups.add(m);
		}
		
		populateWordOccurrences();
		
	}
	
	public static void populateWordOccurrences()	{
		for(Model m : groups)	{			
			for(Entry<String, Float> p : m.wordOccurrences.entrySet())	{		
				if(probEachWord.containsKey(p.getKey()))	{
					float val = probEachWord.get(p.getKey());
					val += p.getValue()/groups.size();
					probEachWord.put(p.getKey(), val);
				}	else	{
					probEachWord.put(p.getKey(), p.getValue()/groups.size());
				}
			}
		}	
	}
	
	public static Map<String, Float> loadWordModelFromFile(String path) throws FileNotFoundException {			
			File file = new File(path);
			Scanner sc = new Scanner(file);		
			String line = null;
			 Map<String, Float> returnMap = new HashMap<String, Float>();
			
			while(sc.hasNextLine()) {
				line = sc.nextLine();
				String[] values = line.split(" ");
				returnMap.put(values[0], Float.parseFloat(values[1]));
			}
			sc.close();
			return returnMap;		
	}
	
	public static void saveWordModelToFile(String path) throws IOException {
		BufferedWriter output=null;
		File resultFile = new File( path);
		output = new BufferedWriter(new FileWriter(resultFile));
		for(Entry<String, Float> word : probEachWord.entrySet())	{
			output.write(word.getKey() + " " +  word.getValue().toString() + "\n");
		}		
		output.close();
	}
	
	public static void loadModel(String modelFolder) throws Exception	{
		groups = new ArrayList<Model>();
		probEachWord = new HashMap<String, Float>();		
		
		File folder = new File(modelFolder);
		if(!folder.isDirectory()) {throw new Exception(folderPath + " is not a directory");}
		if(folder.listFiles().length==0) {
			throw new Exception(folder + " is empty");
		}
		for(File fileEntry : folder.listFiles()) {
			if(fileEntry.isDirectory()) {throw new Exception(fileEntry + " is a directory");}
			Model m = new Model();
			m.loadFromFile(fileEntry.getPath());
			groups.add(m);
		}
		
		try {
			probEachWord = loadWordModelFromFile("data/word-model");
		}	catch(Exception e)	{
			populateWordOccurrences();
			saveWordModelToFile("data/word-model");
		}
	}
	
	public static void doFindBestFit(String[] args) throws FileNotFoundException {
		//args[1] = test file
		//args[2] = model folder
		try {
			loadModel(args[2]);
		}	catch(Exception e) {
			System.out.println("Error: you must build the model first.  Try running \"build [directory containing classifications (i.e. data/newsgroups]\"");
			return;
		}	
		File file = new File(args[1]);
		Scanner sc = new Scanner(file);		
		String line = null;
		
		Set<String> allWords = new HashSet<String>();
		
		while(sc.hasNextLine()) {
			line = sc.nextLine();
			String[] words = line.split(" ");
			for(String word : words) {
				allWords.add(word);
			}
		}
		
		Model bestModel = getBestFit((String[]) allWords.toArray(new String[0]));
		System.out.println(bestModel);
		
	}
	
	public static void doBuild(String[] args) throws Exception {
		
		//first delete all files in the model:
		File buildFile = new File("data/model");
		for(File fileEntry : buildFile.listFiles()) {
			fileEntry.delete();
		}
		
		buildModel(args[1]);
		for(Model m : groups) {
			System.out.println("saving model " + m);
			m.saveToFile("data/model");
		}
		System.out.println("saving word-model");
		saveWordModelToFile("data/word-model");
	}


	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//Model m1 = new Model("data/newsgroups/alt.atheism");
		Scanner userIn = new Scanner(System.in);  // Create a Scanner object

		while(true) {
			System.out.print(">");
			String input = userIn.nextLine();
			String[] tokens = input.split(" ");

			switch(tokens[0]) {
			case "build":
				doBuild(tokens);
				break;
			case "best_fit":
				doFindBestFit(tokens);
				break;
			default:
				break;
			}	
			//System.out.println();
		}
	}

}
