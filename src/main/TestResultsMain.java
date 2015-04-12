package main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

import multitouch.MultiTouch;
import multitouch.UserParamsMultiTouch;

import bigtouch.BigTouch;
import bigtouch.GuessFactoryBigTouch;
import bigtouch.UserParamsBigTouch;


public class TestResultsMain {

	public static void main(String[] args) {
		try {
//			testBigTouch();
			testMultiTouch();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Big Touch demo
	 */
	private static void testBigTouch() throws IOException{
		//Open result file and write header
		PrintWriter writer = new PrintWriter("results/bigTouch.csv", "UTF-8");
		writer.println("user,Avg. touch,wAvg,wDown,wTime,wPressure,wRadius,Avg. dist");

		//Create Users from files
		ArrayList<String> bigTouchFiles = filesList("expResults/bigTouch");
		ArrayList<User> users = getUsersFromFiles(bigTouchFiles);

		//For each user
		for (User user : users){
			//Write user's details
			String userData = user.getName()+","+d2s(user.getAvgTouchSize());
			writer.print(userData);

			//Get user's params according to his tests and assign them to the user
			UserParamsBigTouch userParams = BigTouch.getBigTouchParams(user.getTests());
			user.setUserParamsBigTouch(userParams);

			//Print user's params
			writer.print(","+d2s(userParams.getwAvg())+","+d2s(userParams.getwDown())+","+d2s(userParams.getwTime())+","+d2s(userParams.getwPressure())+","+d2s(userParams.getwRadius()));

			//For each one of the user's tests - calculate the distance between the
			//target and the guess according to the user's param
			double totDist = 0;
			for(Test test : user.getTests()){
				Circle c = BigTouch.guessCircleBigTouch(test.getTouches(), user.getUserParamsBigTouch());
				double dist = GuessFactoryBigTouch.getDistanceFromCenter(test.getCircle(), c);
				totDist +=dist;
			}
			//Print the average distance from target
			writer.print(","+d2s(totDist/user.getTests().size()));

			//For each test - print the distance from the target's X and target's Y
			for(Test test : user.getTests()){
				if (!Arrays.asList(Consts.TEST_TYPES).contains(test.getType())){
					continue;
				}
				Circle c = BigTouch.guessCircleBigTouch(test.getTouches(), user.getUserParamsBigTouch());
				double xDist = c.getCenter().getX() - test.getCircle().getCenter().getX();
				double yDist = c.getCenter().getY() - test.getCircle().getCenter().getY();
				writer.print(",X="+d2s(xDist)+" Y="+d2s(yDist));
			}

			writer.println();
		}
		writer.close();
	}


	/**
	 * MultiTouch demo
	 * @throws IOException 
	 */
	private static void testMultiTouch() throws IOException{
		PrintWriter writer = new PrintWriter("results/MultiTouch.csv", "UTF-8");
		writer.println("user,wFirst,wPressure,wTime,wStructure");

		//Create Users from files
		ArrayList<String> multiTouchFiles = filesList("expResults/multiTouch");
		ArrayList<User> users = getUsersFromFiles(multiTouchFiles);

		//For each user
		for (User user : users){
			//Write user's details
			String userData = user.getName();
			writer.print(userData);

			//Get user's params according to his tests and assign them to the user
			UserParamsMultiTouch userParams = MultiTouch.getMultiTouchParams(user.getTests());
			user.setUserParamsMultiTouch(userParams);
			
			//Print user's params
			writer.print(","+d2s(userParams.getwFirst())+","+d2s(userParams.getwMaxPressure())+","+d2s(userParams.getwMaxTime())+","+d2s(userParams.getwStructure()));

			//Print guesses
			for (Test test : user.getTests()){
				if (!Arrays.asList(Consts.TEST_TYPES).contains(test.getType())){
					continue;
				}
				if (Consts.DEBUG) System.out.print(user.getName());
				writer.print(","+MultiTouch.guessFingure(test.getTouches(), userParams));
				if (Consts.DEBUG) System.out.println();
			}
			
			writer.println();
		}
		writer.close();
	}



	////////////// Files ///////////////////
	private static ArrayList<User> getUsersFromFiles(ArrayList<String> filePaths) throws IOException{
		ArrayList<User> users = new ArrayList<>();

		User currUser = null;
		for (String filePath : filePaths){

			File file = new File(filePath);
			String fileName = file.getName();
			String userName = fileName.substring(0,fileName.indexOf("_"));

			if (currUser == null || !userName.equals(currUser.getName())){
				if (currUser !=  null){
					users.add(currUser);
				}
				currUser = new User(userName);
			}

			ArrayList<Test> tests = getTestsFromFile(filePath);
			currUser.addTests(tests);
		}
		//Add last user
		if (currUser != null){
			users.add(currUser);
		}
		return users;
	}


	private static ArrayList<Test> getTestsFromFile(String filePath) throws IOException{
		ArrayList<Test> tests = new ArrayList<>();

		File file = new File(filePath);
		String fileName = file.getName();

		String[] nameParts = fileName.split("_");

		String circlesType = nameParts[nameParts.length-1];
		String circlesFilePath = "circles/circles_"+circlesType;

		String type = circlesType.substring(0,circlesType.indexOf("."));

		for (String n : nameParts){
			if (isNumeric(n) || n.indexOf("tid") != -1){
				int shapeId = 0;
				int touchId = 1;
				if (isNumeric(n)){
					shapeId = Integer.parseInt(n);
				}
				else{
					shapeId = Integer.parseInt(n.substring(0,n.indexOf("tid")));
					touchId = Integer.parseInt(n.substring(n.indexOf("tid")+3));
				}
				tests.add(new Test(getCircleFromFile(circlesFilePath, shapeId), getTouchesFromFile(filePath, shapeId, touchId),type));
			}
		}
		return tests;
	}

	private static ArrayList<Touch> getTouchesFromFile(String filePath, int shapeId, int touchId) throws FileNotFoundException, IOException{
		ArrayList<Touch> touches = new ArrayList<Touch>();
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line;
		br.readLine();
		boolean isFirst = true;
		int firstPointerId = 0;
		int pointerId;

		while ((line = br.readLine()) != null) {
			if (line.startsWith("TPS")){
				break;
			}
			String[] parts = line.split(",");
			if (Integer.parseInt(parts[0]) == shapeId && Integer.parseInt(parts[1]) == touchId){
				
				//TODO fix pointerId bug on TremorTouchTest
				if (isFirst){
					firstPointerId = Integer.parseInt(parts[3]);
					isFirst = false;
					pointerId = 1;
				}
				else{
					pointerId = Integer.parseInt(parts[3]) - firstPointerId +1;
				}
				touches.add(new Touch(new Point(s2d(parts[6]), s2d(parts[7])), parts[4], s2d(parts[9]), s2d(parts[8]), Integer.parseInt(parts[5]),Integer.parseInt(parts[2]),pointerId));
			}
		}
		
		return touches;
	}

	private static Circle getCircleFromFile(String filePath, int shapeId) throws NumberFormatException, IOException{
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String line;
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(",");
			if (Integer.parseInt(parts[0]) == shapeId){
				return new Circle(new Point(s2d(parts[1]), s2d(parts[2])), Integer.parseInt(parts[3]));
			}
		}
		return null;
	}


	///////// Prints ///////////////
	private static ArrayList<String> filesList(String rootDirPath){
		ArrayList<String> filesList = new ArrayList<>();
		File folder = new File(rootDirPath);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			filesList.add(listOfFiles[i].getAbsolutePath());
		}
		return filesList;
	}

	private static double s2d(String s){
		double d = Double.parseDouble(s);
		return d;
	}

	private static String d2s(double d){
		String s = String.format("%.5f",d);
		return s;
	}


	/////////////// Other /////////////////
	private static boolean isNumeric(String str)
	{
		for (char c : str.toCharArray())
		{
			if (!Character.isDigit(c)) return false;
		}
		return true;
	}
}
