import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;

public class NaiveBayes {
	final static int numVac = 61188;
	//create a treemap using its get ceiling function to return the docu's categ
	public static TreeMap<Integer, Integer> tm_train = new TreeMap<Integer, Integer>();
	public static TreeMap<Integer, Integer> tm_test = new TreeMap<Integer, Integer>();
	
	public static int[] calculatePerCateg(String filePath) {
		int[] DocuPerCateg = new int[20];
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine())!=null) {
				String item[] = line.split(",");
				String data_s = item[item.length - 1];
				int data = Integer.parseInt(data_s);
				DocuPerCateg[data - 1] ++;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return DocuPerCateg;
	}
	public static int calDocu(int[] a) {
		int sum = 0;
		for (int i = 0;i<a.length;i++) {
			sum += a[i];
		}
		return sum;
	}
	public static void setCateg(int[] numDocuPerCateg, TreeMap<Integer, Integer> tm) {
		
		int sum = 0;
		int i = 0;
		for (int b:numDocuPerCateg) {
			
			sum += b;
			tm.put(sum, i);
			//System.out.println(sum);
			i++;
		}
	}
	public static int getCateg(int docId, TreeMap<Integer, Integer> tm) {
		int categ = 0;
		Integer ceiling = tm.ceilingKey(docId);		
		categ = tm.get(ceiling);
		return categ;
	}
	public static int[][] calNumWord(String filePath, TreeMap<Integer, Integer> tm) {
		int[][] numWord = new int[20][numVac + 1];
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			//index of numDocuPerCateg
			//int test = 0;
			
			while ((line = reader.readLine())!=null) {
			//line = reader.readLine();
			String item[] = line.split(",");		
				
				int docId = Integer.parseInt(item[0]);
				int vocId = Integer.parseInt(item[1]);
				int count = Integer.parseInt(item[2]);
				
				int categ = getCateg(docId, tm);

				numWord[categ][vocId-1] += count;
				numWord[categ][numVac] += count;
				
				//test = vocId;
			}
			//System.out.println(test);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return numWord;
	}
	public static float[] calProOfCareg(int sum, int[] a) {
		float[] prob = new float[20];
		for (int i = 0; i < a.length; i++)
			prob[i] = (float)a[i] / (float)sum;
		return prob;
	}
	public static float[][] calPmle(int[][] numWordPerCateg) {
		float[][] Pmle = new float[20][numVac];
		for (int i = 0; i < 20; i++) 
			for (int j = 0; j < numVac; j++) 
				Pmle[i][j] = (float)numWordPerCateg[i][j] / (float)numWordPerCateg[i][numVac];

		return Pmle;
	}
	public static float[][] calPbe(int[][] numWordPerCateg) {
		float[][] Pbe = new float[20][numVac];
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < numVac; j++) {
				Pbe[i][j] = (float)(numWordPerCateg[i][j] + 1)/ (float)(numWordPerCateg[i][numVac] + numVac); 
			//if (j==0) System.out.println(Pbe[i][j]);
			}
		}
		
		return Pbe;
	}
	public static int[][] getEstiCateg(String filePath, float[][] Pmle, int[][] numWordPerCateg, float[] prob_categ, int[][] confusion_matrix, TreeMap<Integer, Integer> tm) {
		//int[][] confusion_matrix = new int[20][20];
		int categ = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			//index of numDocuPerCateg
			//int test = 0;
			int previousDoc = -1;
			float[] sum = new float[20];
			
			while ((line = reader.readLine())!=null) {
			//line = reader.readLine();
			String item[] = line.split(",");
				
				int docId = Integer.parseInt(item[0]);
				int vocId = Integer.parseInt(item[1]);
				int count = Integer.parseInt(item[2]);
				
				if (previousDoc != docId && previousDoc!=-1) {
					//System.out.println("THIS IS SUM!"+sum[i]);
					float max = (float)Integer.MIN_VALUE;
					int maxId = 0;
					for (int j = 0; j < 20; j++) {
						if (sum[j] > max) {
							max = sum[j];
							maxId = j;
						}
						sum[j] = 0;
						sum[j] += Math.log(prob_categ[j]);
					}
					confusion_matrix[getCateg(previousDoc, tm)][maxId] ++;
					previousDoc = docId;
				}
				
				for (int i = 0; i < 20; i ++) {
					if (previousDoc == -1) {
						previousDoc = docId;
						sum[i] += Math.log(prob_categ[i]);
					}
					
					//System.out.println(confusion_matrix[getCateg(previousDoc[i])][10]);
					//Math.pow(float a,float b)；
					sum[i] += count * Math.log(Pmle[i][vocId-1]);
				}
			}
			float max = (float)Integer.MIN_VALUE;
			int maxId = 0;
			for (int j = 0; j < 20; j++) {
				if (sum[j] > max) {
					max = sum[j];
					maxId = j;
				}
				sum[j] = 0;
				sum[j] += Math.log(prob_categ[j]);
			}
			confusion_matrix[getCateg(previousDoc, tm)][maxId] ++;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
				
		
		return confusion_matrix;
	}
	public static void outputMatric(int[][] matrix) {
		for (int i = 0; i < matrix.length; i ++) {
			for (int j = 0; j < matrix[i].length; j ++) {
				System.out.print(matrix[i][j] + ",");
			}
			System.out.println();
		}
	}
	public static void outputCategPro(float[] a) {
		System.out.println("------THIS IS Class priors --------");
		for (int i = 0; i < a.length; i++) System.out.println("P(Omega = "+ (i+1) + ") = " + a[i]);
		
	}
	public static void calClassAccuracy(int [][] matrix) {
		int sum = 0;
		int[] correct = new int[matrix.length];
		int[] sum_class = new int[matrix.length];
		for (int i = 0; i < matrix.length; i ++) {
			for (int j = 0; j < matrix[0].length; j ++) {
				sum += matrix[i][j];
				sum_class[i] += matrix[i][j];
				if (i == j) correct[i] = matrix[i][j];
			}
		}
		int correct_sum = 0;
		for (int b:correct) correct_sum += b;
		System.out.println("Overall Accuracy = " + (float)correct_sum / (float)sum);
		System.out.println("Class Accuracy:");
		for (int i = 0; i < correct.length; i++) {
			System.out.println("Group "+ (i+1) +": " + (float)correct[i]/(float)sum_class[i]);
		}
	}
	public static void main(String[] args) {
		String vocabulary_txt = args[0];
		String map_csv = args[1];
		String training_label_csv = args[2];
		String training_data_csv = args[3];
		String testing_label_csv = args[4];
		String testing_data_csv = args[5];
		
		//You can add your file root here!
		String fileRoot = "";
		int[] numDocuPerCateg = new int[20];
		numDocuPerCateg = calculatePerCateg(fileRoot + training_label_csv);
		//System.out.println(numDocuPerCateg[0]);
		int sumDocu = calDocu(numDocuPerCateg);
		//System.out.println(sumDocu);
		float[] prob_categ = new float[20];
		prob_categ = calProOfCareg(sumDocu, numDocuPerCateg);
		outputCategPro(prob_categ);
		//System.out.println(prob_categ[0]);
		//numWordPerCateg[][numVac+1] is the total number n of each class
		int[][] numWordPerCateg = new int[20][numVac+1];
		setCateg(numDocuPerCateg, tm_train);
		//System.out.println(getCateg(479));
		numWordPerCateg = calNumWord(fileRoot + training_data_csv, tm_train);
		float[][] Pmle = new float[20][numVac];
		Pmle = calPmle(numWordPerCateg);
		float[][] Pbe = new float[20][numVac];
		Pbe = calPbe(numWordPerCateg);
		

		//Using BE to test training datas
		int[][] test_confusion_matrix = new int[20][20];
		test_confusion_matrix = getEstiCateg(fileRoot + training_data_csv, Pbe, numWordPerCateg, prob_categ, test_confusion_matrix, tm_train);
		outputMatric(test_confusion_matrix);
		calClassAccuracy(test_confusion_matrix);
		System.out.println("----------------THIS IS A LINE-------------------");
		
		//Using BE and MLE to test test datas
		int[] numDocuPerCateg_test = new int[20];
		numDocuPerCateg_test = calculatePerCateg(fileRoot + testing_label_csv);
		int sumDocu_test = calDocu(numDocuPerCateg_test);
		//System.out.println(sumDocu);
		float[] prob_categ_test = new float[20];
		prob_categ_test = calProOfCareg(sumDocu_test, numDocuPerCateg_test);
		//System.out.println(prob_categ[0]);
		//numWordPerCateg[][numVac+1] is the total number n of each class
		int[][] numWordPerCateg_test = new int[20][numVac+1];
		setCateg(numDocuPerCateg_test, tm_test);
		//System.out.println(getCateg(479));
		numWordPerCateg_test = calNumWord(fileRoot + testing_data_csv, tm_test);
		
		
		//Using BE to test!
		int[][] BE_confusion_matrix = new int[20][20];
		BE_confusion_matrix = getEstiCateg(fileRoot + testing_data_csv, Pbe, numWordPerCateg_test, prob_categ_test, BE_confusion_matrix, tm_test);
		outputMatric(BE_confusion_matrix);
		calClassAccuracy(BE_confusion_matrix);
		System.out.println("----------------THIS IS A LINE-------------------");
		
		//Using MLE to test!
		int[][] MLE_confusion_matrix = new int[20][20];
		MLE_confusion_matrix = getEstiCateg(fileRoot + testing_data_csv, Pmle, numWordPerCateg_test, prob_categ_test, MLE_confusion_matrix, tm_test);
		outputMatric(MLE_confusion_matrix);
		calClassAccuracy(MLE_confusion_matrix);
		System.out.println("--------------------END-----------------------");
		
	}

}
