package disjoint.set.union;

import java.util.ArrayList;

public class Group implements Comparable<Group> {
    private final ArrayList<Integer> strings;

    public Group() {
        this.strings = new ArrayList<>();
    }

    @Override
    public int compareTo(Group group) {
        return group.strings.size() - this.strings.size();
    }

    public void addString(Integer s) {
        strings.add(s);
    }

    public ArrayList<Integer> getStrings() {
        return strings;
    }
}
