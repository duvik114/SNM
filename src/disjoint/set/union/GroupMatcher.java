package disjoint.set.union;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GroupMatcher {

    private final String INPUT_FILE;
    private final String OUTPUT_FILE;
    private final static int COL_NUM = 3;
    private final LinkedHashSet<String> strings;
    private final ArrayList<HashMap<Double, Integer>> wordsMaps;

    public GroupMatcher(String INPUT_FILE, String OUTPUT_FILE) {
        this.INPUT_FILE = INPUT_FILE;
        this.OUTPUT_FILE = OUTPUT_FILE;
        this.strings = new LinkedHashSet<>();
        this.wordsMaps = new ArrayList<>();
    }

    private Double[] splitString(String s) throws GroupMatchException {
        String[] tokens = s.split(";", -1);

        if (tokens.length != COL_NUM) {
            throw new GroupMatchException("Wrong number of columns (should be " + COL_NUM + "): " + s);
        }

        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].isEmpty()) {
                tokens[i] = "\"\"";
            }
            else if (tokens[i].length() < 2) {
                throw new GroupMatchException("Wrong string in input file: " + s);
            }
        }

        try {
            return Arrays.stream(tokens)
                    .map(t -> t.substring(1, t.length() - 1))
                    .map(t -> {
                        if (t.isEmpty()) {
                            return null;
                        } else {
                            return Double.valueOf(t);
                        }
                    })
                    .toArray(Double[]::new);
        } catch (NumberFormatException e) {
            throw new GroupMatchException("Cannot format string to numbers: " + s);
        }
    }

    private void getSNM(DSU dsu) throws GroupMatchException {
        for (int i = 0; i < COL_NUM; i++) {
            wordsMaps.add(new HashMap<>());
        }

        try (BufferedReader br = new BufferedReader(new FileReader(INPUT_FILE, StandardCharsets.UTF_8))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {

                Double[] values;
                try {
                    values = splitString(line);
                } catch (GroupMatchException e) {
                    System.err.println(e.getMessage());
                    continue;
                }

                if (strings.contains(line)) {
                    continue;
                } else {
                    addNewLine(dsu, line, lineNum);
                }

                checkColumns(dsu, lineNum, values);

                lineNum++;
            }
        } catch (FileNotFoundException e) {
            throw new GroupMatchException("Input file " + INPUT_FILE + " not found");
        } catch (IOException e) {
            throw new GroupMatchException("Error while reading input file: " + INPUT_FILE);
        }
    }

    private void addNewLine(DSU dsu, String line, int lineNum) {
        strings.add(line);
        dsu.addString(lineNum);
    }

    private void checkColumns(DSU dsu, int lineNum, Double[] values) {
        for (int i = 0; i < COL_NUM; i++) {
            Double value = values[i];

            if (value == null) {
                continue;
            }

            if (wordsMaps.get(i).containsKey(value)) {
                dsu.union(wordsMaps.get(i).get(value), lineNum);
            } else {
                wordsMaps.get(i).put(value, lineNum);
            }
        }
    }

    private int printGroups(DSU dsu, long timeStart) throws GroupMatchException {
        int i = 1;
        String[] pos = new String[1];
        Group[] groups = dsu.getGroupsAndPos(pos);
        String[] stringsArray = strings.toArray(String[]::new);

        try {
            File output = new File(OUTPUT_FILE);
            output.createNewFile();
        } catch (IOException e) {
            throw new GroupMatchException("Error creating output file");
        }

        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, StandardCharsets.UTF_8));

            writer.write("Number of groups containing more than 1 string: " + pos[0]);
            writer.newLine();

            for (Group g : groups) {
                writer.write("Group " + i++ + ":");
                writer.newLine();
                for (int num : g.getStrings()) {
                    writer.write(stringsArray[num]);
                    writer.newLine();
                }
            }

            writer.write("================================================================");
            writer.newLine();

            writer.write("Number of groups: " + groups.length);
            writer.newLine();

            writer.write("Done in " + ((System.currentTimeMillis() - timeStart) / 1000) + " seconds!");
            writer.newLine();

            writer.close();
        } catch (IOException e) {
            throw new GroupMatchException("Error writing to output file: " + e.getMessage());
        }
        return groups.length;
    }

    private static void printUsage() {
        System.out.println("Two arguments expected: <INPUT_FILE_NAME> <OUTPUT_FILE_NAME>");
        System.out.println("Input file should be in \"SNM/src/disjoint/set/union\" folder");
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            return;
        }

        long timeStart = System.currentTimeMillis();

        DSU dsu = new DSU();
        GroupMatcher groupMatcher = new GroupMatcher(args[0], args[1]);

        try {
            groupMatcher.getSNM(dsu);
        } catch (GroupMatchException e) {
            System.err.println(e.getMessage());
            return;
        }

        int groupsCount;
        try {
            groupsCount = groupMatcher.printGroups(dsu, timeStart);
        } catch (GroupMatchException e) {
            System.err.println(e.getMessage());
            return;
        }

        System.out.println("================================================================");
        System.out.println("Number of groups: " + groupsCount);
        System.out.println("Done in " + ((System.currentTimeMillis() - timeStart) / 1000) + " seconds!");
    }
}
