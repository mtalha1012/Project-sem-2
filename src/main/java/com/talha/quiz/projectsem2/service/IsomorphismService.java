package com.talha.quiz.projectsem2.service;

import com.talha.quiz.projectsem2.model.Graph;
import com.talha.quiz.projectsem2.model.IsomorphismResult;
import com.talha.quiz.projectsem2.model.Node;

import java.util.*;
import java.util.stream.Collectors;

public class IsomorphismService {

    // Methods
    public static IsomorphismResult check(Graph g1, Graph g2) {
        // compute all invariants unconditionally so the UI always gets a full report
        boolean vertexMatch    = g1.getNodes().size() == g2.getNodes().size();
        boolean edgeMatch      = g1.getEdges().size() == g2.getEdges().size();
        boolean degreeMatch    = degreeSequencesMatch(g1, g2);
        boolean connectedMatch = isConnected(g1) == isConnected(g2);
        boolean bipartiteMatch = isBipartite(g1) == isBipartite(g2);
        boolean eulerMatch     = hasEulerCircuit(g1) == hasEulerCircuit(g2);

        boolean invariantsPassed = vertexMatch && edgeMatch && degreeMatch
                && connectedMatch && bipartiteMatch && eulerMatch;

        boolean isIsomorphic;
        IdentityHashMap<Node, Node> mapping = null;

        if (!invariantsPassed) {
            isIsomorphic = false;
        } else if (g1.getNodes().isEmpty()) {
            isIsomorphic = true;
            mapping = new IdentityHashMap<>();
        } else if (!wlTest(g1, g2)) {
            isIsomorphic = false;
        } else {
            mapping = vf2(g1, g2);
            isIsomorphic = mapping != null;
        }

        return new IsomorphismResult(g1, g2,
                vertexMatch, edgeMatch, degreeMatch,
                connectedMatch, bipartiteMatch, eulerMatch,
                isIsomorphic, mapping);
    }

    private static boolean degreeSequencesMatch(Graph g1, Graph g2) {
        return sortedDegrees(g1).equals(sortedDegrees(g2));
    }

    private static List<Integer> sortedDegrees(Graph g) {
        IdentityHashMap<Node, Integer> deg = new IdentityHashMap<>();
        for (Node n : g.getNodes()) deg.put(n, 0);
        for (var e : g.getEdges()) {
            deg.merge(e.getSourceNode(), 1, Integer::sum);
            deg.merge(e.getTargetNode(), 1, Integer::sum);
        }
        List<Integer> seq = new ArrayList<>(deg.values());
        Collections.sort(seq);
        return seq;
    }

