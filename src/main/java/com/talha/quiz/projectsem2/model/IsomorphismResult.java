package com.talha.quiz.projectsem2.model;

import java.time.LocalDateTime;
import java.util.IdentityHashMap;

public class IsomorphismResult {
    // Attributes
    private final Integer id;
    private final Graph g1;
    private final Graph g2;
    private final boolean vertexMatch;
    private final boolean edgeMatch;
    private final boolean degreeMatch;
    private final boolean connectedMatch;
    private final boolean bipartiteMatch;
    private final boolean eulerMatch;
    private final boolean isIsomorphic;
    private final LocalDateTime checkedAt;
    // node mapping from g1 -> g2, populated only when isIsomorphic == true
    private final IdentityHashMap<Node, Node> mapping;

    // Constructor
    public IsomorphismResult(Graph g1, Graph g2,
                              boolean vertexMatch, boolean edgeMatch, boolean degreeMatch,
                              boolean connectedMatch, boolean bipartiteMatch, boolean eulerMatch,
                              boolean isIsomorphic, IdentityHashMap<Node, Node> mapping) {
        this.id = null;
        this.g1 = g1;
        this.g2 = g2;
        this.vertexMatch = vertexMatch;
        this.edgeMatch = edgeMatch;
        this.degreeMatch = degreeMatch;
        this.connectedMatch = connectedMatch;
        this.bipartiteMatch = bipartiteMatch;
        this.eulerMatch = eulerMatch;
        this.isIsomorphic = isIsomorphic;
        this.checkedAt = LocalDateTime.now();
        this.mapping = mapping;
    }

    // Methods
    public Integer getId() { return id; }
    public Graph getG1() { return g1; }
    public Graph getG2() { return g2; }
    public boolean isVertexMatch() { return vertexMatch; }
    public boolean isEdgeMatch() { return edgeMatch; }
    public boolean isDegreeMatch() { return degreeMatch; }
    public boolean isConnectedMatch() { return connectedMatch; }
    public boolean isBipartiteMatch() { return bipartiteMatch; }
    public boolean isEulerMatch() { return eulerMatch; }
    public boolean isIsomorphic() { return isIsomorphic; }
    public LocalDateTime getCheckedAt() { return checkedAt; }
    public IdentityHashMap<Node, Node> getMapping() { return mapping; }
}
