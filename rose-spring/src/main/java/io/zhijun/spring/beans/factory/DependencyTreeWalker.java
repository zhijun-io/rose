package io.zhijun.spring.beans.factory;

import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;

/**
 * {@link Dependency} Tree Walker
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DependencyTreeWalker {

    /**
     * Walks the given {@link Dependency} tree, merging duplicate children into their siblings
     * and removing duplicated entries. After this method returns, the tree will contain no
     * duplicate nodes at any level; instead, their children will have been merged into the
     * remaining occurrence.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DependencyTreeWalker walker = new DependencyTreeWalker();
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B")
     *    .addChild("C")
     *    .child("C")
     *    .addChildren("D", "E").addChild("B");
     *   // Before walk: A[B, C[D, E, B]]
     *   walker.walk(a);
     *   // After walk:  A[C[D, E, B]]
     * }</pre>
     *
     * @param dependency the root {@link Dependency} whose tree will be walked and deduplicated
     * @return the same {@link Dependency} instance after deduplication
     */
    public Dependency walk(Dependency dependency) {
        List<Dependency> children = dependency.children;
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Dependency child = children.get(i);
            List<Dependency> siblings = children.subList(i + 1, size);
            walk(child, siblings);
        }
        removeIfDuplicated(dependency);
        return dependency;
    }

    private void removeIfDuplicated(Dependency dependency) {
        List<Dependency> children = dependency.children;
        Iterator<Dependency> iterator = children.iterator();
        while (iterator.hasNext()) {
            Dependency child = iterator.next();
            removeIfDuplicated(child);
            if (child.duplicated) {
                iterator.remove();
            }
        }
    }

    private void walk(Dependency child, List<Dependency> siblings) {
        if (isEmpty(siblings)) {
            return;
        }
        if (siblings.contains(child)) {
            child.duplicate();
        }
        for (Dependency sibling : siblings) {
            if (child.equals(sibling)) {
                child.duplicate();
                mergeChildren(child, sibling);
            }
            // Recursive call
            walk(child, sibling.children);
        }
    }

    private void mergeChildren(Dependency child, Dependency sibling) {
        List<Dependency> sources = child.children;
        List<Dependency> targets = sibling.children;
        if (!sources.equals(targets)) {
            for (Dependency source : sources) {
                if (!targets.contains(source)) {
                    targets.add(source);
                }
            }
        }

    }
}