    private static boolean isConnected(Graph g) {
        if (g.getNodes().isEmpty()) return true;
        IdentityHashMap<Node, List<Node>> adj = buildAdjacency(g);
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Queue<Node> queue = new LinkedList<>();
        Node start = g.getNodes().get(0);
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            for (Node nb : adj.getOrDefault(queue.poll(), Collections.emptyList())) {
                if (visited.add(nb)) queue.add(nb);
            }
        }
        return visited.size() == g.getNodes().size();
    }

    private static boolean isBipartite(Graph g) {
        IdentityHashMap<Node, List<Node>> adj = buildAdjacency(g);
        IdentityHashMap<Node, Integer> color = new IdentityHashMap<>();
        for (Node start : g.getNodes()) {
            if (color.containsKey(start)) continue;
            Queue<Node> queue = new LinkedList<>();
            queue.add(start);
            color.put(start, 0);
            while (!queue.isEmpty()) {
                Node curr = queue.poll();
                for (Node nb : adj.getOrDefault(curr, Collections.emptyList())) {
                    if (!color.containsKey(nb)) {
                        color.put(nb, 1 - color.get(curr));
                        queue.add(nb);
                    } else if (color.get(nb).equals(color.get(curr))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean hasEulerCircuit(Graph g) {
        // undirected graph has an Euler circuit iff every vertex has even degree
        IdentityHashMap<Node, Integer> deg = new IdentityHashMap<>();
        for (Node n : g.getNodes()) deg.put(n, 0);
        for (var e : g.getEdges()) {
            deg.merge(e.getSourceNode(), 1, Integer::sum);
            deg.merge(e.getTargetNode(), 1, Integer::sum);
        }
        return deg.values().stream().allMatch(d -> d % 2 == 0);
    }

    // 1-WL color refinement — catches most non-isomorphic pairs sharing the same degree sequence
    private static boolean wlTest(Graph g1, Graph g2) {
        IdentityHashMap<Node, List<Node>> adj1 = buildAdjacency(g1);
        IdentityHashMap<Node, List<Node>> adj2 = buildAdjacency(g2);
        IdentityHashMap<Node, String> colors1 = initialColors(g1, adj1);
        IdentityHashMap<Node, String> colors2 = initialColors(g2, adj2);

        int maxIter = g1.getNodes().size() + 1;
        for (int i = 0; i < maxIter; i++) {
            IdentityHashMap<Node, String> next1 = refineColors(colors1, adj1);
            IdentityHashMap<Node, String> next2 = refineColors(colors2, adj2);
            if (!histogramsMatch(next1, next2)) return false;
            boolean stable1 = colors1.entrySet().stream().allMatch(e -> e.getValue().equals(next1.get(e.getKey())));
            boolean stable2 = colors2.entrySet().stream().allMatch(e -> e.getValue().equals(next2.get(e.getKey())));
            colors1 = next1;
            colors2 = next2;
            if (stable1 && stable2) break;
        }
        return true;
    }

    private static IdentityHashMap<Node, String> initialColors(Graph g, IdentityHashMap<Node, List<Node>> adj) {
        // seed colour = degree
        IdentityHashMap<Node, String> colors = new IdentityHashMap<>();
        for (Node n : g.getNodes()) {
            colors.put(n, String.valueOf(adj.getOrDefault(n, Collections.emptyList()).size()));
        }
        return colors;
    }

    private static IdentityHashMap<Node, String> refineColors(IdentityHashMap<Node, String> colors,
                                                              IdentityHashMap<Node, List<Node>> adj) {
        IdentityHashMap<Node, String> next = new IdentityHashMap<>();
        for (Node n : colors.keySet()) {
            List<String> nbColors = adj.getOrDefault(n, Collections.emptyList()).stream()
                    .map(colors::get)
                    .sorted()
                    .toList();
            String combined = colors.get(n) + "|" + nbColors;
            next.put(n, Integer.toHexString(combined.hashCode()));
        }
        return next;
    }

    private static boolean histogramsMatch(IdentityHashMap<Node, String> c1, IdentityHashMap<Node, String> c2) {
        Map<String, Long> h1 = c1.values().stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        Map<String, Long> h2 = c2.values().stream().collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        return h1.equals(h2);
    }

    // VF2 — returns the full node mapping (g1 node -> g2 node), or null if no iso found
    private static IdentityHashMap<Node, Node> vf2(Graph g1, Graph g2) {
        IdentityHashMap<Node, IdentityHashMap<Node, Boolean>> adj1 = buildAdjacencySet(g1);
        IdentityHashMap<Node, IdentityHashMap<Node, Boolean>> adj2 = buildAdjacencySet(g2);
        IdentityHashMap<Node, Node> mapping = new IdentityHashMap<>();
        boolean found = backtrack(g1.getNodes(), g2.getNodes(), adj1, adj2,
                mapping, new IdentityHashMap<>(), 0);
        return found ? mapping : null;
    }

    private static boolean backtrack(List<Node> nodes1, List<Node> nodes2,
                                      IdentityHashMap<Node, IdentityHashMap<Node, Boolean>> adj1,
                                      IdentityHashMap<Node, IdentityHashMap<Node, Boolean>> adj2,
                                      IdentityHashMap<Node, Node> mapping,
                                      IdentityHashMap<Node, Boolean> used,
                                      int depth) {
        if (depth == nodes1.size()) return true;
        Node n1 = nodes1.get(depth);
        for (Node n2 : nodes2) {
            if (used.containsKey(n2)) continue;
            if (isCompatible(n1, n2, adj1, adj2, mapping)) {
                mapping.put(n1, n2);
                used.put(n2, true);
                if (backtrack(nodes1, nodes2, adj1, adj2, mapping, used, depth + 1)) return true;
                mapping.remove(n1);
                used.remove(n2);
            }
        }
        return false;
    }

    private static boolean isCompatible(Node n1, Node n2,
                                         IdentityHashMap<Node, IdentityHashMap<Node, Boolean>> adj1,
                                         IdentityHashMap<Node, IdentityHashMap<Node, Boolean>> adj2,
                                         IdentityHashMap<Node, Node> mapping) {
        IdentityHashMap<Node, Boolean> nb1 = adj1.getOrDefault(n1, new IdentityHashMap<>());
        IdentityHashMap<Node, Boolean> nb2 = adj2.getOrDefault(n2, new IdentityHashMap<>());
        if (nb1.size() != nb2.size()) return false;
        for (Map.Entry<Node, Node> entry : mapping.entrySet()) {
            if (nb1.containsKey(entry.getKey()) != nb2.containsKey(entry.getValue())) return false;
        }
        return true;
    }

    private static IdentityHashMap<Node, List<Node>> buildAdjacency(Graph g) {
        IdentityHashMap<Node, List<Node>> adj = new IdentityHashMap<>();
        for (Node n : g.getNodes()) adj.put(n, new ArrayList<>());
        for (var e : g.getEdges()) {
            if (e.getSourceNode() != null && e.getTargetNode() != null) {
                adj.get(e.getSourceNode()).add(e.getTargetNode());
                adj.get(e.getTargetNode()).add(e.getSourceNode());
            }
        }
        return adj;
    }

    private static IdentityHashMap<Node, IdentityHashMap<Node, Boolean>> buildAdjacencySet(Graph g) {
        IdentityHashMap<Node, IdentityHashMap<Node, Boolean>> adj = new IdentityHashMap<>();
        for (Node n : g.getNodes()) adj.put(n, new IdentityHashMap<>());
        for (var e : g.getEdges()) {
            if (e.getSourceNode() != null && e.getTargetNode() != null) {
                adj.get(e.getSourceNode()).put(e.getTargetNode(), true);
                adj.get(e.getTargetNode()).put(e.getSourceNode(), true);
            }
        }
        return adj;
    }
}
